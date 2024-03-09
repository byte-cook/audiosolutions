package de.kobich.audiosolutions.frontend.file.editor.filecollection.layout;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.kobich.audiosolutions.frontend.common.ui.editor.AbstractCollectionEditorLayout;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditorLayout;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditorModel;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutDelta;
import de.kobich.audiosolutions.frontend.common.ui.editor.LayoutType;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.FileCollectionEditor;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.ui.FileCollectionContentProvider;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.ui.FileCollectionEditorColumn;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.ui.FileCollectionEditorComparator;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.ui.FileCollectionEditorLabelProvider;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.ui.FileCollectionEditorComparator.Direction;

public class FileFlatLayout extends AbstractCollectionEditorLayout implements ICollectionEditorLayout {
	private final FileCollectionContentProvider contentProvider;
	private final FileCollectionEditorLabelProvider labelProvider;
	private TableViewer tableViewer;
	private Composite tableComposite;

	public FileFlatLayout(FileCollectionEditor editor, FileCollectionEditorLabelProvider labelProvider) {
		this.contentProvider = new FileCollectionContentProvider(LayoutType.FLAT);
		this.labelProvider = labelProvider;
	}

	@Override
	public void createLayout(Composite parent, FormToolkit toolkit, ViewerFilter filter) {
		tableComposite = toolkit.createComposite(parent);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Table table = toolkit.createTable(tableComposite, SWT.FULL_SELECTION | SWT.MULTI);
		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(contentProvider);
		tableViewer.setLabelProvider(labelProvider);
		tableViewer.setComparator(new FileCollectionEditorComparator(FileCollectionEditorColumn.FILE_NAME));
		tableViewer.addFilter(filter);
		
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		for (final FileCollectionEditorColumn column : FileCollectionEditorColumn.values()) {
			final TableColumn tableColumn = new TableColumn(table, SWT.LEFT, column.getIndex());
			tableColumn.setText(column.getLabel());
			tableColumnLayout.setColumnData(tableColumn, new ColumnWeightData(column.getWidthPercent(), column.getWidth()));
			tableColumn.setMoveable(true);
			tableColumn.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Direction direction = ((FileCollectionEditorComparator) tableViewer.getComparator()).setSortColumn(column);
					int dir = (Direction.ASCENDING.equals(direction)) ? SWT.UP : SWT.DOWN; 
					tableViewer.getTable().setSortDirection(dir);
					tableViewer.getTable().setSortColumn(tableColumn);
					tableViewer.refresh();
				}
			});
		}

		// turn on the header and the lines
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	@Override
	public void setInput(ICollectionEditorModel input) {
		ISelection selection = tableViewer.getSelection();
		tableViewer.setInput(input);
		tableViewer.setSelection(selection, true);
	}
	
	@Override
	public ISelection createSelection(FileDescriptorSelection selection) {
		return super.createSelection(selection, contentProvider, LayoutType.FLAT);
	}
	
	@Override
	public void updateLayout(LayoutDelta layoutDelta, FileDescriptorSelection oldSelection, boolean active) {
		super.updateTableLayout(this.tableViewer, layoutDelta, LayoutType.FLAT, oldSelection, active);
	}

	@Override
	public void refresh() {
		tableViewer.refresh();
	}

	@Override
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	@Override
	public <T> T getViewerAdapter(Class<T> clazz) {
		if (clazz.isAssignableFrom(tableViewer.getClass())) {
			return clazz.cast(tableViewer);
		}
		else if (clazz.isAssignableFrom(tableViewer.getTable().getClass())) {
			return clazz.cast(tableViewer.getTable());
		}
		return null;
	}

	@Override
	public Composite getComposite() {
		return tableComposite;
	}

	@Override
	public void dispose() {
		this.tableComposite.dispose();
		this.tableViewer.getTable().dispose();
		this.contentProvider.dispose();
	}

}
