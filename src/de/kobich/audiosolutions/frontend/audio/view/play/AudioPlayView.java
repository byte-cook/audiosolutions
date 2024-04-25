package de.kobich.audiosolutions.frontend.audio.view.play;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
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

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.play.AudioPlayerClient;
import de.kobich.audiosolutions.core.service.play.IAudioPlayingService;
import de.kobich.audiosolutions.core.service.play.PersistableAudioPlayingList;
import de.kobich.audiosolutions.core.service.play.player.IAudioPlayerListener;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylist;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFileComparator;
import de.kobich.audiosolutions.core.service.playlist.PlaylistService;
import de.kobich.audiosolutions.core.service.playlist.repository.Playlist;
import de.kobich.audiosolutions.frontend.audio.view.play.action.PauseAudioFileAction;
import de.kobich.audiosolutions.frontend.audio.view.play.action.ToggleLoopEnabledAction;
import de.kobich.audiosolutions.frontend.audio.view.play.ui.AudioPlayContentProvider;
import de.kobich.audiosolutions.frontend.audio.view.play.ui.AudioPlayLabelProvider;
import de.kobich.audiosolutions.frontend.common.selection.PostSelectionAdapter;
import de.kobich.commons.concurrent.StartupLock;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.table.ViewerColumn;
import de.kobich.commons.ui.jface.table.ViewerColumnManager;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;
import lombok.Getter;

/**
 * Audio play view.
 */
public class AudioPlayView extends ViewPart implements IMementoItemSerializable, PropertyChangeListener { 
	public static final Logger logger = Logger.getLogger(AudioPlayView.class);
	public static final String ID = "de.kobich.audiosolutions.view.audio.playerView";
	private static final String PLAYLIST_NAME = "audiosolutions_player";
	private static final String STATE_CURRENT_FILE = "audioPlayView.currentFile";
	public static ViewerColumn COLUMN_TRACK = new ViewerColumn("Track", 40);
	public static ViewerColumn COLUMN_FILE = new ViewerColumn("File", 60);
	public static ViewerColumnManager COLUMNS = new ViewerColumnManager(COLUMN_TRACK, COLUMN_FILE);
	private StartupLock startupLock;
	
	@Getter
	private PersistableAudioPlayingList playlist;
	private boolean playlistModifed;
	private AudioPlayerListener playerListener;
	@Getter
	private AudioPlayerClient playerClient;
	private Composite infoGroup;
	private Text trackText;
	private Scale progressScale;
	private Label timeLabel;
	private TableViewer tableViewer;
	private IMemento memento;
	@Getter
	private AudioPlayViewSourceProvider provider;
	private boolean disposedCalled;
	
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.startupLock = new StartupLock(1);
		this.memento = memento;
		this.playerListener = new AudioPlayerListener(this);
		this.playerClient = new AudioPlayerClient("audio-player");
		this.playerClient.getListenerList().addListener(playerListener);
		this.provider = AudioPlayViewSourceProvider.getInstance();
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
					String time = playerListener.getTimeString(beginMillis);
					playerListener.setTime(time);
	
					if (!provider.isPaused()) {
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
					audioPlayService.rewind(getPlayerClient(), beginMillis);
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
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				try {
					List<EditablePlaylistFile> selection = getSelectedPlayItems();
					if (!selection.isEmpty()) {
						selection.sort(EditablePlaylistFileComparator.INSTANCE);
						startPlaying(selection.get(0));
					}
				}
				catch (Exception exc) {
					MessageDialog.openError(getSite().getShell(), getSite().getShell().getText(), exc.getMessage());
				}
			}
		});
		
		// enable context menu
		MenuManager menuManager = new MenuManager();
		Menu menuFlat = menuManager.createContextMenu(table);
		table.setMenu(menuFlat);
		getSite().registerContextMenu(menuManager, tableViewer);
		
		// load playlist
		loadPlaylist();
		
		setContentDescription("Please select one or more audio files.");
		
		// register for events
		getViewSite().setSelectionProvider(tableViewer);
	}

	private void loadPlaylist() {
		JFaceExec.builder(getSite().getShell(), "Opening Playlist")
			.ui(ctx -> setContentDescription("Loading..."))
			.worker(ctx -> {
				EditablePlaylist editablePlaylist;
				PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
				Playlist systemPlaylist = playlistService.getSystemPlaylist(PLAYLIST_NAME).orElse(null);
				if (systemPlaylist == null) {
					editablePlaylist = playlistService.createNewPlaylist(PLAYLIST_NAME, true);
				}
				else {
					editablePlaylist = playlistService.openPlaylist(systemPlaylist, null);
				}
				this.playlist = new PersistableAudioPlayingList(editablePlaylist);
				this.playlistModifed = false;
				this.startupLock.release();
			})
			.ui(ctx -> {
				restoreState();
				this.playlist.setLoopEnabled(ToggleLoopEnabledAction.getInitialValue());
				this.tableViewer.setInput(playlist);
				this.refresh();
				setContentDescription("");
			})
			.exceptionalDialog("Cannot open playlist")
			.runBackgroundJob(0, false, true, null);
	}
	
	public void startPlaying(EditablePlaylistFile startFile) throws AudioException {
		startupLock.waitForInitialisation();
		
		if (startFile != null) {
			this.playlist.setStartFile(startFile);
		}
		IAudioPlayingService audioPlayService = AudioSolutions.getService(IAudioPlayingService.JAVA_ZOOM_PLAYER, IAudioPlayingService.class);
		audioPlayService.play(this.getPlayerClient(), this.getPlaylist());
	}
	
	public Set<EditablePlaylistFile> appendFiles(Set<File> files, boolean playAsNext) {
		startupLock.waitForInitialisation();
		
		Set<EditablePlaylistFile> appendedFiles = playAsNext ? playlist.appendFilesAfterCurrent(files) : playlist.appendFiles(files);
		return appendedFiles;
	}
	
	public void appendFilesAndPlay(Set<File> files) throws AudioException {
		startupLock.waitForInitialisation();
		
		List<EditablePlaylistFile> appendedFiles = new ArrayList<>(appendFiles(files, false));
		if (!appendedFiles.isEmpty()) {
			appendedFiles.sort(EditablePlaylistFileComparator.INSTANCE);
			startPlaying(appendedFiles.get(0));
		}
	}
	
	public void removeFiles(List<EditablePlaylistFile> files) {
		startupLock.waitForInitialisation();

		this.playlist.removeFiles(files);
	}
	
	/**
	 * @return the selected file
	 */
	public List<EditablePlaylistFile> getSelectedPlayItems() {
		List<EditablePlaylistFile> playItems = new ArrayList<>();
		if (tableViewer.getTable().getSelectionCount() > 0) {
			TableItem[] selection = tableViewer.getTable().getSelection();
			for (TableItem selectionItem : selection) {
				Object data = selectionItem.getData();
				if (data instanceof EditablePlaylistFile file) {
					playItems.add(file);
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
		mementoItem.putString(STATE_CURRENT_FILE, this.playlist.getCurrentFile().map(File::getAbsolutePath).orElse(""));
		
		if (this.playlistModifed) {
			JFaceExec.builder(getSite().getShell())
				.worker(ctx -> {
					PlaylistService playlistService = AudioSolutions.getService(PlaylistService.class);
					playlistService.savePlaylist(this.playlist.getPlaylist(), null);
				})
				.exceptionalDialog("Could not save playing list")
				.runBackgroundJob(0, false, true, null);
		}
	}
	
	@Override
	public void restoreState() {
		IMementoItem mementoItem = MementoUtils.getMementoItemToRestore(memento, AudioPlayView.class.getName());
		if (mementoItem == null) {
			return;
		}
		
		String currentFilePath = mementoItem.getString(STATE_CURRENT_FILE, "");
		if (StringUtils.isNoneBlank(currentFilePath)) {
			this.playlist.getFile(new File(currentFilePath)).ifPresent(f -> this.playlist.setStartFile(f));
		}
		
		provider.setPlaylistEmpty(this.playlist.isEmpty());
		this.playlist.getPropertyChangeSupport().addPropertyChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		this.playlist.getPropertyChangeSupport().removePropertyChangeListener(this);
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
		super.dispose();
	}
	
	/**
	 * Refreshes this view
	 */
	public void refresh() {
		// scroll to current file
		this.playlist.getCurrentFile().flatMap(f -> this.playlist.getFile(f)).ifPresent(f -> tableViewer.reveal(f));
		tableViewer.refresh();
	}
	
	public void setLoopEnabled(boolean newValue) {
		this.playlist.setLoopEnabled(newValue);
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
			view.provider.setPaused(true);
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
						// fire events
						view.provider.setPlaying(true);
						view.provider.setPaused(false);
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
		public void resume() {
		}

		@Override
		public void stopped() {
			view.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!isViewDisposed()) {
						// fire event
						view.provider.setPlaying(false);
						view.provider.setPaused(false);
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
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		provider.setPlaylistEmpty(this.playlist.isEmpty());
		playlistModifed = true;
	}
}