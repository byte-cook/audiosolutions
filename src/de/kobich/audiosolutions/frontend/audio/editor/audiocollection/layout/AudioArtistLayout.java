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

public class AudioArtistLayout extends AbstractCollectionEditorLayout implements ICollectionEditorLayout {
	private final AudioCollectionEditor editor;
	private final AudioCollectionContentProvider contentProvider;
	private final AudioCollectionEditorLabelProvider labelProvider;
	private TreeViewer artistViewer;
	private Composite artistComposite;

	public AudioArtistLayout(AudioCollectionEditor editor, AudioCollectionEditorLabelProvider labelProvider) {
		this.editor = editor;
		this.contentProvider = new AudioCollectionContentProvider(LayoutType.ARTIST);
		this.labelProvider = labelProvider;
	}

	@Override
	public void createLayout(Composite parent, FormToolkit toolkit, ViewerFilter filter) {
		artistComposite = toolkit.createComposite(parent, SWT.NONE);
		TreeColumnLayout treeColumnLayout = new TreeColumnLayout();
		artistComposite.setLayout(treeColumnLayout);
		artistComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Tree tree = toolkit.createTree(artistComposite, SWT.FULL_SELECTION | SWT.MULTI);
		artistViewer = new TreeViewer(tree);
		artistViewer.setContentProvider(contentProvider);
		artistViewer.setLabelProvider(labelProvider);
		artistViewer.setComparator(new AudioCollectionEditorComparator(AudioCollectionEditorColumn.FILE_NAME));
		artistViewer.addFilter(filter);

		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.addKeyListener(new TreeExpandKeyListener(artistViewer));

		List<String> columnNames = new ArrayList<String>();
		List<CellEditor> cellEditors = new ArrayList<CellEditor>();
		for (final AudioCollectionEditorColumn column : AudioCollectionEditorColumn.values()) {
			final TreeColumn treeColumn = new TreeColumn(tree, SWT.LEFT, column.getIndex());
			treeColumn.setText(column.getLabel());
			treeColumnLayout.setColumnData(treeColumn, new ColumnWeightData(column.getWidthPercent(), column.getWidth()));
			treeColumn.setMoveable(true);
			treeColumn.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Direction direction = ((AudioCollectionEditorComparator) artistViewer.getComparator()).setSortColumn(column);
					int dir = (Direction.ASCENDING.equals(direction)) ? SWT.UP : SWT.DOWN; 
					artistViewer.getTree().setSortDirection(dir);
					artistViewer.getTree().setSortColumn(treeColumn);
					artistViewer.refresh();
				}
			});
			columnNames.add(column.name());
			cellEditors.add(new TextCellEditor(tree));
		}

		// add editor support
		CellEditor[] editors = cellEditors.toArray(new CellEditor[0]); 
	    String[] columnProperties = columnNames.toArray(new String[0]); 
	    artistViewer.setColumnProperties(columnProperties);
	    artistViewer.setCellModifier(new AudioCollectionEditorCellModifier(editor));
	    artistViewer.setCellEditors(editors);

		// turn on the header and the lines
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
	}

	@Override
	public void setInput(ICollectionEditorModel input) {
		Object[] elements = artistViewer.getExpandedElements();
		ISelection selection = artistViewer.getSelection();
		artistViewer.setInput(input);
		artistViewer.setExpandedElements(elements);
		artistViewer.setSelection(selection, true);
	}
	
	@Override
	public ISelection createSelection(FileDescriptorSelection selection) {
		return super.createSelection(selection, contentProvider, LayoutType.ARTIST);
	}
	
	@Override
	public void updateLayout(LayoutDelta layoutDelta, FileDescriptorSelection oldSelection, boolean active) {
		super.updateTreeLayout(this.artistViewer, layoutDelta, LayoutType.ARTIST, oldSelection, active);
	}

	@Override
	public void refresh() {
		artistViewer.refresh();
	}

	@Override
	public void setFocus() {
		artistViewer.getTree().setFocus();
	}

	@Override
	public <T> T getViewerAdapter(Class<T> clazz) {
		if (clazz.isAssignableFrom(artistViewer.getClass())) {
			return clazz.cast(artistViewer);
		}
		else if (clazz.isAssignableFrom(artistViewer.getTree().getClass())) {
			return clazz.cast(artistViewer.getTree());
		}
		return null;
	}

	@Override
	public Composite getComposite() {
		return artistComposite;
	}

	@Override
	public void dispose() {
		artistComposite.dispose();
		artistViewer.getTree().dispose();
		this.contentProvider.dispose();
	}

}
