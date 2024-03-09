package de.kobich.audiosolutions.frontend.file.editor.filecollection;

import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import de.kobich.audiosolutions.core.service.info.FileInfo;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import de.kobich.audiosolutions.frontend.common.listener.AudioDelta;
import de.kobich.audiosolutions.frontend.common.listener.FileDelta;
import de.kobich.audiosolutions.frontend.common.ui.editor.AbstractFormEditor;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorFileMonitor;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorLayoutManager;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorUpdateManager;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorViewerFilter;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileOpeningInfo;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditorLayout;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutDelta;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutType;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.audiosolutions.frontend.common.util.FileLabelUtil;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.layout.FileFlatLayout;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.layout.FileHierarchicalLayout;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileCollectionModel;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.ui.FileCollectionEditorLabelProvider;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.component.file.FileDescriptor;

/**
 * Files editor.
 */
public class FileCollectionEditor extends AbstractFormEditor implements ICollectionEditor {
	public static final String ID = "de.kobich.audiosolutions.editor.fileCollectionEditor";
	private static final Point LOGO_SIZE = new Point(192, 128);
	private FileCollection fileCollection;
	private FileCollectionModel model;
	private FileCollectionEditorLabelProvider labelProvider;
	private CollectionEditorFileMonitor fileMonitor;
	private FileCollectionEditorEventListener eventListener;
	private CollectionEditorLayoutManager editorLayoutManager;
	private CollectionEditorUpdateManager editorUpdateManager;
	private StyledText infoText;
	private StyledText pathText;
	private StyledText fileSizeText;
	private CollectionEditorViewerFilter filter;
	private Text filterText;
	private Composite switchComposite;
	private StackLayout switchLayout;
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
			this.labelProvider = new FileCollectionEditorLabelProvider(true);
			this.eventListener = new FileCollectionEditorEventListener(this);
			this.eventListener.register();
			this.model = new FileCollectionModel(this.fileCollection);
			this.editorUpdateManager = new CollectionEditorUpdateManager(this, model);
			this.filter = new CollectionEditorViewerFilter();
			setPartName(fileCollection.getName());
			
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
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		if (smallLogoImage != null) {
			smallLogoImage.dispose();
		}
		if (largeLogoImage != null) {
			largeLogoImage.dispose();
		}
		this.editorLayoutManager.dispose();
		this.labelProvider.dispose();
		this.eventListener.deregister();
		this.switchComposite.dispose();
		if (this.fileMonitor != null) {
			this.fileMonitor.dispose();
		}
		this.filterText.dispose();
		this.infoText.dispose();
		this.pathText.dispose();
		this.fileSizeText.dispose();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor arg0) {}

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
		return false;
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
		defaultLogoImage = Activator.getDefault().getImage(ImageKey.FILE_COLLECTION_EDITOR);
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
		Label br = getFormToolkit().createLabel(informationGroup, null);
		br.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 1, 1));
		this.pathText = super.createStyledText(informationGroup, false);
		this.pathText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 1, 1));
		this.fileSizeText = super.createStyledText(informationGroup, false);
		this.fileSizeText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 1, 1));

//		Label br1 = getFormToolkit().createLabel(informationGroup, null);
//		br1.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 1, 1));

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
		editorLayoutManager.addLayout(LayoutType.FLAT, new FileFlatLayout(this, labelProvider));
		ICollectionEditorLayout hierarchicalLayout = new FileHierarchicalLayout(this, labelProvider);
		editorLayoutManager.addLayout(LayoutType.HIERARCHICAL, hierarchicalLayout);
		editorLayoutManager.addLayout(LayoutType.ALBUM, hierarchicalLayout);
		editorLayoutManager.addLayout(LayoutType.ARTIST, hierarchicalLayout);

		// composite
		switchComposite = toolkit.createComposite(parent);
		switchLayout = new StackLayout();
		switchComposite.setLayout(switchLayout);
		switchComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		editorLayoutManager.makeLayouts(switchComposite, this.model);
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
		Composite control = editorLayoutManager.switchLayout(LayoutType.getCurrentLayoutType(), this.fileCollection);
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
		FileDescriptor fileDescriptor = fileInfo.getFileDescriptor();
		String info = fileDescriptor.getFileName();
		
		this.infoText.setText(info);
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
		this.pathText.setText(fileCollection.getOpeningInfo().getName());
		this.fileSizeText.setText("");
	}
}
