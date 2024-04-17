package de.kobich.audiosolutions.frontend.audio.view.playlist;

import java.util.HashSet;
import java.util.List;
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
import de.kobich.audiosolutions.core.service.playlist.PlaylistService;
import de.kobich.audiosolutions.core.service.playlist.repository.Playlist;
import de.kobich.audiosolutions.frontend.audio.view.playlist.action.OpenPlaylistsAction;
import de.kobich.audiosolutions.frontend.audio.view.playlist.ui.PlaylistColumnType;
import de.kobich.audiosolutions.frontend.audio.view.playlist.ui.PlaylistComparator;
import de.kobich.audiosolutions.frontend.audio.view.playlist.ui.PlaylistContentProvider;
import de.kobich.audiosolutions.frontend.audio.view.playlist.ui.PlaylistLabelProvider;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.DelayListener;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.commons.ui.jface.JFaceUtils;

public class PlaylistView extends ViewPart {
	public static final String ID = "de.kobich.audiosolutions.view.audio.playlistsView";
	private TableViewer tableViewer;
	private Text filterText;
	private PlaylistViewEventListener eventListener;
	
	@Override
	public void init(IViewSite viewSite, IMemento memento) throws PartInitException {
		super.init(viewSite, memento);
		this.eventListener = new PlaylistViewEventListener(this);
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
				Display.getDefault().asyncExec(() -> PlaylistView.this.refresh());
			}
		};
		
		filterText = new Text(parent, SWT.BORDER | SWT.SEARCH);
		filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterText.setMessage("Filter playlists");
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
//		filterText.addTraverseListener(new TraverseListener() {
//			@Override
//			public void keyTraversed(TraverseEvent e) {
//				delayListener.delayEvent(e);
//			}
//		});
		
		// artists
		Composite tableComposite = new Composite(parent, SWT.NONE);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer = new TableViewer(tableComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();

		for (PlaylistColumnType column : PlaylistColumnType.values()) {
			TableViewerColumn mediumColumn = new TableViewerColumn(tableViewer, SWT.LEFT, column.getIndex());
			mediumColumn.getColumn().setText(column.getLabel());
			tableColumnLayout.setColumnData(mediumColumn.getColumn(), new ColumnWeightData(column.getWidthPercent()));
		}

		// turn on the header and the lines
		tableViewer.setContentProvider(new PlaylistContentProvider());
		tableViewer.setLabelProvider(new PlaylistLabelProvider());
		tableViewer.setComparator(new PlaylistComparator(PlaylistColumnType.NAME));
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				try {
					IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
					handlerService.executeCommand(OpenPlaylistsAction.ID, null);
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
		final Wrapper<List<Playlist>> playlists = Wrapper.empty();
		JFaceExec.builder(getSite().getShell(), "Loading Playlists")
			.ui(ctx -> setContentDescription("Loading..."))
			.worker(ctx -> {
				PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
				playlists.set(playlistService.getPlaylists(filter));
			})
			.ui(ctx -> {
				List<Playlist> p = playlists.orElse(List.of());
				if (p.isEmpty()) {
					setContentDescription("No playlist available");
				}
				else {
					setContentDescription(p.size() + " playlist(s) found");
				}
				
				PlaylistModel model = new PlaylistModel(p);
				setModel(model);
			})
			.runBackgroundJob(100, false, true, null);
	}
	
	public Set<Playlist> getSelectedPlaylists() {
		Set<Playlist> playlists = new HashSet<>();
		if (tableViewer.getTable().getSelectionCount() > 0) {
			TableItem[] items = tableViewer.getTable().getSelection();
			for (TableItem item : items) {
				Object data = item.getData();
				if (data instanceof Playlist playlist) {
					playlists.add(playlist);
				}
			}
		}
		return playlists;
	}
	
	/**
	 * @param model the model to set
	 */
	public void setModel(final PlaylistModel model) {
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!PlaylistView.this.tableViewer.getTable().isDisposed()) {
					PlaylistView.this.tableViewer.setInput(model);
					PlaylistView.this.tableViewer.refresh();
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
				PlaylistView.this.setContentDescription(contentDescription);
			}
		});
	}

}