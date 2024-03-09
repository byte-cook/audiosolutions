package de.kobich.audiosolutions.frontend.audio.view.mediums;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.frontend.audio.view.mediums.action.OpenTracksOfMediumsAction;
import de.kobich.audiosolutions.frontend.audio.view.mediums.model.MediumItem;
import de.kobich.audiosolutions.frontend.audio.view.mediums.model.MediumModel;
import de.kobich.audiosolutions.frontend.audio.view.mediums.ui.MediumColumnType;
import de.kobich.audiosolutions.frontend.audio.view.mediums.ui.MediumComparator;
import de.kobich.audiosolutions.frontend.audio.view.mediums.ui.MediumContentProvider;
import de.kobich.audiosolutions.frontend.audio.view.mediums.ui.MediumLabelProvider;
import de.kobich.commons.ui.DelayListener;
import de.kobich.commons.ui.jface.JFaceUtils;

/**
 * Lent medium view.
 */
public class MediumsView extends ViewPart {
	public static final String ID = "de.kobich.audiosolutions.view.audio.mediumsView";
	private TableViewer tableViewer;
	private Text filterText;
	private MediumsViewEventListener eventListener;
	
	@Override
	public void init(IViewSite viewSite, IMemento memento) throws PartInitException {
		super.init(viewSite, memento);
		this.eventListener = new MediumsViewEventListener(this);
	}

	/**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
	public void createPartControl(Composite parent) {
		parent.setLayout(JFaceUtils.createViewGridLayout(1, false, JFaceUtils.MARGIN_TOP));
		
		// filter
		DelayListener<TypedEvent> delayListener = new DelayListener<>(500, TimeUnit.MILLISECONDS) {
			@Override
			public void handleEvent(List<TypedEvent> events) {
				MediumsView.this.refresh();
			}
		};
		filterText = new Text(parent, SWT.BORDER | SWT.SEARCH);
		filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterText.setMessage("Filter mediums");
		filterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				delayListener.delayEvent(e);
			}
		});
		filterText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					filterText.setText("");
					delayListener.delayEvent(e);
				}
			}
		});
		
		// mediums
		Composite tableComposite = new Composite(parent, SWT.NONE);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer = new TableViewer(tableComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();

		for (MediumColumnType column : MediumColumnType.values()) {
			TableViewerColumn mediumColumn = new TableViewerColumn(tableViewer, SWT.LEFT, column.getIndex());
			mediumColumn.getColumn().setText(column.getLabel());
			tableColumnLayout.setColumnData(mediumColumn.getColumn(), new ColumnWeightData(column.getWidthPercent()));
		}

		// turn on the header and the lines
		tableViewer.setContentProvider(new MediumContentProvider());
		tableViewer.setLabelProvider(new MediumLabelProvider());
		tableViewer.setComparator(new MediumComparator(MediumColumnType.MEDIUM));
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				try {
					IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
					handlerService.executeCommand(OpenTracksOfMediumsAction.ID, null);
				}
				catch (Exception exc) {
					MessageDialog.openError(getSite().getShell(), getSite().getShell().getText(), exc.getMessage());
				}
			}
		});
		refresh();
		
		// register for events
		eventListener.register();
		
		// enable context menu
		MenuManager menuManager = new MenuManager();
		Menu menuFlat = menuManager.createContextMenu(table);
		table.setMenu(menuFlat);
		getSite().registerContextMenu(menuManager, tableViewer);

		// register for events
		getViewSite().setSelectionProvider(tableViewer);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		this.eventListener.deregister();
		this.tableViewer.getTable().dispose();
		this.filterText.dispose();
		super.dispose();
	}
	
	/**
	 * Refreshes this view
	 */
	public void refresh() {
		Display.getDefault().asyncExec(() -> {
			Job job = new LoadingJob(this, filterText.getText());
			job.setUser(false);
			job.setSystem(true);
			job.schedule();
		});
	}

	/**
	 * @return the selected item
	 */
	public MediumItem getSelectedMediumItem() {
		MediumItem lentItem = null;
		if (tableViewer.getTable().getSelectionCount() > 0) {
			TableItem[] selection = tableViewer.getTable().getSelection();
			Object data = selection[0].getData();
			if (data instanceof MediumItem) {
				lentItem = (MediumItem) data;
			}
		}
		return lentItem;
	}
	
	/**
	 * Returns medium items
	 * @return
	 */
	public Set<MediumItem> getSelectedMediumItems() {
		Set<MediumItem> mediumItems = new HashSet<MediumItem>();
		if (tableViewer.getTable().getSelectionCount() > 0) {
			TableItem[] items = tableViewer.getTable().getSelection();
			for (TableItem item : items) {
				Object data = item.getData();
				if (data instanceof MediumItem) {
					mediumItems.add((MediumItem) data);
				}
			}
		}
		return mediumItems;
	}
	
	/**
	 * @param model the model to set
	 */
	public void setModel(final MediumModel model) {
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MediumsView.this.tableViewer.setInput(model);
				MediumsView.this.tableViewer.refresh();
			}
		});
	}
	
	/**
	 * Sets content description asynchrony
	 * @param contentDescription
	 */
	public void asyncSetContentDescription(final String contentDescription) {
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MediumsView.this.setContentDescription(contentDescription);
			}
		});
	}

	/**
	 * LoadingJob
	 */
	private class LoadingJob extends Job {
		private final MediumsView view;
		private final String filter;
		
		public LoadingJob(MediumsView view, String filter) {
			super("Load Mediums");
			this.view = view;
			this.filter = StringUtils.isNotBlank(filter) ? "*" + filter + "*" : null;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			AudioSearchService lentMediumService = AudioSolutions.getService(AudioSearchService.class);
			List<Medium> lentMediums = lentMediumService.searchMediums(filter);
			
			if (lentMediums.isEmpty()) {
				view.asyncSetContentDescription("No mediums available");
			}
			else {
				view.asyncSetContentDescription(lentMediums.size() + " medium(s) found");
			}
			
			Set<MediumItem> mediumItems = new HashSet<MediumItem>();
			for (Medium medium : lentMediums) {
				mediumItems.add(new MediumItem(medium));
			}
			MediumModel model = new MediumModel(mediumItems);
			view.setModel(model);
			return Status.OK_STATUS;
		}
	}
}