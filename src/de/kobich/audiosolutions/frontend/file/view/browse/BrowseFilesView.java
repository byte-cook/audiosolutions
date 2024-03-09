package de.kobich.audiosolutions.frontend.file.view.browse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;

import de.kobich.audiosolutions.frontend.file.view.browse.action.ShowFilesAction;
import de.kobich.audiosolutions.frontend.file.view.browse.model.DirFirstFileTreeNodeComparator;
import de.kobich.audiosolutions.frontend.file.view.browse.model.FileTreeNode;
import de.kobich.audiosolutions.frontend.file.view.browse.ui.BrowseFilesContentProvider;
import de.kobich.audiosolutions.frontend.file.view.browse.ui.BrowseFilesLabelProvider;
import de.kobich.audiosolutions.frontend.file.view.browse.ui.BrowseFilesViewerFilter;
import de.kobich.audiosolutions.frontend.file.view.browse.ui.ExpandedFilesTreeListener;
import de.kobich.audiosolutions.frontend.file.view.browse.ui.OpenFileSelectionListener;
import de.kobich.commons.ui.jface.ComboUtils;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.listener.TreeExpandKeyListener;
import de.kobich.commons.ui.jface.memento.FileListComboSerializer;
import de.kobich.commons.ui.jface.memento.FileListComboSerializer.FileType;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;

/**
 * Browser view.
 */
public class BrowseFilesView extends ViewPart implements IMementoItemSerializable {
	private static final Logger logger = Logger.getLogger(BrowseFilesView.class);
	public static final String ID = "de.kobich.audiosolutions.view.file.browseFilesView";
	private static final String STATE_ROOT_DIR = "rootDir";
	private static final String DEFAULT_DIR = System.getProperty("user.dir");
	private File rootDirectory;
	private boolean filesVisible;
	private Combo rootDirectoryCombo;
	private Text filterText;
	private TreeViewer treeViewer;
	private Comparator<FileTreeNode> comparator;
	private ExpandedFilesTreeListener expandedFilesTreeListener;
	private OpenFileSelectionListener selectionListener;
	private IMemento memento;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;
		this.comparator = new DirFirstFileTreeNodeComparator();
		ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);
		Command showCommand = commandService.getCommand(ShowFilesAction.ID);
		State showState = showCommand.getState(ShowFilesAction.STATE_ID);
		Boolean showValue = Boolean.FALSE;
		if (showState != null) {
			showValue = (Boolean) showState.getValue();
		}
		this.filesVisible = showValue.booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(JFaceUtils.createViewGridLayout(1, false, JFaceUtils.MARGIN_TOP));

		// root directory
		Composite filterComposite = new Composite(parent, SWT.NONE);
		filterComposite.setLayout(JFaceUtils.createViewGridLayout(2, false, JFaceUtils.MARGIN_WIDTH));
		filterComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label dirLabel = new Label(filterComposite, SWT.NONE);
		dirLabel.setText("Current Directory:");
		rootDirectoryCombo = new Combo(filterComposite, SWT.BORDER);
		rootDirectoryCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rootDirectoryCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				File directory = new File(rootDirectoryCombo.getText());
				if (directory.exists()) {
					setRootDirectory(directory);
				}
			}
		});
		rootDirectoryCombo.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.CR) {
					File directory = new File(rootDirectoryCombo.getText());
					if (directory.exists()) {
						setRootDirectory(directory);
					}
				}
			}
		});

		// filter
		final BrowseFilesViewerFilter filter = new BrowseFilesViewerFilter();
		filterText = new Text(filterComposite, SWT.BORDER | SWT.SEARCH);
		filterText.setEnabled(filesVisible);
		GridData filterGD = new GridData(GridData.FILL_HORIZONTAL);
		filterGD.horizontalSpan = 2;
		filterText.setLayoutData(filterGD);
		filterText.setMessage("Filter files");
		filterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String wildcardMatcher = filterText.getText() + "*";
				filter.setWildcardMatcher(wildcardMatcher);
				treeViewer.refresh();
			}
		});
		filterText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					filterText.setText("");
					filter.setWildcardMatcher("*");
					treeViewer.refresh();
				}
			}
		});

		// file tree
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE);
		treeViewer.setContentProvider(new BrowseFilesContentProvider(this));
		treeViewer.setLabelProvider(new BrowseFilesLabelProvider(this));
		treeViewer.addFilter(filter);

		Tree tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		expandedFilesTreeListener = new ExpandedFilesTreeListener(this);
		tree.addTreeListener(expandedFilesTreeListener);
		selectionListener = new OpenFileSelectionListener(this);
		tree.addSelectionListener(selectionListener);
		tree.addKeyListener(new TreeExpandKeyListener(treeViewer));

		// rename listeners
		// tree.addListener(SWT.MouseDown, new FileRenameListener(this));
		// tree.addKeyListener(new FileTreeKeyListener(this));

		// restore state
		restoreState();
		fireRootDirectoryChanged(rootDirectory);

		// enable context menu
		MenuManager menuManager = new MenuManager();
		Menu menuFlat = menuManager.createContextMenu(tree);
		tree.setMenu(menuFlat);
		getSite().registerContextMenu(menuManager, treeViewer);

		// register for events
		getViewSite().setSelectionProvider(treeViewer);
	}

	public void setFocus() {
		this.rootDirectoryCombo.setFocus();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		this.rootDirectoryCombo.dispose();
		this.filterText.dispose();
		this.treeViewer.getTree().dispose();
		super.dispose();
	}

	/**
	 * Called if files are selected
	 */
	protected void fireRootDirectoryChanged(File directory) {
		// validates the root first
		if ((!directory.isDirectory()) || (!directory.exists())) {
			directory = new File(DEFAULT_DIR);
		}

		this.rootDirectory = directory;
		rootDirectoryCombo.setText(directory.getAbsolutePath());
		ComboUtils.addTextToCombo(rootDirectoryCombo.getText(), rootDirectoryCombo);

		refreshView();
	}

	/**
	 * @return the selected file
	 */
	public File getSelectedFile() {
		File directory = null;
		if (treeViewer.getTree().getSelectionCount() > 0) {
			TreeItem[] selection = treeViewer.getTree().getSelection();
			Object data = selection[0].getData();
			if (data instanceof FileTreeNode) {
				FileTreeNode fileTreeNode = (FileTreeNode) data;
				directory = (File) fileTreeNode.getContent();
			}
		}
		else {
			directory = getRootDirectory();
		}
		return directory;
	}

	/**
	 * @return the rootDirectory
	 */
	public File getRootDirectory() {
		return rootDirectory;
	}

	/**
	 * @param rootDirectory the rootDirectory to set
	 */
	public void setRootDirectory(File rootDirectory) {
		if (!this.rootDirectory.equals(rootDirectory)) {
			fireRootDirectoryChanged(rootDirectory);
		}
	}

	/**
	 * @return the filesVisible
	 */
	public boolean isFilesVisible() {
		return filesVisible;
	}

	/**
	 * @param filesVisible the filesVisible to set
	 */
	public void setFilesVisible(boolean filesVisible) {
		this.filesVisible = filesVisible;
		this.filterText.setEnabled(filesVisible);
	}

	/**
	 * @return the comparator
	 */
	public Comparator<FileTreeNode> getComparator() {
		return comparator;
	}

	/**
	 * Refreshes the view
	 */
	public void refreshView() {
		// reset content
		treeViewer.getTree().removeAll();
		treeViewer.setInput(rootDirectory);
		// expand previous files
		Collection<FileTreeNode> expandedFiles = expandedFilesTreeListener.getExpandedElements();
		if (logger.isDebugEnabled()) {
			for (FileTreeNode expandedFile : expandedFiles) {
				logger.debug("Expanded file: " + expandedFile.getContent());
			}
		}
		treeViewer.setExpandedElements(expandedFiles.toArray());
		// select previous file
		FileTreeNode selectedFileTreeNode = selectionListener.getSelectedFile();
		if (selectedFileTreeNode != null) {
			List<FileTreeNode> segments = new ArrayList<FileTreeNode>();
			File selectedFile = selectedFileTreeNode.getContent();
			File parentFile = selectedFile;
			while (parentFile != null && !parentFile.equals(rootDirectory)) {
				logger.debug("Selected parent file: " + parentFile);
				segments.add(new FileTreeNode(parentFile));
				parentFile = parentFile.getParentFile();
			}
			Collections.reverse(segments);
			TreePath selectedPath = new TreePath(segments.toArray());
			TreeSelection selection = new TreeSelection(selectedPath);
			treeViewer.setSelection(selection, true);
		}
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
		IMementoItem mementoItem = MementoUtils.getMementoItemToSave(memento, BrowseFilesView.class.getName());
		if (mementoItem == null) {
			return;
		}
		FileListComboSerializer directoryMemento = new FileListComboSerializer(STATE_ROOT_DIR, DEFAULT_DIR, true, FileType.DIRECTORY);
		directoryMemento.save(rootDirectoryCombo, mementoItem);

	}

	@Override
	public void restoreState() {
		IMementoItem mementoItem = MementoUtils.getMementoItemToRestore(memento, BrowseFilesView.class.getName());
		if (mementoItem == null) {
			rootDirectory = new File(DEFAULT_DIR);
			return;
		}
		FileListComboSerializer directoryMemento = new FileListComboSerializer(STATE_ROOT_DIR, DEFAULT_DIR, true, FileType.DIRECTORY);
		directoryMemento.restore(rootDirectoryCombo, mementoItem);
		rootDirectory = new File(rootDirectoryCombo.getText());
	}

	/**
	 * Expand element
	 * @param element
	 */
	public void expandElement(Object element) {
		treeViewer.expandToLevel(element, 1);
		treeViewer.update(element, null);
		expandedFilesTreeListener.expandElement(element);
	}

	/**
	 * Collapse element
	 * @param element
	 */
	public void collapseElement(Object element) {
		treeViewer.collapseToLevel(element, 1);
		treeViewer.update(element, null);
		expandedFilesTreeListener.collapseElement(element);
	}

	// /**
	// * BrowseFileViewStateSupport
	// */
	// private class BrowseFileViewStateSupport extends ViewStateSupport {
	// private static final String ROOT_DIR = "rootDir";
	//
	// @Override
	// protected void restoreViewState(IMemento memento) {
	// rootDirectory = new File(DEFAULT_DIR);
	// if (memento != null) {
	// String[] lastPaths = getUtility().restoreStateArray(ROOT_DIR, DEFAULT_DIR);
	// File rootDir = null;
	// for (String path : lastPaths) {
	// File directory = new File(path);
	// if (directory.exists()) {
	// if (rootDir == null) {
	// rootDir = directory;
	// rootDirectoryCombo.setText(rootDir.getPath());
	// }
	// rootDirectoryCombo.add(directory.getPath());
	// }
	// }
	//
	// if (rootDir != null) {
	// rootDirectory = rootDir;
	// }
	// }
	// }
	//
	// @Override
	// protected void saveViewState(IMemento memento) {
	// getUtility().saveComboState(ROOT_DIR, rootDirectoryCombo);
	// }
	// }
}
