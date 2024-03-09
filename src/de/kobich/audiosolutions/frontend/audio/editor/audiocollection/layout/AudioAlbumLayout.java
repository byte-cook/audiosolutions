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

public class AudioAlbumLayout extends AbstractCollectionEditorLayout implements ICollectionEditorLayout {
	private final AudioCollectionEditor editor;
	private final AudioCollectionContentProvider contentProvider;
	private final AudioCollectionEditorLabelProvider labelProvider;
	private TreeViewer albumViewer;
	private Composite albumComposite;

	public AudioAlbumLayout(AudioCollectionEditor editor, AudioCollectionEditorLabelProvider labelProvider) {
		this.editor = editor;
		this.contentProvider = new AudioCollectionContentProvider(LayoutType.ALBUM);
		this.labelProvider = labelProvider;
	}

	@Override
	public void createLayout(Composite parent, FormToolkit toolkit, ViewerFilter filter) {
		albumComposite = toolkit.createComposite(parent, SWT.NONE);
		TreeColumnLayout treeColumnLayout = new TreeColumnLayout();
		albumComposite.setLayout(treeColumnLayout);
		albumComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Tree tree = toolkit.createTree(albumComposite, SWT.FULL_SELECTION | SWT.MULTI);
		albumViewer = new TreeViewer(tree);
		albumViewer.setContentProvider(contentProvider);
		albumViewer.setLabelProvider(labelProvider);
		albumViewer.setComparator(new AudioCollectionEditorComparator(AudioCollectionEditorColumn.FILE_NAME));
		albumViewer.addFilter(filter);

		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.addKeyListener(new TreeExpandKeyListener(albumViewer));

		List<String> columnNames = new ArrayList<String>();
		List<CellEditor> cellEditors = new ArrayList<CellEditor>();
		for (final AudioCollectionEditorColumn column : AudioCollectionEditorColumn.values()) {
			final TreeColumn treeColumn = new TreeColumn(tree, SWT.LEFT, column.getIndex());
			treeColumn.setText(column.getLabel());
			treeColumnLayout.setColumnData(treeColumn, new ColumnWeightData(column.getWidthPercent(), column.getWidth()));
			treeColumn.setMoveable(true);
			treeColumn.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Direction direction = ((AudioCollectionEditorComparator) albumViewer.getComparator()).setSortColumn(column);
					int dir = (Direction.ASCENDING.equals(direction)) ? SWT.UP : SWT.DOWN; 
					albumViewer.getTree().setSortDirection(dir);
					albumViewer.getTree().setSortColumn(treeColumn);
					albumViewer.refresh();
				}
			});
			columnNames.add(column.name());
			cellEditors.add(new TextCellEditor(tree));
		}

		// add editor support
		CellEditor[] editors = cellEditors.toArray(new CellEditor[0]); 
	    String[] columnProperties = columnNames.toArray(new String[0]); 
	    albumViewer.setColumnProperties(columnProperties);
	    albumViewer.setCellModifier(new AudioCollectionEditorCellModifier(editor));
	    albumViewer.setCellEditors(editors);

		// turn on the header and the lines
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
	}

	@Override
	public void setInput(ICollectionEditorModel input) {
		Object[] elements = albumViewer.getExpandedElements();
		ISelection selection = albumViewer.getSelection();
		albumViewer.setInput(input);
		albumViewer.setExpandedElements(elements);
		albumViewer.setSelection(selection, true);
	}
	
	@Override
	public ISelection createSelection(FileDescriptorSelection selection) {
		return super.createSelection(selection, contentProvider, LayoutType.ALBUM);
	}
	
	@Override
	public void updateLayout(LayoutDelta layoutDelta, FileDescriptorSelection oldSelection, boolean active) {
		super.updateTreeLayout(this.albumViewer, layoutDelta, LayoutType.ALBUM, oldSelection, active);
	}

	@Override
	public void refresh() {
		albumViewer.refresh();
	}

	@Override
	public void setFocus() {
		albumViewer.getTree().setFocus();
	}

	@Override
	public <T> T getViewerAdapter(Class<T> clazz) {
		if (clazz.isAssignableFrom(albumViewer.getClass())) {
			return clazz.cast(albumViewer);
		}
		else if (clazz.isAssignableFrom(albumViewer.getTree().getClass())) {
			return clazz.cast(albumViewer.getTree());
		}
		return null;
	}

	@Override
	public Composite getComposite() {
		return albumComposite;
	}

	@Override
	public void dispose() {
		albumComposite.dispose();
		albumViewer.getTree().dispose();
		this.contentProvider.dispose();
	}

}
