package de.kobich.audiosolutions.frontend.audio.view.play;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.play.AudioPlayList;
import de.kobich.audiosolutions.core.service.play.AudioPlayerClient;
import de.kobich.audiosolutions.core.service.play.IAudioPlayingService;
import de.kobich.audiosolutions.core.service.play.player.IAudioPlayerListener;
import de.kobich.audiosolutions.frontend.audio.view.play.action.PauseAudioFileAction;
import de.kobich.audiosolutions.frontend.audio.view.play.ui.AudioPlayContentProvider;
import de.kobich.audiosolutions.frontend.audio.view.play.ui.AudioPlayLabelProvider;
import de.kobich.audiosolutions.frontend.common.selection.PostSelectionAdapter;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.table.ViewerColumn;
import de.kobich.commons.ui.jface.table.ViewerColumnManager;
import de.kobich.commons.ui.memento.FileListSerializer;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;
import de.kobich.component.file.FileDescriptor;

/**
 * Audio play view.
 */
public class AudioPlayView extends ViewPart implements IMementoItemSerializable { 
	public static final Logger logger = Logger.getLogger(AudioPlayView.class);
	public static final String ID = "de.kobich.audiosolutions.view.audio.playerView";
	private static final String STATE_PLAY_LIST_FILES = "playListFiles";
	public static ViewerColumn COLUMN_TRACK = new ViewerColumn("Track", 40);
	public static ViewerColumn COLUMN_FILE = new ViewerColumn("File", 60);
	public static ViewerColumnManager COLUMNS = new ViewerColumnManager(COLUMN_TRACK, COLUMN_FILE);
	
	private AudioPlayViewEventListener eventListener;
	private AudioPlayList playList;
	private AudioPlayerListener playerListener;
	private AudioPlayerClient playerClient;
	private Composite infoGroup;
	private Text trackText;
	private Scale progressScale;
	private Label timeLabel;
	private TableViewer tableViewer;
	private IMemento memento;
	private AudioPlayViewSourceProvider provider;
	private boolean disposedCalled;
	
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;
		this.eventListener = new AudioPlayViewEventListener(this);
		this.playerListener = new AudioPlayerListener(this);
		this.playerClient = new AudioPlayerClient("audio-player");
		this.playerClient.getListenerList().addListener(playerListener);
		ISourceProviderService sourceProviderService = (ISourceProviderService) site.getService(ISourceProviderService.class);
		this.provider = (AudioPlayViewSourceProvider) sourceProviderService.getSourceProvider(AudioPlayViewSourceProvider.PLAYING_STATE);
		this.disposedCalled = false;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(JFaceUtils.createViewGridLayout(1, false, JFaceUtils.MARGIN_TOP));
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// playing track
		infoGroup = new Composite(parent, SWT.NONE);
		GridLayout filterLayout = JFaceUtils.createViewGridLayout(2, false, JFaceUtils.MARGIN_WIDTH);
		infoGroup.setLayout(filterLayout);
		GridData gridData = new GridData(SWT.FILL, SWT.NONE, false, false);
		infoGroup.setLayoutData(gridData);
		
		// track
		trackText = new Text(infoGroup, SWT.BORDER | SWT.READ_ONLY);
		trackText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 2, 1));
		trackText.setBackground(JFaceUtils.getDisabledTextBackgroundColor());
		// progress
		progressScale = new Scale(infoGroup, SWT.HORIZONTAL);
		progressScale.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 1, 1));
		progressScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					long beginMillis = progressScale.getSelection();
					String time = getPlayerListener().getTimeString(beginMillis);
					getPlayerListener().setTime(time);
	
					boolean pause = (Boolean) provider.getCurrentState().get(AudioPlayViewSourceProvider.PAUSE_STATE);
					if (!pause) {
						IAudioPlayingService audioPlayService = AudioSolutions.getService(IAudioPlayingService.JAVA_ZOOM_PLAYER, IAudioPlayingService.class);
						audioPlayService.pause(getPlayerClient());
						PauseAudioFileAction.setState(AudioPlayView.this.getSite(), Boolean.TRUE);
					}
				}
				catch (AudioException exc) {
					logger.warn(exc.getMessage(), exc);
				}
			}
		});
		progressScale.addListener(SWT.Selection, new PostSelectionAdapter(progressScale.getDisplay()) {
			@Override
			public void handlePostEvent(SelectionEvent event) {
				try {
					long beginMillis = progressScale.getSelection();
					IAudioPlayingService audioPlayService = AudioSolutions.getService(IAudioPlayingService.JAVA_ZOOM_PLAYER, IAudioPlayingService.class);
					audioPlayService.skip(getPlayerClient(), beginMillis);
					PauseAudioFileAction.setState(AudioPlayView.this.getSite(), Boolean.FALSE);
				}
				catch (AudioException exc) {
					logger.warn(exc.getMessage(), exc);
				}
			}
		});
		progressScale.setEnabled(false);
		// time
		timeLabel = new Label(infoGroup, SWT.RIGHT);
		GridData timeLabelLD = JFaceUtils.createGridDataWithSpan(GridData.BEGINNING, 1, 1);
		timeLabelLD.widthHint = JFaceUtils.calculateTextSize(timeLabel, "HH:MM:SS / HH:MM:SS").x;
		timeLabel.setLayoutData(timeLabelLD);
		timeLabel.setEnabled(false);

		// play list
		Composite tableComposite = new Composite(parent, SWT.NONE);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer = new TableViewer(tableComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		for (final ViewerColumn column : COLUMNS) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, COLUMNS.indexOf(column));
			tableColumnLayout.setColumnData(viewerColumn.getColumn(), new ColumnWeightData(column.getWidthPercent(), column.getMinimumWidth()));
			viewerColumn.getColumn().setText(column.getName());
		}

		// turn on the header and the lines
		tableViewer.setContentProvider(new AudioPlayContentProvider());
		tableViewer.setLabelProvider(new AudioPlayLabelProvider(this));
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(false);
		table.setLinesVisible(true);
		
		// enable context menu
		MenuManager menuManager = new MenuManager();
		Menu menuFlat = menuManager.createContextMenu(table);
		table.setMenu(menuFlat);
		getSite().registerContextMenu(menuManager, tableViewer);
		
		// load play list
		loadPlayList();

		setContentDescription("Please select one or more audio files.");
		
		// register for events
		getViewSite().setSelectionProvider(tableViewer);
		eventListener.register();
	}

	private void loadPlayList() {
		this.playList = new AudioPlayList();

		// restore state
		restoreState();
		
		this.tableViewer.setInput(playList);
	}

	public AudioPlayList getPlayList() {
		return this.playList;
	}
	
	public void addFilesToPlayList(List<FileDescriptor> fileDescriptors) {
		getPlayList().addFiles(fileDescriptors);
		provider.changeState(AudioPlayViewSourceProvider.PLAYLIST_EMPTY_STATE, getPlayList().getFiles().isEmpty());
		refresh();
	}
	
	public void removeFilesFromPlayList(List<FileDescriptor> fileDescriptors) {
		getPlayList().removeFiles(fileDescriptors);
		provider.changeState(AudioPlayViewSourceProvider.PLAYLIST_EMPTY_STATE, getPlayList().getFiles().isEmpty());
		refresh();
	}
	
	public void startPlaying(FileDescriptor startFile) throws AudioException {
		if (startFile != null && startFile.getFile().exists()) {
			this.getPlayList().setStartFile(startFile);
		}
		else {
			this.getPlayList().setFirstStartFile();
		}
		
		IAudioPlayingService audioPlayService = AudioSolutions.getService(IAudioPlayingService.JAVA_ZOOM_PLAYER, IAudioPlayingService.class);
		audioPlayService.play(this.getPlayerClient(), this.getPlayList());
	}
	
	/**
	 * @return the selected file
	 */
	public List<FileDescriptor> getSelectedPlayItems() {
		List<FileDescriptor> playItems = new ArrayList<>();
		if (tableViewer.getTable().getSelectionCount() > 0) {
			TableItem[] selection = tableViewer.getTable().getSelection();
			for (TableItem selectionItem : selection) {
				Object data = selectionItem.getData();
				if (data instanceof FileDescriptor) {
					playItems.add((FileDescriptor) data);
				}
			}
		}
		return playItems;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		this.memento = memento;
		saveState();
	}
	
	@Override
	public void saveState() {
		IMementoItem mementoItem = MementoUtils.getMementoItemToSave(memento, AudioPlayView.class.getName());
		if (mementoItem == null) {
			return;
		}
		List<File> files = getPlayList().getFiles().stream().map(f -> f.getFile()).collect(Collectors.toList());
		FileListSerializer playItemsMemento = new FileListSerializer(STATE_PLAY_LIST_FILES);
		playItemsMemento.save(files, mementoItem);
	}
	
	@Override
	public void restoreState() {
		IMementoItem mementoItem = MementoUtils.getMementoItemToRestore(memento, AudioPlayView.class.getName());
		if (mementoItem == null) {
			return;
		}
		FileListSerializer playItemsMemento = new FileListSerializer(STATE_PLAY_LIST_FILES);

		List<File> files = playItemsMemento.restore(mementoItem);
		for (File file : files) {
			FileDescriptor fileDescriptor = new FileDescriptor(file, file.getName());
			getPlayList().addFile(fileDescriptor);
		}
		provider.changeState(AudioPlayViewSourceProvider.PLAYLIST_EMPTY_STATE, getPlayList().getFiles().isEmpty());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		this.disposedCalled = true;
		if (this.playerClient != null && playerListener != null) {
			this.playerClient.getListenerList().removeListener(playerListener);
		}
		
		// stop player
		try {
			IAudioPlayingService audioPlayService = AudioSolutions.getService(IAudioPlayingService.JAVA_ZOOM_PLAYER, IAudioPlayingService.class);
			audioPlayService.stop(getPlayerClient());
		}
		catch (AudioException e) {
			logger.warn(e.getMessage(), e);
		}
		// dispose
		this.infoGroup.dispose();
		this.trackText.dispose();
		this.progressScale.dispose();
		this.timeLabel.dispose();
		this.tableViewer.getTable().dispose();
		this.eventListener.deregister();
		super.dispose();
	}

	/**
	 * Called if no files are selected
	 */
	public void fireDeselection() {
		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				setContentDescription("Please select one or more audio files.");
			}
		});
	}

	/**
	 * Called if files are selected
	 */
	public void fireSelection(final List<FileDescriptor> audioFiles) {
		getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					String text = audioFiles.size() + " file(s) selected";
					setContentDescription(text);
				} 
				catch (Exception exc) {
					logger.error("Audio file could not be read", exc);
				}
			}
		});
	}
	
	/**
	 * Refreshes this view
	 */
	public void refresh() {
		tableViewer.refresh();
	}
	
	/**
	 * @return the playerListener
	 */
	public AudioPlayerListener getPlayerListener() {
		return playerListener;
	}

	public AudioPlayerClient getPlayerClient() {
		return playerClient;
	}

	/**
	 * AudioPlayerListener
	 */
	private static class AudioPlayerListener implements IAudioPlayerListener {
		private static final Logger logger = Logger.getLogger(AudioPlayerListener.class);
		private final AudioPlayView view;
		private long completeTrackMillis;
		
		public AudioPlayerListener(final AudioPlayView view) {
			this.view = view;
			this.completeTrackMillis = IAudioPlayerListener.TOTAL_MILLIS_UNDEFINED;
		}

		@Override
		public void paused() {
			// no-op
			view.provider.changeState(AudioPlayViewSourceProvider.PAUSE_STATE, Boolean.TRUE);
		}
		
		@Override
		public void playedMillis(final long millis) {
			final String time = getTimeString(millis);
			view.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!isViewDisposed()) {
						setTime(time);
						if (completeTrackMillis != IAudioPlayerListener.TOTAL_MILLIS_UNDEFINED) {
							view.progressScale.setSelection((int) millis);
						}
						else {
							view.progressScale.setSelection(0);
						}
					}
				}
			});
		}

		@Override
		public void play(final File file, final long totalMillis) {
			logger.info("Playing file: " + file);
			completeTrackMillis = totalMillis;
			view.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!isViewDisposed()) {
						// fire event
						view.provider.changeState(AudioPlayViewSourceProvider.PLAYING_STATE, Boolean.TRUE);
						view.provider.changeState(AudioPlayViewSourceProvider.PAUSE_STATE, Boolean.FALSE);
						PauseAudioFileAction.setState(view.getSite(), Boolean.FALSE);
						
						if (completeTrackMillis != IAudioPlayerListener.TOTAL_MILLIS_UNDEFINED) {
							view.progressScale.setMinimum(0);
							view.progressScale.setMaximum((int) completeTrackMillis);
							view.progressScale.setIncrement((int) TimeUnit.SECONDS.toMillis(1));
							view.progressScale.setPageIncrement((int) TimeUnit.SECONDS.toMillis(15));
							view.progressScale.setEnabled(true);
						}
						else {
							view.progressScale.setEnabled(false);
						}
						view.trackText.setText(file.getName());
						view.refresh();
					}
				}
			});
		}

		@Override
		public void resume(final File file) {
		}

		@Override
		public void stopped() {
			view.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!isViewDisposed()) {
						// fire event
						view.provider.changeState(AudioPlayViewSourceProvider.PLAYING_STATE, Boolean.FALSE);
						view.provider.changeState(AudioPlayViewSourceProvider.PAUSE_STATE, Boolean.FALSE);
						PauseAudioFileAction.setState(view.getSite(), Boolean.FALSE);

						view.trackText.setText("");
						view.progressScale.setSelection(0);
						view.progressScale.setEnabled(false);
						view.timeLabel.setText("");
						view.refresh();
					}
				}
			});
		}

		@Override
		public void errorOccured(final AudioException exc) {
			view.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!isViewDisposed()) {
						String msg = "Error while playing audio:\n";
						MessageDialog.openError(view.getViewSite().getShell(), "Audio Player", msg + exc.getMessage());
					}
				}
			});
		}

		private String getTimeString(long millis) {
			Date time = new Date(millis);
			DateFormat format = new SimpleDateFormat("HH:mm:ss");
			format.setTimeZone(new SimpleTimeZone(0, "simple"));
			String totalTime = format.format(time);
			return totalTime;
		}
		
		private boolean isViewDisposed() {
			return view.disposedCalled;
		}
		
		private void setTime(String time) {
			String completeTime = "";
			if (completeTrackMillis != IAudioPlayerListener.TOTAL_MILLIS_UNDEFINED) {
				completeTime = " / " + getTimeString(completeTrackMillis);
			}
			view.timeLabel.setText(time + completeTime);
			view.timeLabel.setEnabled(true);
		}

		@Override
		public void playListModified() {
			view.provider.changeState(AudioPlayViewSourceProvider.PLAYLIST_EMPTY_STATE, view.getPlayList().getFiles().isEmpty());
			view.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					view.refresh();
				}
			});
		}
	}
}