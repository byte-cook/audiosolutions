package de.kobich.audiosolutions.frontend.audio.view.artists;

import java.util.ArrayList;
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
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.frontend.audio.view.artists.action.OpenTracksOfArtistAction;
import de.kobich.audiosolutions.frontend.audio.view.artists.model.ArtistItem;
import de.kobich.audiosolutions.frontend.audio.view.artists.model.ArtistsModel;
import de.kobich.audiosolutions.frontend.audio.view.artists.ui.ArtistsColumnType;
import de.kobich.audiosolutions.frontend.audio.view.artists.ui.ArtistsComparator;
import de.kobich.audiosolutions.frontend.audio.view.artists.ui.ArtistsContentProvider;
import de.kobich.audiosolutions.frontend.audio.view.artists.ui.ArtistsLabelProvider;
import de.kobich.commons.ui.DelayListener;
import de.kobich.commons.ui.jface.JFaceUtils;

/**
 * Lent medium view.
 */
public class ArtistsView extends ViewPart {
	public static final String ID = "de.kobich.audiosolutions.view.audio.artistsView";
	private TableViewer tableViewer;
	private Text filterText;
	private ArtistsViewEventListener eventListener;
	
	@Override
	public void init(IViewSite viewSite, IMemento memento) throws PartInitException {
		super.init(viewSite, memento);
		this.eventListener = new ArtistsViewEventListener(this);
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
				ArtistsView.this.refresh();
			}
		};
		filterText = new Text(parent, SWT.BORDER | SWT.SEARCH);
		filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterText.setMessage("Filter artists");
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
		
		// artists
		Composite tableComposite = new Composite(parent, SWT.NONE);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer = new TableViewer(tableComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();

		for (ArtistsColumnType column : ArtistsColumnType.values()) {
			TableViewerColumn mediumColumn = new TableViewerColumn(tableViewer, SWT.LEFT, column.getIndex());
			mediumColumn.getColumn().setText(column.getLabel());
			tableColumnLayout.setColumnData(mediumColumn.getColumn(), new ColumnWeightData(column.getWidthPercent()));
		}

		// turn on the header and the lines
		tableViewer.setContentProvider(new ArtistsContentProvider());
		tableViewer.setLabelProvider(new ArtistsLabelProvider());
		tableViewer.setComparator(new ArtistsComparator(ArtistsColumnType.NAME));
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				try {
					IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
					handlerService.executeCommand(OpenTracksOfArtistAction.ID, null);
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
	 * @return the selected artist
	 */
	public ArtistItem getSelectedArtistItem() {
		ArtistItem artistItem = null;
		if (tableViewer.getTable().getSelectionCount() > 0) {
			TableItem[] selection = tableViewer.getTable().getSelection();
			Object data = selection[0].getData();
			if (data instanceof ArtistItem) {
				artistItem = (ArtistItem) data;
			}
		}
		return artistItem;
	}
	
	/**
	 * Returns medium items
	 * @return
	 */
	public Set<ArtistItem> getSelectedArtistItems() {
		Set<ArtistItem> artistItems = new HashSet<ArtistItem>();
		if (tableViewer.getTable().getSelectionCount() > 0) {
			TableItem[] items = tableViewer.getTable().getSelection();
			for (TableItem item : items) {
				Object data = item.getData();
				if (data instanceof ArtistItem) {
					artistItems.add((ArtistItem) data);
				}
			}
		}
		return artistItems;
	}
	
	/**
	 * Returns artists 
	 * @return
	 */
	public Set<ArtistItem> getArtistItems() {
		Set<ArtistItem> artistItems = new HashSet<ArtistItem>();
		TableItem[] items = new TableItem[0];
		if (tableViewer.getTable().getSelectionCount() > 0) {
			items = tableViewer.getTable().getSelection();
		}
		else {
			items = tableViewer.getTable().getItems();
		}
		for (TableItem item : items) {
			Object data = item.getData();
			if (data instanceof ArtistItem) {
				artistItems.add((ArtistItem) data);
			}
		}
		return artistItems;
	}
	
	/**
	 * @param model the model to set
	 */
	public void setModel(final ArtistsModel model) {
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!ArtistsView.this.tableViewer.getTable().isDisposed()) {
					ArtistsView.this.tableViewer.setInput(model);
					ArtistsView.this.tableViewer.refresh();
				}
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
				ArtistsView.this.setContentDescription(contentDescription);
			}
		});
	}

	/**
	 * LoadingJob
	 */
	private class LoadingJob extends Job {
		private final ArtistsView view;
		private final String filter;
		
		public LoadingJob(ArtistsView view, String filter) {
			super("Load Artists");
			this.view = view;
			this.filter = StringUtils.isNotBlank(filter) ? "*" + filter + "*" : null;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			AudioSearchService audioSearchService = AudioSolutions.getService(AudioSearchService.class);
			List<Artist> artists = audioSearchService.searchArtists(filter);
			if (artists.isEmpty()) {
				view.asyncSetContentDescription("No artists available");
			}
			else {
				view.asyncSetContentDescription(artists.size() + " artist(s) found");
			}
			
			List<ArtistItem> artistItems = new ArrayList<ArtistItem>();
			for (Artist artist : artists) {
				artistItems.add(new ArtistItem(artist));
			}
			ArtistsModel model = new ArtistsModel(artistItems);
			view.setModel(model);
			return Status.OK_STATUS;
		}
	}
}