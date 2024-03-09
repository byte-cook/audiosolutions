package de.kobich.audiosolutions.frontend.audio.view.statistic;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioStatistics;
import de.kobich.audiosolutions.core.service.persist.AudioPersistenceService;
import de.kobich.audiosolutions.frontend.audio.view.statistic.ui.AudioStatisticColumnType;
import de.kobich.audiosolutions.frontend.audio.view.statistic.ui.AudioStatisticContentProvider;
import de.kobich.audiosolutions.frontend.audio.view.statistic.ui.AudioStatisticLabelProvider;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceUtils;

/**
 * Audio statistic view.
 */
public class AudioStatisticView extends ViewPart { 
	public static final Logger logger = Logger.getLogger(AudioStatisticView.class);
	public static final String ID = "de.kobich.audiosolutions.view.audio.statisticView";
	private AudioStatisticViewEventListener eventListener;
	private TableViewer tableViewer;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.eventListener = new AudioStatisticViewEventListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
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
		Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		refreshView();

		// register for events
		eventListener.register();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		this.tableViewer.getTable().setFocus();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		this.eventListener.deregister();
		this.tableViewer.getTable().dispose();
		super.dispose();
	}
	
	/**
	 * Sets content description asynchrony
	 * @param contentDescription
	 */
	public void asyncSetContentDescription(final String contentDescription) {
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				AudioStatisticView.this.setContentDescription(contentDescription);
			}
		});
	}
	
	/**
	 * Refreshes the editor
	 * @param mp3Files
	 */
	public void refreshView() {
		LoadingJob job = new LoadingJob(this);
		job.runBackgroundJob(500, false, true, null);
	}
	
	/**
	 * TaskRunnable
	 */
	private class LoadingJob extends JFaceThreadRunner {
		private final AudioStatisticView view;
		private AudioStatistics statistics;
		
		public LoadingJob(AudioStatisticView view) {
			super("Load Audio Statistic", view.getSite().getShell(), List.of(RunningState.WORKER_1, RunningState.UI_1));
			this.view = view;
		}

		@Override
		protected void run(RunningState state) throws Exception {
			switch (state) {
			case WORKER_1:
				AudioPersistenceService persistenceService = AudioSolutions.getService(AudioPersistenceService.class);
				statistics = persistenceService.getStatistics();
				break;
			case UI_1:
				tableViewer.setInput(statistics);
				break;
			case UI_ERROR:
				view.setContentDescription(super.getException().getMessage());
				break;
			default:
				break;
			}
		}
		
	}
}