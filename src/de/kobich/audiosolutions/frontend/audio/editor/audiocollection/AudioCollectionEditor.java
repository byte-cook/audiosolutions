package de.kobich.audiosolutions.frontend.audio.editor.audiocollection;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
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
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.layout.AudioAlbumLayout;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.layout.AudioArtistLayout;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.layout.AudioFlatLayout;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.layout.AudioHierarchicalLayout;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.AudioCollectionModel;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorLabelProvider;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.AudioDelta;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.FileDelta;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.ui.ProgressDialog;
import de.kobich.audiosolutions.frontend.common.ui.editor.AbstractFormEditor;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorFileMonitor;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorLayoutManager;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorUpdateManager;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorViewerFilter;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileOpeningInfo;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutDelta;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutType;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.audiosolutions.frontend.common.util.FileLabelUtil;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.progress.ProgressMonitorAdapter;
import de.kobich.component.file.FileDescriptor;

/**
 * Audio collection editor.
 */
public class AudioCollectionEditor extends AbstractFormEditor implements ICollectionEditor {
	public static final String ID = "de.kobich.audiosolutions.editor.audioCollectionEditor";
	private static final Logger logger = Logger.getLogger(AudioCollectionEditor.class);
	private static final Point LOGO_SIZE = new Point(192, 128);
	private FileCollection fileCollection;
	private AudioCollectionModel model;
	private AudioCollectionEditorLabelProvider labelProvider;
	private CollectionEditorFileMonitor fileMonitor;
	private AudioCollectionEditorEventListener eventListener;
	private CollectionEditorLayoutManager editorLayoutManager;
	private CollectionEditorUpdateManager editorUpdateManager;
	private StyledText infoText;
	private StyledText artistText;
	private StyledText pathText;
	private StyledText fileSizeText;
	private CollectionEditorViewerFilter filter;
	private Text filterText;
	private Composite switchComposite;
	private StackLayout switchLayout;
	private boolean dirty;
	private Label logoLabel;
	private ToolTip logoTooltip;
	private Image defaultLogoImage;
	private Image smallLogoImage;
	private Image largeLogoImage;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite editorSite, IEditorInput editorInput) throws PartInitException {
		setSite(editorSite);
		setInput(editorInput);

		if (editorInput instanceof FileCollection) {
			this.fileCollection = (FileCollection) editorInput;
			this.labelProvider = new AudioCollectionEditorLabelProvider(true);
			this.eventListener = new AudioCollectionEditorEventListener(this);
			this.eventListener.register();
			this.model = new AudioCollectionModel(this.fileCollection);
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
		this.editorLayoutManager.dispose();
		this.switchComposite.dispose();
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
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
		Composite informationGroup = super.createSection(body, "", 1, Section.TITLE_BAR | Section.NO_TITLE | Section.EXPANDED);

		this.infoText = super.createStyledText(informationGroup, false);//getFormToolkit().createLabel(informationGroup, "", SWT.NONE);
		this.infoText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 1, 1));
		Font font = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
		this.infoText.setFont(font);
		this.artistText = super.createStyledText(informationGroup, false);//getFormToolkit().createLabel(informationGroup, "", SWT.NONE);
		this.artistText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 1, 1));
		this.pathText = super.createStyledText(informationGroup, false);
		this.pathText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 1, 1));
		this.fileSizeText = super.createStyledText(informationGroup, false);
		this.fileSizeText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 1, 1));
		
//		Label br = getFormToolkit().createLabel(informationGroup, null);
//		br.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 1, 1));
		
		// filter
		filterText = new Text(informationGroup, SWT.BORDER | SWT.SEARCH);
		filterText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 1, 1));
		filterText.setMessage("Filter files");
		filterText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent evt) {
				filter.setSearchText("*" + filterText.getText() + "*");
				editorLayoutManager.refreshEditor();
			}
		});
		filterText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					filterText.setText("");
					filter.setSearchText("*");
					editorLayoutManager.refreshEditor();
				}
			}
		});
	}
	
	protected void makeContent(Composite parent) {
		FormToolkit toolkit = super.getFormToolkit();
		
		// layout manager
		editorLayoutManager = new CollectionEditorLayoutManager(this, toolkit, filter);
		editorLayoutManager.addLayout(LayoutType.FLAT, new AudioFlatLayout(this, labelProvider));
		editorLayoutManager.addLayout(LayoutType.HIERARCHICAL, new AudioHierarchicalLayout(this, labelProvider));
		editorLayoutManager.addLayout(LayoutType.ALBUM, new AudioAlbumLayout(this, labelProvider));
		editorLayoutManager.addLayout(LayoutType.ARTIST, new AudioArtistLayout(this, labelProvider));

		// composite
		switchComposite = toolkit.createComposite(parent);
		switchLayout = new StackLayout();
		switchComposite.setLayout(switchLayout);
		switchComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		editorLayoutManager.makeLayouts(switchComposite, model);
		switchLayout();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		editorLayoutManager.getActiveLayout().setFocus();
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
					editorLayoutManager.updateLayout(layoutDelta, oldSelection);
					switchComposite.layout();
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
					editorLayoutManager.updateLayout(layoutDelta, oldSelection);
					switchComposite.layout();
				}
			});
		}
	}

	/**
	 * Switches the layout
	 */
	public void switchLayout() {
		Control control = editorLayoutManager.switchLayout(LayoutType.getCurrentLayoutType(), this.fileCollection);
		switchLayout.topControl = control;
		switchComposite.layout();
	}
	
	/**
	 * @return the fileCollection
	 */
	public FileCollection getFileCollection() {
		return fileCollection;
	}
	
	@Override
	public FileDescriptorSelection getFileDescriptorSelection() {
		return editorLayoutManager.getSelectedFiles();
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
	
}
