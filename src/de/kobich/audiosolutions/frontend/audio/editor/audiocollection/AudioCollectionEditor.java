package de.kobich.audiosolutions.frontend.audio.editor.audiocollection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.info.FileInfo;
import de.kobich.audiosolutions.core.service.persist.AudioPersistenceService;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.AlbumTreeNode;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.AudioCollectionModel;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionContentProvider;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorCellModifier;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorColumn;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorComparator;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorComparator.Direction;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorLabelProvider;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.AudioDelta;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.FileDelta;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.audiosolutions.frontend.common.ui.AbstractTableTreeNode;
import de.kobich.audiosolutions.frontend.common.ui.ProgressDialog;
import de.kobich.audiosolutions.frontend.common.ui.editor.AbstractFormEditor;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorFileMonitor;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorUpdateManager;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorViewerFilter;
import de.kobich.audiosolutions.frontend.common.ui.editor.EditorLayoutManager;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileOpeningInfo;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.audiosolutions.frontend.common.ui.editor.IEditorLayoutSupport;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutDelta;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutDelta.AddItem;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutDelta.ReplaceItem;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutType;
import de.kobich.audiosolutions.frontend.common.ui.editor.LogoImagePostSelectionListener;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.audiosolutions.frontend.common.util.FileLabelUtil;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileDescriptorTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.RelativePathTreeNode;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.listener.TreeExpandKeyListener;
import de.kobich.commons.ui.jface.progress.ProgressMonitorAdapter;
import de.kobich.commons.ui.jface.tree.TreeColumnLayoutManager;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.component.file.FileDescriptor;
import lombok.Getter;

/**
 * Audio collection editor.
 */
public class AudioCollectionEditor extends AbstractFormEditor implements ICollectionEditor, IEditorLayoutSupport {
	public static final String ID = "de.kobich.audiosolutions.editor.audioCollectionEditor";
	private static final Logger logger = Logger.getLogger(AudioCollectionEditor.class);
	private static final Point LOGO_SIZE = new Point(192, 128);
	private FileCollection fileCollection;
	private AudioCollectionModel model;
	private AudioCollectionContentProvider contentProvider;
	private AudioCollectionEditorLabelProvider labelProvider;
	private CollectionEditorFileMonitor fileMonitor;
	private AudioCollectionEditorEventListener eventListener;
	@Getter
	private TreeColumnLayoutManager columnManager;
	private EditorLayoutManager layoutManager;
	private CollectionEditorUpdateManager editorUpdateManager;
	private TreeViewer treeViewer;
	private Composite treeComposite;
	private AudioCollectionEditorComparator comparator;
	private StyledText infoText;
	private StyledText artistText;
	private StyledText pathText;
	private StyledText fileSizeText;
	private CollectionEditorViewerFilter filter;
	private Text filterText;
	private boolean dirty;
	private Label logoLabel;
	private ToolTip logoTooltip;
	private Image defaultLogoImage;
	private Image smallLogoImage;
	private Image largeLogoImage;
	private IMementoItem mementoItem;

	@Override
	public void init(IEditorSite editorSite, IEditorInput editorInput) throws PartInitException {
		setSite(editorSite);
		setInput(editorInput);

		if (editorInput instanceof FileCollection) {
			IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
			this.mementoItem = MementoUtils.getMementoItemToSave(dialogSettings, ID);
			
			this.fileCollection = (FileCollection) editorInput;
			this.layoutManager = new EditorLayoutManager(this, mementoItem);
			this.contentProvider = new AudioCollectionContentProvider(this.layoutManager);
			this.eventListener = new AudioCollectionEditorEventListener(this);
			this.eventListener.register();
			this.model = new AudioCollectionModel(this.fileCollection, this.layoutManager);
			this.editorUpdateManager = new CollectionEditorUpdateManager(this, this.model);
			this.filter = new CollectionEditorViewerFilter();
			setPartName(fileCollection.getName());
			
			this.fileCollection.addPropertyChangeListener(this.eventListener);
			for (FileDescriptor fileDescriptor : getFileCollection().getFileDescriptors()) {
				fileDescriptor.addPropertyChangeListener(eventListener);
				if (fileDescriptor.hasMetaData() && fileDescriptor.getMetaData() instanceof AudioData) {
					AudioData audioData = (AudioData) fileDescriptor.getMetaData();
					audioData.addPropertyChangeListener(eventListener);
				}
			}

			// use file monitor
			if (CollectionEditorType.DIRECTORY.equals(fileCollection.getEditorType())) {
				FileOpeningInfo fileOpeningInfo = fileCollection.getOpeningInfo(FileOpeningInfo.class);
				this.fileMonitor = new CollectionEditorFileMonitor(this, this.getSite(), fileOpeningInfo);
				this.fileMonitor.start();
			}
		}
		else {
			throw new IllegalStateException("Illegal editor input type <" + editorInput.getClass().getName() + ">, expected<"
					+ FileCollection.class.getName() + ">");
		}
	}
	
	@Override
	public void dispose() {
		if (smallLogoImage != null) {
			smallLogoImage.dispose();
		}
		if (largeLogoImage != null) {
			largeLogoImage.dispose();
		}
		for (FileDescriptor fileDescriptor : getFileCollection().getFileDescriptors()) {
			fileDescriptor.removePropertyChangeListener(eventListener);
		}
		this.fileCollection.removePropertyChangeListener(this.eventListener);
		this.eventListener.deregister();
		this.labelProvider.dispose();
		if (this.fileMonitor != null) {
			this.fileMonitor.dispose();
		}
		this.filterText.dispose();
		this.infoText.dispose();
		this.artistText.dispose();
		this.pathText.dispose();
		this.fileSizeText.dispose();
		super.dispose();
	}

	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		try {
			ProgressDialog progressDialog = new ProgressDialog(getEditorSite().getShell());
			SaveRunnableWithProgress progressRunnable = new SaveRunnableWithProgress(getSite().getWorkbenchWindow());
			progressDialog.run(true, false, progressRunnable);
		}
		catch (Exception exc) {
			logger.error("Failed to save editor", exc);
		}
	}

	@Override
	public void doSaveAs() {}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Sets the editor's dirty attribute
	 * @param dirty
	 */
	public void setDirty(boolean dirty) {
		if (dirty == this.dirty) {
			return;
		}
		this.dirty = dirty;
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				firePropertyChange(PROP_DIRTY);
			}
		});
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	/**
	 * Creates the logo image
	 * @param informationComposite
	 */
	protected void makeLogoImage(Composite informationComposite) {
		defaultLogoImage = Activator.getDefault().getImage(ImageKey.AUDIO_COLLECTION_EDITOR);
		logoLabel = super.createLogo(informationComposite, LOGO_SIZE);
		logoLabel.setImage(defaultLogoImage);
		logoTooltip = new ToolTip(logoLabel, ToolTip.RECREATE, false) {
			@Override
			protected Composite createToolTipContentArea(Event event, Composite parent) {
				Label l = new Label(parent, SWT.NONE);
				l.setImage(largeLogoImage);
				return parent;
			}
		};
		logoTooltip.setShift(new Point(LOGO_SIZE.x * -1, 10));
		logoTooltip.deactivate();
	}

	/**
	 * Creates the information section
	 * @param informationComposite
	 */
	protected void makeInformation(Composite body) {
		Composite informationGroup = super.createSection(body, "", 4, Section.TITLE_BAR | Section.NO_TITLE | Section.EXPANDED);

		this.infoText = super.createStyledText(informationGroup, false);//getFormToolkit().createLabel(informationGroup, "", SWT.NONE);
		this.infoText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 4, 1));
		Font font = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
		this.infoText.setFont(font);
		this.artistText = super.createStyledText(informationGroup, false);//getFormToolkit().createLabel(informationGroup, "", SWT.NONE);
		this.artistText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 4, 1));
		this.pathText = super.createStyledText(informationGroup, false);
		this.pathText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 4, 1));
		this.fileSizeText = super.createStyledText(informationGroup, false);
		this.fileSizeText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 4, 1));

		// layout
		this.layoutManager.restoreState();
		Image iconFlat = Activator.getDefault().getImage(ImageKey.LAYOUT_FLAT);
		this.layoutManager.createButton(informationGroup, SWT.TOGGLE, LayoutType.FLAT, iconFlat);
		Image iconHierarchical = Activator.getDefault().getImage(ImageKey.LAYOUT_HIERARCHICAL);
		this.layoutManager.createButton(informationGroup, SWT.TOGGLE, LayoutType.HIERARCHICAL, iconHierarchical);
		Image iconAlbum = Activator.getDefault().getImage(ImageKey.LAYOUT_ALBUM);
		this.layoutManager.createButton(informationGroup, SWT.TOGGLE, LayoutType.ALBUM, iconAlbum);

		// filter
		filterText = new Text(informationGroup, SWT.BORDER | SWT.SEARCH);
		filterText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 1, 1));
		filterText.setMessage("Filter files");
		filterText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent evt) {
				filter.setSearchText("*" + filterText.getText() + "*");
				
				treeViewer.refresh();
				// update selection (required for filtering)
				treeViewer.setSelection(treeViewer.getSelection());
			}
		});
		filterText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					filterText.setText("");
					filter.setSearchText("*");
					
					treeViewer.refresh();
					// update selection (required for filtering)
					treeViewer.setSelection(treeViewer.getSelection());
				}
			}
		});
	}
	
	protected void makeContent(Composite parent) {
		FormToolkit toolkit = super.getFormToolkit();

		treeComposite = toolkit.createComposite(parent, SWT.NONE);
		TreeColumnLayout treeColumnLayout = new TreeColumnLayout();
		treeComposite.setLayout(treeColumnLayout);
		treeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		comparator = new AudioCollectionEditorComparator(AudioCollectionEditorColumn.FILE_NAME);

		Tree tree = toolkit.createTree(treeComposite, SWT.FULL_SELECTION | SWT.MULTI);
		treeViewer = new TreeViewer(tree);
		treeViewer.setComparator(this.comparator);
		treeViewer.addFilter(filter);
		this.columnManager = new TreeColumnLayoutManager(treeComposite, treeViewer, mementoItem);
		this.columnManager.setTreeColumnProvider(columnData -> {
			final AudioCollectionEditorColumn column = (AudioCollectionEditorColumn) columnData.getElement();
			
			TreeColumn treeColumn = new TreeColumn(tree, SWT.LEFT);
			treeColumn.setText(column.getLabel());
			treeColumn.setMoveable(true);
			treeColumn.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Direction direction = comparator.setSortColumn(column);
					int dir = (Direction.ASCENDING.equals(direction)) ? SWT.UP : SWT.DOWN; 
					treeViewer.getTree().setSortDirection(dir);
					treeViewer.getTree().setSortColumn(treeColumn);
					treeViewer.refresh();
				}
			});
			return treeColumn;
			
		});
		List<String> columnNames = new ArrayList<>();
		List<CellEditor> cellEditors = new ArrayList<>();
		for (AudioCollectionEditorColumn column : AudioCollectionEditorColumn.values()) {
			columnNames.add(column.name());
			cellEditors.add(new TextCellEditor(tree));
			columnManager.addColumn(column.createTreeColumnData());
		}
		columnManager.restoreState();
		columnManager.createColumns();

		this.labelProvider = new AudioCollectionEditorLabelProvider(columnManager);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setContentProvider(contentProvider);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.addKeyListener(new TreeExpandKeyListener(treeViewer));

		// add editor support
		CellEditor[] editors = cellEditors.toArray(new CellEditor[0]); 
	    String[] columnProperties = columnNames.toArray(new String[0]); 
	    treeViewer.setColumnProperties(columnProperties);
	    treeViewer.setCellModifier(new AudioCollectionEditorCellModifier(this));
		treeViewer.setCellEditors(editors);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		Object[] elements = treeViewer.getExpandedElements();
		ISelection selection = treeViewer.getSelection();
		model.initLayout();
		treeViewer.setInput(this.model);
		treeViewer.setExpandedElements(elements);
		treeViewer.setSelection(selection, true);
		
		treeViewer.addPostSelectionChangedListener(new LogoImagePostSelectionListener(this, filter));
		getSite().setSelectionProvider(treeViewer);
		SelectionSupport.INSTANCE.registerEditor(this, treeViewer);
		showDefaultLogo();
//		treeViewer.setSelection(treeViewer.getSelection(), true);
		
		// register context menu
		MenuManager menuManager = new MenuManager();
		Menu menuFlat = menuManager.createContextMenu(tree);
		tree.setMenu(menuFlat);
		getSite().registerContextMenu(menuManager, treeViewer);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		Form form = super.createForm(parent);
		Composite body = form.getBody();
		
		// information view
		Composite informationComposite = super.createInformationComposite(body);
		makeLogoImage(informationComposite);
		makeInformation(informationComposite);

		// tabs
		Composite contentComposite = super.createContentComposite(body);
		contentComposite.setLayout(JFaceUtils.createViewGridLayout(1, true, JFaceUtils.MARGIN_TOP));
		makeContent(contentComposite);
	}

	@Override
	public void setFocus() {
		treeViewer.getTree().setFocus();
	}
	
	@Override
	public void update(AudioDelta delta) {
		// Caution: Do not synchronize this method!
		// Reason: This method is called by UI- and non-UI threads and uses Display.syncExec(). Synchronization can result in blocking the UI thread!  
		final FileDescriptorSelection oldSelection = getFileDescriptorSelection();
		// update model
		final LayoutDelta layoutDelta = this.editorUpdateManager.update(delta);
		if (layoutDelta != null) {
			// update UI
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateTreeLayout(layoutDelta, oldSelection, true);
				}
			});
		}
	}
	
	@Override
	public void update(FileDelta delta) {
		// Caution: Do not synchronize this method!
		// Reason: This method is called by UI- and non-UI threads and uses Display.syncExec(). Synchronization can result in blocking the UI thread!  
		final FileDescriptorSelection oldSelection = getFileDescriptorSelection();
		// update model
		final LayoutDelta layoutDelta = this.editorUpdateManager.update(delta);
		if (layoutDelta != null) {
			// update UI
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateTreeLayout(layoutDelta, oldSelection, true);
				}
			});
		}
	}
	
	/**
	 * Update tree layout by using layout delta
	 * @param viewer
	 * @param layoutDelta
	 * @param layoutType
	 * @param activeLayout
	 */
	private void updateTreeLayout(LayoutDelta layoutDelta, FileDescriptorSelection oldSelection, boolean activeLayout) {
		try {
			treeViewer.getTree().setRedraw(false);
			
			LayoutType layoutType = this.layoutManager.getActiveLayout();
			
			// add
			if (layoutDelta.getAddItems().containsKey(layoutType)) {
				for (AddItem item : layoutDelta.getAddItems().get(layoutType)) {
					treeViewer.update(item.parent, new String[] { CollectionEditorViewerFilter.FILTER_PROP });
					treeViewer.add(item.parent, item.element);
				}
			}
			// selection
			if (activeLayout) {
				// do selection before remove because remove fires a SelectionChanged event if a selected item is affected
				List<Object> newSelectionElements = new ArrayList<Object>();
				
				// add old selection
				newSelectionElements.addAll(oldSelection.getElements());
				// replace
				if (layoutDelta.getReplaceItems().containsKey(layoutType)) {
					for (ReplaceItem item : layoutDelta.getReplaceItems().get(layoutType)) {
						for (Object o : oldSelection.getElements()) {
							if (o instanceof FileDescriptorTreeNode) {
								if (item.oldElement.equals(o)) {
									newSelectionElements.remove(item.oldElement);
									newSelectionElements.add(item.newElement);
								}
							}
						}
					}
				}
				
				ISelection newSel = new StructuredSelection(newSelectionElements);
				FileDescriptorSelection newSelection = new FileDescriptorSelection(newSel, filter);
				boolean selectionChanged = !newSelection.getFileDescriptors().equals(oldSelection.getFileDescriptors());
				if (selectionChanged) {
					treeViewer.setSelection(newSel);
				}
			}
			// update
			if (layoutDelta.getUpdateItems().containsKey(layoutType)) {
				for (Object element : layoutDelta.getUpdateItems().get(layoutType)) {
					treeViewer.update(element, null);
				}
			}
			// refresh
			if (layoutDelta.getRefreshItems().containsKey(layoutType)) {
				for (Object element : layoutDelta.getRefreshItems().get(layoutType)) {
					treeViewer.refresh(element);
				}
			}
			// remove
			if (layoutDelta.getRemoveItems().containsKey(layoutType)) {
				for (Object element : layoutDelta.getRemoveItems().get(layoutType)) {
					treeViewer.remove(element);
				}
			}
		}
		finally {
			treeViewer.getTree().setRedraw(true);
		}
	}

	/**
	 * Switches the layout
	 */
	public void switchLayout() {
		// TODO editor remove
	}
	
	/**
	 * @return the fileCollection
	 */
	public FileCollection getFileCollection() {
		return fileCollection;
	}
	
	@Override
	public FileDescriptorSelection getFileDescriptorSelection() {
		if (Display.getCurrent() != null) {
			// current thread is UI thread
			ISelection selection = treeViewer.getSelection();
			return new FileDescriptorSelection(selection, filter);
		}
		else {
			final Wrapper<FileDescriptorSelection> SELECTION = Wrapper.empty();
			JFaceExec.builder(getSite().getShell())
				.ui(ctx -> {
					ISelection selection = treeViewer.getSelection();
					SELECTION.set(new FileDescriptorSelection(selection, filter));
				})
				.run();
			return SELECTION.get();
		}
	}
	
	@Override
	public void showLogo(FileInfo fileInfo) {
		logoTooltip.hide();
		if (smallLogoImage != null) {
			smallLogoImage.dispose();
		}
		if (largeLogoImage != null) {
			largeLogoImage.dispose();
		}
		
		// show image
		ImageData imageData = super.getImageData(fileInfo).orElse(null);
		if (imageData != null) {
			ImageData smallImageData = JFaceUtils.scaleImageData(imageData, LOGO_SIZE.x, LOGO_SIZE.y);
			smallLogoImage = new Image(logoLabel.getDisplay(), smallImageData);
			ImageData largeImageData = JFaceUtils.scaleImageData(imageData, LOGO_SIZE.x * 4, LOGO_SIZE.y * 4);
			largeLogoImage = new Image(logoLabel.getDisplay(), largeImageData);
			logoLabel.setImage(smallLogoImage);
			logoTooltip.activate();
		}
		else {
			logoLabel.setImage(defaultLogoImage);
		}
		
		// show description
		String album = "";
//		String track = "";
		String artist = "";
		FileDescriptor fileDescriptor = fileInfo.getFileDescriptor();
		AudioData audioData = fileDescriptor.getMetaDataOptional(AudioData.class).orElse(null);
		if (audioData != null) {
			album = audioData.getAlbum().orElse("");
//			if (audioData.hasAttribute(AudioAttribute.TRACK)) {
//				track = audioData.getAttribute(AudioAttribute.TRACK);
//			}
			artist = audioData.getArtist().orElse("");
		}
		
		StringBuilder info = new StringBuilder();
		
		if (!album.isEmpty()) {
			info.append(album);
		}
//		if (!track.isEmpty()) {
//			if (!info.toString().isEmpty()) {
//				info.append(" - ");
//			}
//			info.append(track);
//		}
		if (info.toString().isEmpty()) {
			info.append(fileDescriptor.getFileName());
		}
		
		this.infoText.setText(info.toString());
		this.artistText.setText(artist);
		this.pathText.setText(fileDescriptor.getFile().getAbsolutePath());
		this.fileSizeText.setText(FileLabelUtil.getFileSizeLabel(fileDescriptor.getFile()));
	}
	
	@Override
	public void showDefaultLogo() {
		logoTooltip.hide();
		logoTooltip.deactivate();
		if (smallLogoImage != null) {
			smallLogoImage.dispose();
		}
		if (largeLogoImage != null) {
			largeLogoImage.dispose();
		}
		logoLabel.setImage(defaultLogoImage);
		this.infoText.setText(fileCollection.getFileDescriptors().size() + " files");
		this.artistText.setText("");
		this.pathText.setText(fileCollection.getOpeningInfo().getName());
		this.fileSizeText.setText("");
	}

	/**
	 * SaveRunnableWithProgress
	 */
	private class SaveRunnableWithProgress implements IRunnableWithProgress {
		private IWorkbenchWindow window;
		
		public SaveRunnableWithProgress(IWorkbenchWindow window) {
			this.window = window;
		}
		
		public void run(IProgressMonitor monitor) {
			ProgressMonitorAdapter progressMonitor = new ProgressMonitorAdapter(monitor);
			try {
				AudioPersistenceService persistenceService = AudioSolutions.getService(AudioPersistenceService.class);
				Set<FileDescriptor> result = persistenceService.persist(getFileCollection().getFileDescriptors(), progressMonitor);
				
				UIEvent event = new UIEvent(ActionType.AUDIO_SAVED, AudioCollectionEditor.this);
				event.getEditorDelta().getUpdateItems().addAll(result);
				event.getAudioDelta().getUpdateItems().addAll(result);
				EventSupport.INSTANCE.fireEvent(event);

				AudioCollectionEditor.this.setDirty(false);
			} 
			catch (final Exception exc) {
				logger.error("Audio Collection could not be saved", exc);
				window.getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openError(window.getShell(), "Save Error", "Audio Collection could not be saved: \n" + exc.getMessage());
					}
				});
			}
		}
	}
	
	public void refresh() {
		getSite().getShell().getDisplay().syncExec(() -> treeViewer.refresh());
	}

	@Override
	public void switchLayout(LayoutType layout) {
		JFaceExec.builder(getEditorSite().getShell())
			.worker(ctx -> layoutManager.saveState())
			.worker(ctx -> model.initLayout())
			.ui(ctx -> {
				ISelection selection = switchSelection(getFileDescriptorSelection(), layout);
				treeViewer.refresh();
				treeViewer.setSelection(selection, true);
			})
			.runProgressMonitorDialog(true, false);
	}
	
	/**
	 * Returns the selection for the new layout (tries to select as less nodes as possible)
	 */
	private ISelection switchSelection(FileDescriptorSelection selection, LayoutType layoutType) {
		List<AbstractTableTreeNode<?, ?>> nodes = new ArrayList<>();
		Set<FileDescriptor> filesInNodes = new HashSet<>();
		
		for (FileDescriptor fileDescriptor : selection.getFileDescriptors()) {
			// check if nodes already contains file descriptor (e.g. RelativePathTreeNode's children files) 
			if (filesInNodes.contains(fileDescriptor)) {
				continue;
			}
			
			// create file node
			FileDescriptorTreeNode node = new FileDescriptorTreeNode(fileDescriptor);
			
			// get parent node
			AbstractTableTreeNode<?, ?> parentNode = null;
			Object parent = contentProvider.getParent(node);
			if (parent instanceof RelativePathTreeNode) {
				parentNode = (RelativePathTreeNode) contentProvider.getParent(node);
			}
			else if (parent instanceof AlbumTreeNode) {
				parentNode = (AlbumTreeNode) contentProvider.getParent(node);
			}
			
			// if all children of parent node should be selected 
			if (parentNode != null && selection.getFileDescriptors().containsAll(parentNode.getFileDescriptors())) {
				nodes.add(parentNode);
				filesInNodes.addAll(parentNode.getFileDescriptors());
			}
			else {
				nodes.add(node);
				filesInNodes.add(node.getContent());
			}
		}
		return new StructuredSelection(nodes);
	}
}
