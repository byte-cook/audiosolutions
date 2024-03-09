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

import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;

public class FileResultDialog extends MessageDialog {
	private final Set<FileDescriptor> failedFiles;
	private TableViewer tableViewer;
	
	public static FileResultDialog createDialog(Shell parentShell, String title, String message, Set<FileDescriptor> failedFiles) {
		FileResultDialog dialog = new FileResultDialog(parentShell, title, message, failedFiles);
		return dialog;
	}
	
	public static FileResultDialog createDialog(Shell parentShell, String title, AudioFileResult result) {
		String message = "Operation succeeded for " + result.getSucceededFiles().size()  + " file(s).";
		FileResultDialog dialog = new FileResultDialog(parentShell, title, message, result.getFailedFiles());
		return dialog;
	}

	private FileResultDialog(Shell parentShell, String dialogTitle, String message, Set<FileDescriptor> failedFiles) {
		super(parentShell, dialogTitle, null, null, failedFiles.isEmpty() ? MessageDialog.INFORMATION : MessageDialog.WARNING, new String[] { IDialogConstants.OK_LABEL }, 0);
		super.setShellStyle(getShellStyle() | SWT.RESIZE);
		super.message = message;
		this.failedFiles = failedFiles;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createCustomArea(Composite parent) {
		if (failedFiles.isEmpty()) 
			return null;
		
		Label failedLabel = new Label(parent, SWT.NONE);
		failedLabel.setText(failedFiles.size() + " file(s) failed:");
		
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
		List<FileDescriptor> fileList = new ArrayList<FileDescriptor>(this.failedFiles);
		Collections.sort(fileList, new DefaultFileDescriptorComparator());
		tableViewer.setInput(fileList);
		
		return parent;
	}
	


}
