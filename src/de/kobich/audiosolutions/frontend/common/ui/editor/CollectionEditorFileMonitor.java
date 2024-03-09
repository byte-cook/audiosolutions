package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;
import org.eclipse.ui.IWorkbenchPartSite;

import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.preferences.GeneralPreferencePage;
import de.kobich.audiosolutions.frontend.common.ui.editor.CollectionEditorFileMonitor.FileEvent.FileType;
import de.kobich.commons.ui.DelayListener;

/**
 * File monitor for collection editors. 
 */
public class CollectionEditorFileMonitor {
	private static final Logger logger = Logger.getLogger(CollectionEditorFileMonitor.class);
	private final FileOpeningInfo fileOpeningInfo;
	private FileAlterationMonitor fileMonitor;
	private FileListener fileListener;
	
	public CollectionEditorFileMonitor(ICollectionEditor editor, IWorkbenchPartSite display, FileOpeningInfo fileOpeningInfo) {
		this.fileOpeningInfo = fileOpeningInfo;
	}
	
	public void start() {
		if (this.fileMonitor != null) {
			return;
		}
		try {
			this.fileMonitor = new FileAlterationMonitor(3000);
			
			this.fileListener = new FileListener();
			FileAlterationObserver observer = new FileAlterationObserver(fileOpeningInfo.getDirectory());
			observer.addListener(fileListener);
			
			this.fileMonitor.addObserver(observer);
			this.fileMonitor.start();
		}
		catch (Exception exc) {
			logger.warn(exc.getMessage());
		}
	}
	
	public void dispose() {
		if (this.fileListener != null) {
			this.fileListener.dispose();
		}
		if (this.fileMonitor != null) {
			try {
				this.fileMonitor.stop(200);
				this.fileMonitor = null;
			}
			catch (Exception exc) {
				logger.warn(exc.getMessage());
			}
		}
	}
	
	private class FileListener implements FileAlterationListener {
		private final MonitorDelayListener delayListener;
		
		public FileListener() {
			this.delayListener = new MonitorDelayListener();
		}
	
		@Override
		public void onDirectoryChange(File file) {
		}
	
		@Override
		public void onDirectoryCreate(File file) {
		}
	
		@Override
		public void onDirectoryDelete(File file) {
		}
	
		@Override
		public void onFileChange(File file) {
			FileEvent e = new FileEvent(FileType.MODIFY, file);
			delayListener.delayEvent(e);
		}
	
		@Override
		public void onFileCreate(File file) {
			FileEvent e = new FileEvent(FileType.CREATE, file);
			delayListener.delayEvent(e);
		}
	
		@Override
		public void onFileDelete(File file) {
			FileEvent e = new FileEvent(FileType.DELETE, file);
			delayListener.delayEvent(e);
		}
	
		@Override
		public void onStart(FileAlterationObserver arg0) {
		}
	
		@Override
		public void onStop(FileAlterationObserver arg0) {
		}
		
		public void dispose() {
		}
	}
	
	private class MonitorDelayListener extends DelayListener<FileEvent> {
		
		public MonitorDelayListener() {
			super(1, TimeUnit.SECONDS);
		}

		@Override
		public void handleEvent(List<FileEvent> events) {
			boolean useFileMonitor = Activator.getDefault().getPreferenceStore().getBoolean(GeneralPreferencePage.USE_FILE_MONITOR);
			if (!useFileMonitor) {
				return;
			}
			
			UIEvent uiEvent = new UIEvent(ActionType.FILE_MONITOR);
			for (FileEvent e : events) {
				switch (e.type) {
				case CREATE:
					uiEvent.getFileDelta().getCreateItems().add(e.file);
					break;
				case MODIFY:
					uiEvent.getFileDelta().getModifyItems().add(e.file);
					break;
				case DELETE:
					uiEvent.getFileDelta().getDeleteItems().add(e.file);
					break;
				}
			}
			EventSupport.INSTANCE.fireEvent(uiEvent);
		}
	}
	
	protected static class FileEvent {
		public static enum FileType { CREATE, MODIFY, DELETE }
		public final FileType type;
		public final File file;
		
		public FileEvent(FileType type, File file) {
			this.type = type;
			this.file = file;
		}
	}
}
