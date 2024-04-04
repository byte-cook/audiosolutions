package de.kobich.audiosolutions.frontend.common.ui;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.FileResult;

public class FileResultDialog extends MessageDialog {
	private final Collection<?> failedFiles;
	private final LabelProvider labelProvider;
	private final ViewerComparator comparator;
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
	
	public static FileResultDialog createDialog(Shell parentShell, String title, FileResult result) {
		String message = "Operation succeeded for " + result.getCreatedFiles().size()  + " file(s).";
		FileResultDialog dialog = new FileResultDialog(parentShell, title, message, result.getFailedFiles());
		return dialog;
	}

	private FileResultDialog(Shell parentShell, String dialogTitle, String message, Set<?> failedFiles) {
		super(parentShell, dialogTitle, null, null, failedFiles.isEmpty() ? MessageDialog.INFORMATION : MessageDialog.WARNING, new String[] { IDialogConstants.OK_LABEL }, 0);
		super.setShellStyle(getShellStyle() | SWT.RESIZE);
		super.message = message;
		this.failedFiles = failedFiles;
		this.labelProvider = new DialogLabelProvider();
		this.comparator = new DialogViewerComparator();
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
		tableViewer.setLabelProvider(this.labelProvider);
		tableViewer.setComparator(this.comparator);
		tableViewer.setInput(this.failedFiles);
		return parent;
	}
	
	private static class DialogLabelProvider extends LabelProvider {
		@Override
		public String getText(Object e) {
			if (e instanceof FileDescriptor fd) {
				return fd.getRelativePath();
			}
			else if (e instanceof File f) {
				return f.getAbsolutePath();
			}
			return e.toString();
		}
		
	}
	
	private static class DialogViewerComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof FileDescriptor fd1 && e2 instanceof FileDescriptor fd2) {
				return fd1.getFile().compareTo(fd2.getFile());
			}
			else if (e1 instanceof File f1 && e2 instanceof File f2) {
				return f1.compareTo(f2);
			}
			else {
				return e1.toString().compareTo(e2.toString());
			}
		}
	}

}
