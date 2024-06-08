package de.kobich.audiosolutions.frontend.audio.editor.playlist;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.part.EditorPart;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylist;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import de.kobich.audiosolutions.core.service.playlist.PlaylistService;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditorComparator.Direction;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.selection.SelectionManager;
import de.kobich.audiosolutions.frontend.common.ui.editor.EditorLayoutManager;
import de.kobich.audiosolutions.frontend.common.ui.editor.IEditorLayoutSupport;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutType;
import de.kobich.commons.ui.DelayListener;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.listener.TreeExpandKeyListener;
import de.kobich.commons.ui.jface.tree.TreeColumnLayoutManager;
import de.kobich.commons.ui.memento.IMementoItem;
import lombok.Getter;

public class PlaylistEditor extends EditorPart implements PropertyChangeListener, IEditorLayoutSupport {
	private static final Logger logger = Logger.getLogger(PlaylistEditor.class);
	public static final String ID = "de.kobich.audiosolutions.editor.playlistEditor";

	private FormToolkit toolkit;
	private Form form;
	private Text name;
	private Text filterText;
	private PlaylistEditorViewerFilter filter;
	private TreeViewer treeViewer;
	private PlaylistEditorEventListener eventListener;
	@Getter
	private EditablePlaylist playlist;
	@Getter
	private TreeColumnLayoutManager columnManager;
	private EditorLayoutManager layoutManager;
	private boolean dirty;
	private IMementoItem mementoItem;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		if (input instanceof PlaylistEditorInput playlistInput) {
			IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
			this.mementoItem = MementoUtils.getMementoItemToSave(dialogSettings, ID);

			this.playlist = playlistInput.getEditablePlaylist();
			this.playlist.getPropertyChangeSupport().addPropertyChangeListener(this);
			this.eventListener = new PlaylistEditorEventListener(this);
			this.eventListener.register();
			this.layoutManager = new EditorLayoutManager(this, mementoItem);
			this.dirty = false;
			setPartName(this.playlist.getName());
		}
		else {
			throw new PartInitException("Illegal editor iput: " + input);
		}
	}
	
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		GridLayout gl = new GridLayout(1, true);
		gl.marginHeight = 0;
		form.getBody().setLayout(gl);
		
		// name
		final DelayListener<ModifyEvent> delayListener = new DelayListener<>(50, TimeUnit.MILLISECONDS) {
			@Override
			public void handleEvent(List<ModifyEvent> events) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						playlist.setName(name.getText());
						setPartName(playlist.getName());
					}
				});
			}
		};
		
		Composite searchSection = toolkit.createComposite(form.getBody());
		GridLayout searchSectionLayout = new GridLayout(2, false);
		searchSectionLayout.marginHeight = 20;
		searchSection.setLayout(searchSectionLayout);
		searchSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toolkit.createLabel(searchSection, "Playlist:");
		name = toolkit.createText(searchSection, "", SWT.BORDER);
		name.setText(playlist.getName());
		name.setMessage("Name is required");
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		name.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				delayListener.delayEvent(e);
			}
		});
		
		// content
		Section contentSection = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);
		contentSection.setText("Folders And Files");
		contentSection.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite contentComposite = toolkit.createComposite(contentSection);
		contentComposite.setLayout(JFaceUtils.createViewGridLayout(1, false));
		contentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentSection.setClient(contentComposite);
		
		// layout + filter
		Composite filterSection = toolkit.createComposite(contentComposite);
		filterSection.setLayout(new GridLayout(3, false));
		filterSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// layout
		this.layoutManager.restoreState();
		Image iconFlat = Activator.getDefault().getImage(ImageKey.LAYOUT_FLAT);
		this.layoutManager.createButton(filterSection, SWT.TOGGLE, LayoutType.FLAT, iconFlat);
		Image iconHierarchical = Activator.getDefault().getImage(ImageKey.LAYOUT_HIERARCHICAL);
		this.layoutManager.createButton(filterSection, SWT.TOGGLE, LayoutType.HIERARCHICAL, iconHierarchical);
		// filter
		filter = new PlaylistEditorViewerFilter();
		filterText = new Text(filterSection, SWT.BORDER | SWT.SEARCH);
		filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterText.setMessage("Filter files");
		filterText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent evt) {
				filter.setSearchText("*" + filterText.getText() + "*");
				refresh();
				// update selection (required for filtering)
				treeViewer.setSelection(treeViewer.getSelection());
			}
		});
		filterText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					filterText.setText("");
					filter.setSearchText("");
					refresh();
					// update selection (required for filtering)
					treeViewer.setSelection(treeViewer.getSelection());
				}
			}
		});		

		// file list
		Composite fileListComposite = toolkit.createComposite(contentComposite);
		fileListComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		TreeColumnLayout treeColumnLayout = new TreeColumnLayout();
		fileListComposite.setLayout(treeColumnLayout);
		
		Tree tree = toolkit.createTree(fileListComposite, SWT.FULL_SELECTION | SWT.MULTI);
		tree.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		treeViewer = new TreeViewer(tree);
		treeViewer.setComparator(new PlaylistEditorComparator(PlaylistEditorColumn.NAME));
		
		this.columnManager = new TreeColumnLayoutManager(fileListComposite, treeViewer, mementoItem);
		this.columnManager.setTreeColumnProvider(columnData -> {
			final PlaylistEditorColumn column = (PlaylistEditorColumn) columnData.getElement();
			
			TreeViewerColumn treeViewerColumn = new TreeViewerColumn(treeViewer, column.getStyle());
			treeViewerColumn.setLabelProvider(column.createCellLabelProvider());
			treeViewerColumn.setEditingSupport(column.createEditingSupport(this, treeViewer));
			
			TreeColumn treeColumn = treeViewerColumn.getColumn();
			treeColumn.setText(column.getLabel());
			treeColumn.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Direction direction = ((PlaylistEditorComparator) treeViewer.getComparator()).setSortColumn(column);
					int dir = (Direction.ASCENDING.equals(direction)) ? SWT.UP : SWT.DOWN; 
					treeViewer.getTree().setSortDirection(dir);
					treeViewer.getTree().setSortColumn(treeColumn);
					treeViewer.refresh();
				}
			});
			return treeColumn;
			
		});
		for (PlaylistEditorColumn column : PlaylistEditorColumn.values()) {
			columnManager.addColumn(column.createTreeColumnData());
		}
		columnManager.restoreState();
		columnManager.createColumns();
		
		treeViewer.setContentProvider(new PlaylistEditorContentProvider(layoutManager));
		tree.addKeyListener(new TreeExpandKeyListener(treeViewer));
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		treeViewer.addFilter(filter);
		treeViewer.setInput(this.playlist);

		SelectionManager.INSTANCE.registerEditor(this, treeViewer);
		
		// enable context menu
		MenuManager menuManager = new MenuManager();
		Menu menuFlat = menuManager.createContextMenu(tree);
		tree.setMenu(menuFlat);
		getSite().registerContextMenu(menuManager, treeViewer);
	}

	@Override
	public void setFocus() {
		if (StringUtils.isBlank(this.name.getText())) {
			this.name.setFocus();
		}
		else {
			this.filterText.setFocus();
		}
	}

	@Override
	public void dispose() {
		this.playlist.getPropertyChangeSupport().removePropertyChangeListener(this);
		this.eventListener.deregister();
		SelectionManager.INSTANCE.deregisterEditor(this, treeViewer);
		toolkit.dispose();
		form.dispose();
		super.dispose();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		JFaceThreadRunner runner = new JFaceThreadRunner("Saving Playlist", getSite().getShell(), List.of(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2)) {

			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case UI_1:
					if (StringUtils.isBlank(playlist.getName())) {
						MessageDialog.openError(super.getParent(), super.getName(), "Please set a name for the playlist.");
						super.setNextState(RunningState.UI_ERROR);
					}
					break;
				case WORKER_1:
					PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
					playlistService.savePlaylist(playlist, super.getProgressMonitor());
					break;
				case UI_2:
					PlaylistEditor.this.dirty = false;
					firePropertyChange(PROP_DIRTY);
					
					UIEvent event = new UIEvent(ActionType.PLAYLIST_SAVED);
					event.getPlaylistDelta().getPlaylistIds().add(playlist.getId().orElse(null));
					EventSupport.INSTANCE.fireEvent(event);
					
					break;
				case UI_ERROR:
					Exception e = super.getException();
					logger.error(e.getMessage(), e);
					String msg = "Saving playlist failed: " + e.getMessage();
					MessageDialog.openError(super.getParent(), super.getName(), msg);
					break;
				default:
					break;
				}
			}
			
		};
		runner.runProgressMonitorDialog(true, false);
	}

	@Override
	public void doSaveAs() {}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public Optional<EditablePlaylistFolder> getFirstSelectedFolder() {
		ITreeSelection selection = treeViewer.getStructuredSelection();
		for (Object o : selection.toList()) {
			if (o instanceof EditablePlaylistFolder folder) {
				return Optional.of(folder);
			}
		}
		return Optional.empty();
	}
	
	public Optional<EditablePlaylistFile> getFirstSelectedFile() {
		ITreeSelection selection = treeViewer.getStructuredSelection();
		for (Object o : selection.toList()) {
			if (o instanceof EditablePlaylistFile file) {
				return Optional.of(file);
			}
		}
		return Optional.empty();
	}
	
	public PlaylistSelection getSelection() {
		List<EditablePlaylistFile> files = new ArrayList<>();
		List<EditablePlaylistFolder> folders = new ArrayList<>();
		ITreeSelection selection = treeViewer.getStructuredSelection();
		for (Object o : selection.toList()) {
			if (o instanceof EditablePlaylistFile file) {
				files.add(file);
			}
			else if (o instanceof EditablePlaylistFolder folder) {
				folders.add(folder);
			}
		}
		return new PlaylistSelection(files, folders, filter);
	}
	
	public void setSelection(Object[] items) {
		getSite().getShell().getDisplay().syncExec(() -> {
			StructuredSelection sel = new StructuredSelection(items);
			treeViewer.setSelection(sel, true);
		});
	}
	
	public void collapseAll() {
		getSite().getShell().getDisplay().syncExec(() -> treeViewer.collapseAll());
	}
	
	public void refresh() {
		getSite().getShell().getDisplay().syncExec(() -> treeViewer.refresh());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (this.dirty) {
			return;
		}
		this.dirty = true;
		getSite().getShell().getDisplay().asyncExec(() -> firePropertyChange(PROP_DIRTY));
	}

	@Override
	public void switchLayout(LayoutType layout) {
		layoutManager.saveState();
		
		ISelection selection = switchSelection(getSelection(), (ITreeContentProvider) treeViewer.getContentProvider(), layout);
		refresh();
		treeViewer.setSelection(selection);
	}
	
	/**
	 * Returns the selection for the new layout (tries to select as less nodes as possible)
	 */
	protected ISelection switchSelection(PlaylistSelection selection, ITreeContentProvider contentProvider, LayoutType layoutType) {
		List<Object> elements = new ArrayList<>();
		Set<EditablePlaylistFile> filesInElements = new HashSet<>();
		
		switch (layoutType) {
			case FLAT:
				return new StructuredSelection(selection.getAllFiles());
			default:
			case HIERARCHICAL:
				for (EditablePlaylistFile file : selection.getAllFiles()) {
					if (filesInElements.contains(file)) {
						continue;
					}
					
					// get parent node
					Object parent = contentProvider.getParent(file);
					EditablePlaylistFolder folder = null;
					if (parent instanceof EditablePlaylistFolder f) {
						folder = f;
					}
					
					// if all children of parent node should be selected 
					if (folder != null && selection.getAllFiles().containsAll(folder.getFiles())) {
						elements.add(folder);
						filesInElements.addAll(folder.getFiles());
					}
					else {
						elements.add(file);
						filesInElements.add(file);
					}
				}
				return new StructuredSelection(elements);
		}
		
	}

}
