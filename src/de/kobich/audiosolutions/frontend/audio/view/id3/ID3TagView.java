package de.kobich.audiosolutions.frontend.audio.view.id3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.audiosolutions.core.service.mp3.id3.MP3ID3TagType;
import de.kobich.audiosolutions.core.service.mp3.id3.ReadID3TagResponse;
import de.kobich.audiosolutions.frontend.audio.view.id3.action.PinViewAction;
import de.kobich.audiosolutions.frontend.audio.view.id3.model.ID3TagItem;
import de.kobich.audiosolutions.frontend.audio.view.id3.model.ID3TagModel;
import de.kobich.audiosolutions.frontend.audio.view.id3.ui.ID3TagColumnType;
import de.kobich.audiosolutions.frontend.audio.view.id3.ui.ID3TagComparator;
import de.kobich.audiosolutions.frontend.audio.view.id3.ui.ID3TagContentProvider;
import de.kobich.audiosolutions.frontend.audio.view.id3.ui.ID3TagEditingSupport;
import de.kobich.audiosolutions.frontend.audio.view.id3.ui.ID3TagLabelProvider;
import de.kobich.commons.monitor.progress.ProgressCancelException;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.progress.ProgressMonitorAdapter;
import de.kobich.component.file.FileDescriptor;

public class ID3TagView extends ViewPart { 
	public static final Logger logger = Logger.getLogger(ID3TagView.class);
	public static final String ID = "de.kobich.audiosolutions.view.audio.id3TagView";
	public static final String MP3_FORMAT = "mp3";
	private ID3TagViewEventListener eventListener;
	private Composite tableComposite;
	private TableViewer tableViewer;
	private boolean pin;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.eventListener = new ID3TagViewEventListener(this);
		ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);
		Command pinCommand = commandService.getCommand(PinViewAction.ID);
		State pinState = pinCommand.getState(PinViewAction.STATE_ID);
		Boolean pinValue = Boolean.FALSE;
		if (pinState != null) {
			pinValue = (Boolean) pinState.getValue();
		}
		this.pin = pinValue.booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(JFaceUtils.createViewGridLayout(1, false));

		// edit audio data
		tableComposite = new Composite(parent, SWT.NONE);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer = new TableViewer(tableComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		
		for (ID3TagColumnType column : ID3TagColumnType.values()) {
			TableViewerColumn valueColumn = new TableViewerColumn(tableViewer, SWT.LEFT, column.getIndex());
			valueColumn.getColumn().setText(column.getLabel());
			tableColumnLayout.setColumnData(valueColumn.getColumn(), new ColumnWeightData(column.getWidthPercent()));
			valueColumn.setEditingSupport(new ID3TagEditingSupport(this, tableViewer, column));
		}
		
		// turn on the header and the lines
		tableViewer.setContentProvider(new ID3TagContentProvider());
		tableViewer.setLabelProvider(new ID3TagLabelProvider());
		tableViewer.setComparator(new ID3TagComparator(ID3TagColumnType.TAG));
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		fireDeselection();

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
	 * Refreshes the editor
	 * @param mp3Files
	 *
	public void refreshView(List<FileDescriptor> mp3Files) {
		getViewSite().getShell().getDisplay().asyncExec(new TaskRunnable(mp3Files));
	}
	
	/**
	 * Pins the view
	 */
	public synchronized void setPinView(boolean pin) {
		this.pin = pin;
	}
	
	/**
	 * Sets content description asynchrony
	 * @param contentDescription
	 */
	public void asyncSetContentDescription(final String contentDescription) {
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				ID3TagView.this.setContentDescription(contentDescription);
			}
		});
	}

	/**
	 * Called if no files are selected
	 */
	public void fireDeselection() {
		if (pin) {
			return;
		}
		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				setContentDescription("Please select one or more MP3 files.");
				if (tableComposite != null && !tableComposite.isDisposed()) {
					tableViewer.setInput(new ID3TagModel(new HashSet<FileDescriptor>(), new ArrayList<ID3TagItem>()));
					
					// fire event
					ID3TagViewSourceProvider p = ID3TagViewSourceProvider.getInstance();
					p.setFileSelected(false);
				}
			}
		});
	}

	/**
	 * Called if files are selected
	 */
	public void fireSelection(Set<FileDescriptor> mp3Files) {
		if (pin) {
			return;
		}
		// cancel all running jobs
		IJobManager manager = Job.getJobManager();
		manager.cancel(ReadID3TagsJob.class);
		
		Job job = new ReadID3TagsJob(mp3Files);
		job.setUser(false);
		job.setSystem(true);
		job.schedule();
	}
	
	/**
	 * TaskRunnable
	 */
	private class ReadID3TagsJob extends Job {
		private Set<FileDescriptor> mp3Files;
		
		public ReadID3TagsJob(Set<FileDescriptor> mp3Files) {
			super("Reading ID3 tags");
			this.mp3Files = mp3Files;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			ProgressMonitorAdapter progressMonitor = new ProgressMonitorAdapter(monitor);
			try {
				IFileID3TagService id3TagService = AudioSolutions.getService(IFileID3TagService.JAUDIO_TAGGER, IFileID3TagService.class);
				ReadID3TagResponse response = id3TagService.readID3Tags(mp3Files, progressMonitor);
				
				Map<FileDescriptor, Map<MP3ID3TagType, String>> succeededFiles = response.getSucceededFiles();
				List<FileDescriptor> failedFiles = response.getFailedFiles();
				Set<FileDescriptor> fileDescriptors = new HashSet<FileDescriptor>(); 
				fileDescriptors.addAll(succeededFiles.keySet());
				
				Map<MP3ID3TagType, ID3TagItem> id3Tag2Item = new Hashtable<MP3ID3TagType, ID3TagItem>();
				for (MP3ID3TagType id3Tag : MP3ID3TagType.values()) {
					// collect all values
					Set<String> values = new HashSet<String>();
					for (FileDescriptor fileDescriptor : succeededFiles.keySet()) {
						String value = succeededFiles.get(fileDescriptor).get(id3Tag);
						if (value != null) {
							values.add(value);
						}
					}
					// add id3 tag item
					if (!id3Tag2Item.containsKey(id3Tag)) {
						id3Tag2Item.put(id3Tag, new ID3TagItem(id3Tag, values, fileDescriptors));
					}
				}
				final List<ID3TagItem> audioDataItems = new ArrayList<ID3TagItem>();
				audioDataItems.addAll(id3Tag2Item.values());
				
				if (succeededFiles.isEmpty()) {
					fireDeselection();
				}
				else {
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							// sets the input (must be set at the end)
							tableViewer.setInput(new ID3TagModel(mp3Files, audioDataItems));
							
							// fire event
							ID3TagViewSourceProvider p = ID3TagViewSourceProvider.getInstance();
							p.setFileSelected(true);
						}
					});
				}
				String text = String.format(mp3Files.size() > 1 ? "%d MP3 files selected" : "%d MP3 file selected", mp3Files.size());
				if (!failedFiles.isEmpty()) {
					text += String.format(", %d failed to read", failedFiles.size());
				}
				asyncSetContentDescription(text);
				
				return Status.OK_STATUS;
			} 
			catch (ProgressCancelException exc) {
				return Status.CANCEL_STATUS;
			}
			catch (Exception exc) {
				fireDeselection();
				String msg = "ID3 tags could not be read";
				asyncSetContentDescription(msg);
				logger.error("ID3 tags could not be read", exc);
				return Status.OK_STATUS;
			}
		}
		
		@Override
		public boolean belongsTo(Object family) {
			return ReadID3TagsJob.class.equals(family);
		}
	}
}