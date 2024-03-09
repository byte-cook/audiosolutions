package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.layout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionContentProvider;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorCellModifier;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorColumn;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorComparator;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorComparator.Direction;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui.AudioCollectionEditorLabelProvider;
import de.kobich.audiosolutions.frontend.common.ui.editor.AbstractCollectionEditorLayout;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditorLayout;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditorModel;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutDelta;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutType;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.commons.ui.jface.listener.TreeExpandKeyListener;

public class AudioHierarchicalLayout extends AbstractCollectionEditorLayout implements ICollectionEditorLayout {
	private final AudioCollectionEditor editor;
	private final AudioCollectionContentProvider contentProvider;
	private final AudioCollectionEditorLabelProvider labelProvider;
	private TreeViewer treeViewer;
	private Composite treeComposite;

	public AudioHierarchicalLayout(AudioCollectionEditor editor, AudioCollectionEditorLabelProvider labelProvider) {
		this.editor = editor;
		this.contentProvider = new AudioCollectionContentProvider(LayoutType.HIERARCHICAL);
		this.labelProvider = labelProvider;
	}

	@Override
	public void createLayout(Composite parent, FormToolkit toolkit, ViewerFilter filter) {
		treeComposite = toolkit.createComposite(parent, SWT.NONE);
		TreeColumnLayout treeColumnLayout = new TreeColumnLayout();
		treeComposite.setLayout(treeColumnLayout);
		treeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Tree tree = toolkit.createTree(treeComposite, SWT.FULL_SELECTION | SWT.MULTI);
		treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setComparator(new AudioCollectionEditorComparator(AudioCollectionEditorColumn.FILE_NAME));
		treeViewer.addFilter(filter);

		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.addKeyListener(new TreeExpandKeyListener(treeViewer));

		List<String> columnNames = new ArrayList<String>();
		List<CellEditor> cellEditors = new ArrayList<CellEditor>();
		for (final AudioCollectionEditorColumn column : AudioCollectionEditorColumn.values()) {
			final TreeColumn treeColumn = new TreeColumn(tree, SWT.LEFT, column.getIndex());
			treeColumn.setText(column.getLabel());
			treeColumnLayout.setColumnData(treeColumn, new ColumnWeightData(column.getWidthPercent(), column.getWidth()));
			treeColumn.setMoveable(true);
			treeColumn.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Direction direction = ((AudioCollectionEditorComparator) treeViewer.getComparator()).setSortColumn(column);
					int dir = (Direction.ASCENDING.equals(direction)) ? SWT.UP : SWT.DOWN; 
					treeViewer.getTree().setSortDirection(dir);
					treeViewer.getTree().setSortColumn(treeColumn);
					treeViewer.refresh();
				}
			});
			columnNames.add(column.name());
			cellEditors.add(new TextCellEditor(tree));
		}

		// add editor support
		CellEditor[] editors = cellEditors.toArray(new CellEditor[0]); 
	    String[] columnProperties = columnNames.toArray(new String[0]); 
	    treeViewer.setColumnProperties(columnProperties);
	    treeViewer.setCellModifier(new AudioCollectionEditorCellModifier(editor));
		treeViewer.setCellEditors(editors);

		// turn on the header and the lines
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
	}

	@Override
	public void setInput(ICollectionEditorModel input) {
		Object[] elements = treeViewer.getExpandedElements();
		ISelection selection = treeViewer.getSelection();
		treeViewer.setInput(input);
		treeViewer.setExpandedElements(elements);
		treeViewer.setSelection(selection, true);
	}
	
	@Override
	public ISelection createSelection(FileDescriptorSelection selection) {
		return super.createSelection(selection, contentProvider, LayoutType.HIERARCHICAL);
	}
	
	@Override
	public void updateLayout(LayoutDelta layoutDelta, FileDescriptorSelection oldSelection, boolean active) {
		super.updateTreeLayout(this.treeViewer, layoutDelta, LayoutType.HIERARCHICAL, oldSelection, active);
	}

	@Override
	public void refresh() {
		treeViewer.refresh();
	}

	@Override
	public void setFocus() {
		treeViewer.getTree().setFocus();
	}

	@Override
	public <T> T getViewerAdapter(Class<T> clazz) {
		if (clazz.isAssignableFrom(treeViewer.getClass())) {
			return clazz.cast(treeViewer);
		}
		else if (clazz.isAssignableFrom(treeViewer.getTree().getClass())) {
			return clazz.cast(treeViewer.getTree());
		}
		return null;
	}

	@Override
	public Composite getComposite() {
		return treeComposite;
	}

	@Override
	public void dispose() {
		treeComposite.dispose();
		treeViewer.getTree().dispose();
		this.contentProvider.dispose();
	}

}
