package de.kobich.audiosolutions.frontend.audio.view.mediums.action;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import de.kobich.audiosolutions.core.service.AudioStatistics;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.audio.view.statistic.ui.AudioStatisticColumnType;
import de.kobich.audiosolutions.frontend.audio.view.statistic.ui.AudioStatisticContentProvider;
import de.kobich.audiosolutions.frontend.audio.view.statistic.ui.AudioStatisticLabelProvider;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;

public class AudioStatisticsDialog extends TitleAreaDialog {
	private final String title;
	private final AudioStatistics statistics;
	private TableViewer tableViewer;
	
	public AudioStatisticsDialog(Shell parentShell, String title, AudioStatistics statistics) {
		super(parentShell);
		super.setShellStyle(getShellStyle() | SWT.RESIZE);
		this.title = title;
		this.statistics = statistics;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		if (IDialogConstants.CANCEL_ID == id) {
			return null;
		}
		return super.createButton(parent, id, label, defaultButton);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		this.setTitle(title);
		setMessage("This dialog shows audio statistics.", IMessageProvider.INFORMATION);
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		JFaceUtils.createHorizontalSeparator(parent, 1);
		
		parent.setLayout(JFaceUtils.createViewGridLayout(1, false));

		Composite tableComposite = new Composite(parent, SWT.NONE);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer = new TableViewer(tableComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

		for (AudioStatisticColumnType column : AudioStatisticColumnType.values()) {
			TableViewerColumn countColumn = new TableViewerColumn(tableViewer, SWT.LEFT, column.getIndex());
			countColumn.getColumn().setText(column.getLabel());
			countColumn.getColumn().setAlignment(column.getAlignment());
			tableColumnLayout.setColumnData(countColumn.getColumn(), new ColumnWeightData(column.getWidthPercent()));
		}

		// turn on the header and the lines
		tableViewer.setContentProvider(new AudioStatisticContentProvider());
		tableViewer.setLabelProvider(new AudioStatisticLabelProvider());
		tableViewer.setInput(this.statistics);
		Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		JFaceUtils.createHorizontalSeparator(parent, 1);
		
		return parent;
	}

	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, AudioStatisticsDialog.class.getName());
	}
}
