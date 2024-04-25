package de.kobich.audiosolutions.frontend.file.view.rename;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.descriptor.RenameAttributeProvider;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.audiosolutions.frontend.file.view.rename.model.FileModel;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenameFilesAttributeTabView;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenameFilesAutoNumberingTabView;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenameFilesBaseTabView;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenameFilesCaseTabView;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenameFilesCuttingTabView;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenameFilesFillTabView;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenameFilesInsertingTabView;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenameFilesPreviewComparator;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenameFilesPreviewListener;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenameFilesReplacingTabView;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenameFilesSelectionTabView;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenameFilesSwappingTabView;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenamingFilesPreviewContentProvider;
import de.kobich.audiosolutions.frontend.file.view.rename.ui.RenamingFilesPreviewLabelProvider;
import de.kobich.commons.misc.rename.rule.IRenameRule;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.memento.SashFormSerializer;
import de.kobich.commons.ui.jface.table.ViewerColumn;
import de.kobich.commons.ui.jface.table.ViewerColumnManager;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;
import de.kobich.component.file.descriptor.IRenameAttributeProvider;

public class RenameFilesView extends ViewPart implements IMementoItemSerializable {
	public static final String ID = "de.kobich.audiosolutions.view.file.renameFilesView";
	public static ViewerColumn COLUMN_FILE_NAME = new ViewerColumn("File Name", 50);
	public static ViewerColumn COLUMN_NEW_NAME = new ViewerColumn("Preview", 50);
	public static ViewerColumnManager COLUMNS = new ViewerColumnManager(COLUMN_FILE_NAME, COLUMN_NEW_NAME);
	private static final String STATE_SASH_WEIGHT = "sashWeight";
	private SashForm sashForm;
	private TabFolder tabFolder;
	private RenameFilesBaseTabView[] tabViews;
	private FileModel fileModel;
	private Composite previewGroup;
	private TableViewer previewList;
	private RenameFilesViewEventListener eventListener;
	private RenameFilesPreviewListener previewListener;
	private IMemento memento;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.fileModel = new FileModel();
		this.memento = memento;
		this.previewListener = new RenameFilesPreviewListener(this);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(JFaceUtils.createViewGridLayout(1, false));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// sash
		sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// preview
		previewGroup = new Composite(sashForm, SWT.NONE);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		previewGroup.setLayout(tableColumnLayout);
		previewGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		previewList = new TableViewer(previewGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		previewList.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		previewList.getTable().setLinesVisible(true);
		previewList.getTable().setHeaderVisible(true);
		for (final ViewerColumn column : COLUMNS) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(previewList, SWT.NONE, COLUMNS.indexOf(column));
			tableColumnLayout.setColumnData(viewerColumn.getColumn(), new ColumnWeightData(column.getWidthPercent(), column.getMinimumWidth()));
			viewerColumn.getColumn().setText(column.getName());
		}
		previewList.setContentProvider(new RenamingFilesPreviewContentProvider());
		previewList.setLabelProvider(new RenamingFilesPreviewLabelProvider());
		previewList.setComparator(new RenameFilesPreviewComparator());

		// tabs for renaming type
		tabFolder = new TabFolder(sashForm, SWT.NONE);
		tabFolder.setLayout(JFaceUtils.createViewGridLayout(1, false, JFaceUtils.MARGIN_WIDTH, JFaceUtils.MARGIN_HEIGHT));
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		tabViews = new RenameFilesBaseTabView[9];
		tabViews[0] = new RenameFilesSelectionTabView(previewListener);
		tabViews[1] = new RenameFilesCuttingTabView(previewListener);
		tabViews[2] = new RenameFilesReplacingTabView(previewListener);
		tabViews[3] = new RenameFilesInsertingTabView(previewListener);
		tabViews[4] = new RenameFilesFillTabView(previewListener);
		tabViews[5] = new RenameFilesAttributeTabView(previewListener);
		tabViews[6] = new RenameFilesAutoNumberingTabView(previewListener);
		tabViews[7] = new RenameFilesCaseTabView(previewListener);
		tabViews[8] = new RenameFilesSwappingTabView(previewListener);
		
		for (int i = 0; i < tabViews.length; ++ i) {
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			String title = i + ". " + tabViews[i].getTitle();
			tabItem.setText(title);
			tabViews[i].setTabItem(tabItem);
			tabItem.setControl(tabViews[i].getTabControl(tabFolder));
		}
		
		fireDeselection();
		restoreState();
		
		// register for events
		eventListener = new RenameFilesViewEventListener(this, previewListener);
		eventListener.register();

		// init model
		JFaceExec.builder(getSite().getShell())
			.worker(ctx -> {
				IFileID3TagService id3TagService = AudioSolutions.getService(IFileID3TagService.JAUDIO_TAGGER, IFileID3TagService.class);
				IRenameAttributeProvider attributeProvider = new RenameAttributeProvider(id3TagService);
				fileModel.setAttributeProvider(attributeProvider);
			})
			.runBackgroundJob(100, false, true, null);
	}

	@Override
	public void setFocus() {
		this.previewGroup.setFocus();
	}

	@Override
	public void dispose() {
		for (RenameFilesBaseTabView tabView : tabViews) {
			tabView.dispose();
		}
		this.eventListener.deregister();
		this.previewGroup.dispose();
		this.tabFolder.dispose();
		super.dispose();
	}

	/**
	 * Called if one or more files are selected
	 */
	public void fireSelection() {
		setContentDescription(fileModel.size() + " file(s) selected");
		sashForm.setVisible(true);
		previewList.setInput(fileModel);
		
		previewListener.updatePreview();
	}

	/**
	 * Called if no files are selected
	 */
	public void fireDeselection() {
		setContentDescription("Please select one or more files.");
		sashForm.setVisible(false);
		fileModel.clear();

//		// fire event
//		ISourceProviderService sourceProviderService = (ISourceProviderService) getSite().getWorkbenchWindow().getService(ISourceProviderService.class);
//		RenameFilesViewSourceProvider p = (RenameFilesViewSourceProvider) sourceProviderService.getSourceProvider(RenameFilesViewSourceProvider.RENAME_TAB_ENABLED_STATE);
//		p.changeState(RenameFilesViewSourceProvider.RENAME_FILE_STATE, Boolean.FALSE);
//		p.changeState(RenameFilesViewSourceProvider.RENAME_PREVIEW_STATE, Boolean.FALSE);
//		p.changeState(RenameFilesViewSourceProvider.RENAME_TAB_ENABLED_STATE, Boolean.FALSE);
//		for (RenameFilesBaseTabView tabView : tabViews) {
//			tabView.reset();
//			tabView.setEnabled(false);
//		}
	}
	
	/**
	 * Returns the rename rules
	 * @return
	 */
	public List<IRenameRule> getRenameRules() {
		List<IRenameRule> renameFilters = new ArrayList<IRenameRule>();
		for (RenameFilesBaseTabView tabView : tabViews) {
			if (tabView.isEnabled()) {
				IRenameRule[] filters = tabView.getRenameRules();
				for (IRenameRule filter : filters) {
					renameFilters.add(filter);
				}
			}
		}
		return renameFilters;
	}
	
	/**
	 * Resets this view
	 */
	public void reset() {
		for (RenameFilesBaseTabView tabView : tabViews) {
			tabView.setEnabled(false);
			tabView.reset();
		}
		fileModel.reset();
	}

	/**
	 * @return the fileModel
	 */
	public FileModel getFileModel() {
		return fileModel;
	}

	/**
	 * Refreshes the preview
	 */
	public void refreshPreview() {
		previewList.refresh();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		this.memento = memento;
		saveState();
	}
	
	@Override
	public void saveState() {
		IMementoItem mementoItem = MementoUtils.getMementoItemToSave(memento, RenameFilesView.class.getName());
		if (mementoItem == null) {
			return;
		}
		SashFormSerializer sashFormMemento = new SashFormSerializer(STATE_SASH_WEIGHT, new int[]{1, 2});
		sashFormMemento.save(sashForm, mementoItem);
	}
	
	@Override
	public void restoreState() {
		IMementoItem mementoItem = MementoUtils.getMementoItemToRestore(memento, RenameFilesView.class.getName());
		if (mementoItem == null) {
			sashForm.setWeights(new int[]{1, 2});
			return;
		}
		SashFormSerializer sashFormMemento = new SashFormSerializer(STATE_SASH_WEIGHT, new int[]{1, 2});
		sashFormMemento.restore(sashForm, mementoItem);
	}
}
