package de.kobich.audiosolutions.frontend.common.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;

public class FileQueryDialog extends MessageDialog {
	private final Set<FileDescriptor> files;
	private TableViewer tableViewer;
	
	public static FileQueryDialog createYesNoDialog(Shell parentShell, String title, String message, Set<FileDescriptor> files) {
		FileQueryDialog dialog = new FileQueryDialog(parentShell, title, message, files, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL });
		return dialog;
	}

	private FileQueryDialog(Shell parentShell, String dialogTitle, String message, Set<FileDescriptor> files, int dialogImageType, String[] dialogButtonLabels) {
		super(parentShell, dialogTitle, null, null, dialogImageType, dialogButtonLabels, 0);
		super.setShellStyle(getShellStyle() | SWT.RESIZE);
		super.message = message;
		this.files = files;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createCustomArea(Composite parent) {
		if (files.isEmpty()) 
			return null;
		
		Label selectedLabel = new Label(parent, SWT.NONE);
		selectedLabel.setText(files.size() + " file(s) selected:");
		
		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridData tableGridData = new GridData(GridData.FILL_BOTH);
		tableComposite.setLayoutData(tableGridData);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);

		tableViewer = new TableViewer(tableComposite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(false);
		table.setLinesVisible(true);
		// column
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, 0);
		tableColumnLayout.setColumnData(viewerColumn.getColumn(), new ColumnWeightData(100));
		viewerColumn.getColumn().setText("File");
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				FileDescriptor fileDescriptor = (FileDescriptor) element;
				return fileDescriptor.getRelativePath();
			}
		});
		
		// input
		List<FileDescriptor> fileList = new ArrayList<FileDescriptor>(this.files);
		Collections.sort(fileList, new DefaultFileDescriptorComparator());
		tableViewer.setInput(fileList);
		
		return parent;
	}
	


}
