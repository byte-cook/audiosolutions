package de.kobich.audiosolutions.frontend.audio.view.mediums;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
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
import de.kobich.audiosolutions.frontend.audio.view.mediums.ui.MediumColumnType;
import de.kobich.audiosolutions.frontend.audio.view.mediums.ui.MediumComparator;
import de.kobich.audiosolutions.frontend.audio.view.mediums.ui.MediumContentProvider;
import de.kobich.audiosolutions.frontend.audio.view.mediums.ui.MediumLabelProvider;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.DelayListener;
import de.kobich.commons.ui.jface.JFaceExec;
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
				Display.getDefault().asyncExec(() -> MediumsView.this.refresh());
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
		final String filter = StringUtils.isNotBlank(filterText.getText()) ? "*" + filterText.getText() + "*" : null;
		final Wrapper<List<Medium>> mediums = Wrapper.empty();
		JFaceExec.builder(getSite().getShell(), "Loading Mediums")
			.ui(ctx -> setContentDescription("Loading..."))
			.worker(ctx -> {
				AudioSearchService searchService = AudioSolutions.getService(AudioSearchService.class);
				 mediums.set(searchService.searchMediums(filter));
			})
			.ui(ctx -> {
				List<Medium> m = mediums.orElse(List.of());
				if (mediums.isEmpty()) {
					setContentDescription("No mediums available");
				}
				else {
					setContentDescription(m.size() + " medium(s) found");
				}
				
				MediumModel model = new MediumModel(m);
				tableViewer.setInput(model);
				tableViewer.refresh();
			})
			.runBackgroundJob(100, false, true, null);
	}
	
	public Optional<Medium> getSelectedMedium() {
		if (tableViewer.getTable().getSelectionCount() > 0) {
			TableItem[] selection = tableViewer.getTable().getSelection();
			Object data = selection[0].getData();
			if (data instanceof Medium medium) {
				return Optional.of(medium);
			}
		}
		return Optional.empty();
	}
	
	public Set<Medium> getSelectedMediums() {
		Set<Medium> mediums = new HashSet<>();
		if (tableViewer.getTable().getSelectionCount() > 0) {
			TableItem[] items = tableViewer.getTable().getSelection();
			for (TableItem item : items) {
				Object data = item.getData();
				if (data instanceof Medium medium) {
					mediums.add(medium);
				}
			}
		}
		return mediums;
	}
	
}