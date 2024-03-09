package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.frontend.common.FileDescriptorConverter;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.AudioDelta;
import de.kobich.audiosolutions.frontend.common.listener.FileDelta;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor.CollectionEditorType;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo.ArtistSearch;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo.MediumSearch;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo.StandardSearch;
import de.kobich.commons.converter.ConverterUtils;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.FileException;
import de.kobich.component.file.descriptor.FileDescriptorService;

public class CollectionEditorUpdateManager {
	private final static Logger logger = Logger.getLogger(CollectionEditorUpdateManager.class);

	private static enum AudioJob {
		SEARCH, APPLY_EDITOR_DELTA, REFRESH_ACTIVE, NOTHING, NOT_POSSIBLE
	}

	private static enum FileJob {
		READ_DIR_ACTIVE, READ_DIR_INACTIVE, REFRESH_ACTIVE, REFRESH_INACTIVE, NOTHING
	}

	private static final Map<String, AudioJob> audioJob;
	private static final Map<String, FileJob> fileJob;
	private final ICollectionEditor editor;
	private final ICollectionEditorModel model;

	static {
		audioJob = new HashMap<String, AudioJob>();
		audioJob.put(createKey(CollectionEditorType.DIRECTORY, ActionType.AUDIO_SEARCH, true), AudioJob.NOT_POSSIBLE);
		audioJob.put(createKey(CollectionEditorType.DIRECTORY, ActionType.AUDIO_SEARCH, false), AudioJob.NOTHING);
		audioJob.put(createKey(CollectionEditorType.DIRECTORY, ActionType.AUDIO_DATA, true), AudioJob.REFRESH_ACTIVE);
		audioJob.put(createKey(CollectionEditorType.DIRECTORY, ActionType.AUDIO_DATA, false), AudioJob.NOTHING);
		audioJob.put(createKey(CollectionEditorType.DIRECTORY, ActionType.AUDIO_SAVED, true), AudioJob.REFRESH_ACTIVE);
		audioJob.put(createKey(CollectionEditorType.DIRECTORY, ActionType.AUDIO_SAVED, false), AudioJob.NOTHING);

		audioJob.put(createKey(CollectionEditorType.IMPORT, ActionType.AUDIO_SEARCH, true), AudioJob.NOT_POSSIBLE);
		audioJob.put(createKey(CollectionEditorType.IMPORT, ActionType.AUDIO_SEARCH, false), AudioJob.NOTHING);
		audioJob.put(createKey(CollectionEditorType.IMPORT, ActionType.AUDIO_DATA, true), AudioJob.REFRESH_ACTIVE);
		audioJob.put(createKey(CollectionEditorType.IMPORT, ActionType.AUDIO_DATA, false), AudioJob.NOTHING);
		audioJob.put(createKey(CollectionEditorType.IMPORT, ActionType.AUDIO_SAVED, true), AudioJob.REFRESH_ACTIVE);
		audioJob.put(createKey(CollectionEditorType.IMPORT, ActionType.AUDIO_SAVED, false), AudioJob.NOTHING);

		audioJob.put(createKey(CollectionEditorType.SEARCH, ActionType.AUDIO_SEARCH, true), AudioJob.APPLY_EDITOR_DELTA);
		audioJob.put(createKey(CollectionEditorType.SEARCH, ActionType.AUDIO_SEARCH, false), AudioJob.NOTHING);
		audioJob.put(createKey(CollectionEditorType.SEARCH, ActionType.AUDIO_DATA, true), AudioJob.APPLY_EDITOR_DELTA);
		audioJob.put(createKey(CollectionEditorType.SEARCH, ActionType.AUDIO_DATA, false), AudioJob.NOTHING);
		audioJob.put(createKey(CollectionEditorType.SEARCH, ActionType.AUDIO_SAVED, true), AudioJob.SEARCH);
		audioJob.put(createKey(CollectionEditorType.SEARCH, ActionType.AUDIO_SAVED, false), AudioJob.SEARCH);

		fileJob = new HashMap<String, FileJob>();
		fileJob.put(createKey(CollectionEditorType.DIRECTORY, ActionType.FILE, true), FileJob.READ_DIR_ACTIVE);
		fileJob.put(createKey(CollectionEditorType.DIRECTORY, ActionType.FILE, false), FileJob.READ_DIR_INACTIVE);
		fileJob.put(createKey(CollectionEditorType.DIRECTORY, ActionType.FILE_MONITOR, true), FileJob.READ_DIR_ACTIVE);
		fileJob.put(createKey(CollectionEditorType.DIRECTORY, ActionType.FILE_MONITOR, false), FileJob.READ_DIR_INACTIVE);

		fileJob.put(createKey(CollectionEditorType.IMPORT, ActionType.FILE, true), FileJob.REFRESH_ACTIVE);
		fileJob.put(createKey(CollectionEditorType.IMPORT, ActionType.FILE, false), FileJob.REFRESH_INACTIVE);
		fileJob.put(createKey(CollectionEditorType.IMPORT, ActionType.FILE_MONITOR, true), FileJob.REFRESH_ACTIVE);
		fileJob.put(createKey(CollectionEditorType.IMPORT, ActionType.FILE_MONITOR, false), FileJob.REFRESH_INACTIVE);

		fileJob.put(createKey(CollectionEditorType.SEARCH, ActionType.FILE, true), FileJob.REFRESH_ACTIVE);
		fileJob.put(createKey(CollectionEditorType.SEARCH, ActionType.FILE, false), FileJob.REFRESH_INACTIVE);
		fileJob.put(createKey(CollectionEditorType.SEARCH, ActionType.FILE_MONITOR, true), FileJob.REFRESH_ACTIVE);
		fileJob.put(createKey(CollectionEditorType.SEARCH, ActionType.FILE_MONITOR, false), FileJob.REFRESH_INACTIVE);
	}

	/**
	 * Create an unique key for each combination
	 * 
	 * @return
	 */
	private static String createKey(CollectionEditorType editorType, ActionType actionType, boolean active) {
		return editorType.name() + actionType.name() + active;
	}

	public CollectionEditorUpdateManager(ICollectionEditor editor, ICollectionEditorModel contentProvider) {
		this.editor = editor;
		this.model = contentProvider;
	}

	/**
	 * Updates the editor input and returns corresponding layout delta 
	 * @param delta
	 * @return
	 */
	public synchronized LayoutDelta update(AudioDelta delta) {
		// This method is synchronized in order to avoid update conflicts on editor input.
		try {
			FileCollection fileCollection = editor.getFileCollection();

			boolean active = delta.getEditorDelta() != null ? delta.getEditorDelta().isActionEditor(this.editor) : false;
			AudioJob job = audioJob.get(createKey(fileCollection.getEditorType(), delta.getActionType(), active));
			switch (job) {
			case SEARCH:
				return doSearch(delta.getActionType());
			case APPLY_EDITOR_DELTA:
				return this.model.updateModel(delta.getEditorDelta());
			case REFRESH_ACTIVE:
				// prefer files from delta inclusive meta data
				FileDescriptorSet refreshA = new FileDescriptorSet();
				refreshA.addFileDescriptors(delta.getEditorDelta().getAllItems(), true);
				return doRefreshFiles(delta.getActionType(), refreshA, delta.getEditorDelta().getAllItems());
			case NOTHING:
			case NOT_POSSIBLE:
				return null;
			}
			logger.error("Job not found: " + job);
			return null;
		}
		catch (Exception e) {
			logger.warn(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Updates the editor input and returns corresponding layout delta 
	 * @param delta
	 * @return
	 */
	public synchronized LayoutDelta update(FileDelta delta) {
		// This method is synchronized in order to avoid update conflicts on editor input.
		try {
			FileCollection fileCollection = editor.getFileCollection();

			boolean active = delta.getEditorDelta() != null ? delta.getEditorDelta().isActionEditor(this.editor) : false;
			FileJob job = fileJob.get(createKey(fileCollection.getEditorType(), delta.getActionType(), active));
			switch (job) {
			case READ_DIR_ACTIVE:
				// 1. prefer new files from delta inclusive meta data
				// 2. prefer updated files from delta inclusive meta data
				// 3. prefer files from editor inclusive meta data
				FileDescriptorSet readA = new FileDescriptorSet();
				readA.addFileDescriptors(delta.getEditorDelta().getAddItems(), true);
				readA.addFileDescriptors(delta.getEditorDelta().getUpdateItems(), true);
				readA.addFileDescriptors(fileCollection.getFileDescriptors(), false);
				// add replace items
				Map<FileDescriptor, FileDescriptor> replaceItems = delta.getEditorDelta().getReplaceItems();
				return doReloadDir(delta.getActionType(), readA, replaceItems, delta);
			case READ_DIR_INACTIVE:
				// prefer files from editor inclusive meta data
				FileDescriptorSet readI = new FileDescriptorSet();
				readI.addFileDescriptors(fileCollection.getFileDescriptors(), true);
				return doReloadDir(delta.getActionType(), readI, null, delta);
			case REFRESH_ACTIVE:
				// prefer files from delta inclusive meta data
				FileDescriptorSet refreshA = new FileDescriptorSet();
				refreshA.addFileDescriptors(delta.getEditorDelta().getAllItems(), true);
				return doRefreshFiles(delta.getActionType(), refreshA, delta.getEditorDelta().getAllItems());
			case REFRESH_INACTIVE:
				// prefer files from editor inclusive meta data
				FileDescriptorSet refreshI = new FileDescriptorSet();
				refreshI.addFileDescriptors(fileCollection.getFileDescriptors(), true);
				// helper
				FileDescriptorSet upI = new FileDescriptorSet();
				upI.addFiles(delta.getAllItems(), true);
				return doRefreshFiles(delta.getActionType(), refreshI, upI.getFileDescriptors());
			case NOTHING:
				return null;
			}
			logger.error("Job not found: " + job);
			return null;
		}
		catch (Exception e) {
			logger.warn(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Refreshes only the files which are already available in this editor
	 */
	private LayoutDelta doRefreshFiles(ActionType actionType, FileDescriptorSet originalFiles, Collection<FileDescriptor> updatedFiles) {
		FileCollection fileCollection = editor.getFileCollection();

		Set<FileDescriptor> editorFiles = new HashSet<>();
		for (FileDescriptor fd : updatedFiles) {
			if (fileCollection.containsFileDescriptor(fd.getRelativePath())) {
				editorFiles.add(fd);
			}
		}
		CollectionEditorDelta updateOnly = new CollectionEditorDelta(actionType, this.editor);
		Set<FileDescriptor> updateFileDescriptors = originalFiles.mergeFileDescriptors(editorFiles);
		updateOnly.getUpdateItems().addAll(updateFileDescriptors);
		return this.model.updateModel(updateOnly);
	}

	/**
	 * Runs the search again
	 */
	private LayoutDelta doSearch(ActionType actionType) {
		SearchOpeningInfo openingInfo = this.editor.getFileCollection().getOpeningInfo(SearchOpeningInfo.class);
		if (openingInfo.getStandardSearch() != null) {
			StandardSearch search = openingInfo.getStandardSearch();

			AudioSearchService searchService = AudioSolutions.getService(AudioSearchService.class);
			Set<FileDescriptor> newFileDescriptors = searchService.search(search.query, null);

			CollectionEditorDelta collectionDelta = createEditorDelta(actionType, newFileDescriptors, null);
			return this.model.updateModel(collectionDelta);
		}
		else if (openingInfo.getArtistSearch() != null) {
			ArtistSearch search = openingInfo.getArtistSearch();

			AudioSearchService audioSearchService = AudioSolutions.getService(AudioSearchService.class);
			Set<FileDescriptor> newFileDescriptors = audioSearchService.searchByArtists(search.artistNames, null);

			CollectionEditorDelta collectionDelta = createEditorDelta(actionType, newFileDescriptors, null);
			return this.model.updateModel(collectionDelta);
		}
		else if (openingInfo.getMediumSearch() != null) {
			MediumSearch search = openingInfo.getMediumSearch();

			AudioSearchService audioSearchService = AudioSolutions.getService(AudioSearchService.class);
			Set<FileDescriptor> newFileDescriptors = audioSearchService.searchByMediums(search.mediumNames, null);

			CollectionEditorDelta collectionDelta = createEditorDelta(actionType, newFileDescriptors, null);
			return this.model.updateModel(collectionDelta);
		}
		return null;
	}

	/**
	 * Reload files from file system
	 * 
	 * @return
	 */
	private LayoutDelta doReloadDir(ActionType actionType, FileDescriptorSet originalFiles, Map<FileDescriptor, FileDescriptor> replaceItems,
			FileDelta fileDelta) throws FileException {
		FileCollection fileCollection = editor.getFileCollection();
		FileDescriptorService fileDescriptorService = AudioSolutions.getService(FileDescriptorService.class);

		FileOpeningInfo openingInfo = fileCollection.getOpeningInfo(FileOpeningInfo.class);
		Set<FileDescriptor> fd = fileDescriptorService.readFiles(openingInfo.getDirectory(), openingInfo.getFileFilter(), null);
		Set<FileDescriptor> newFileDescriptors = originalFiles.mergeFileDescriptors(fd);

		CollectionEditorDelta collectionDelta = createEditorDelta(ActionType.FILE, newFileDescriptors, fileDelta);
		if (replaceItems != null) {
			collectionDelta.getReplaceItems().putAll(replaceItems);
		}
		return this.model.updateModel(collectionDelta);
	}

	/**
	 * Creates the editor delta by comparing the new files with the current
	 * files collection
	 * 
	 * @param newFileDescriptors
	 *            all files (represents the new editor's model)
	 * @param fileDelta
	 *            the file delta if available (can be null)
	 * @return
	 */
	private CollectionEditorDelta createEditorDelta(ActionType actionType, Set<FileDescriptor> newFileDescriptors, FileDelta fileDelta) {
		FileCollection fileCollection = editor.getFileCollection();
		// current files
		FileDescriptorSet currentFileDescriptorSet = new FileDescriptorSet();
		currentFileDescriptorSet.addFileDescriptors(fileCollection.getFileDescriptors(), true);
		// new files
		FileDescriptorSet newFileDescriptorSet = new FileDescriptorSet();
		newFileDescriptorSet.addFileDescriptors(newFileDescriptors, true);
		// all files
		Set<FileDescriptor> all = new HashSet<FileDescriptor>();
		all.addAll(newFileDescriptorSet.getFileDescriptors());
		all.addAll(currentFileDescriptorSet.getFileDescriptors());

		// create deltas
		CollectionEditorDelta collectionDelta = new CollectionEditorDelta(actionType, this.editor);
		for (FileDescriptor file : all) {
			if (!newFileDescriptorSet.contains(file) && currentFileDescriptorSet.contains(file)) {
				collectionDelta.getRemoveItems().add(file);
			}
			if (newFileDescriptorSet.contains(file) && currentFileDescriptorSet.contains(file)) {
				// update only files that have been changed
				if (fileDelta == null || fileDelta.getAllItems().contains(file.getFile())) {
					collectionDelta.getUpdateItems().add(file);
				}
			}
			else if (newFileDescriptorSet.contains(file) && !currentFileDescriptorSet.contains(file)) {
				collectionDelta.getAddItems().add(file);
			}
		}
		return collectionDelta;
	}

	/**
	 * Helper class:
	 * <ul>
	 * <li>allows to prioritize files by {@link #mergeFileDescriptors(Set)}</li>
	 * <li>allows to convert FileDescriptor to File and vis-a-vie</li>
	 * </ul>
	 */
	private static class FileDescriptorSet {
		private final Set<FileDescriptor> fileDescriptors = new HashSet<FileDescriptor>();
		private final Map<File, FileDescriptor> filesMap = new HashMap<File, FileDescriptor>();
		private boolean hasFileDescriptors;

		public void addFileDescriptors(Collection<FileDescriptor> fileDescriptors, boolean overwrite) {
			this.hasFileDescriptors = true;
			addAll(fileDescriptors, overwrite);
		}

		public void addFiles(Set<File> files, boolean overwrite) {
			this.hasFileDescriptors = false;
			Collection<FileDescriptor> tmpFileDescriptors = ConverterUtils.reconvert(files, FileDescriptorConverter.INSTANCE);
			addAll(tmpFileDescriptors, overwrite);
		}

		private void addAll(Collection<FileDescriptor> fileDescriptors, boolean overwrite) {
			for (FileDescriptor fd : fileDescriptors) {
				boolean exists = this.filesMap.containsKey(fd.getFile());

				if (overwrite && exists) {
					this.filesMap.remove(fd.getFile());
					this.fileDescriptors.remove(fd);
				}
				else if (!overwrite && exists) {
					break;
				}
				this.filesMap.put(fd.getFile(), fd);
				this.fileDescriptors.add(fd);
			}

		}

		public Collection<FileDescriptor> getFileDescriptors() {
			return this.fileDescriptors;
		}

		public boolean contains(FileDescriptor fd) {
			return this.filesMap.containsKey(fd.getFile());
		}

		/**
		 * Merges files with this set (files in this set are preferred)
		 * 
		 * @param files
		 * @return
		 */
		public Set<FileDescriptor> mergeFileDescriptors(Set<FileDescriptor> files) {
			if (this.hasFileDescriptors) {
				Set<FileDescriptor> set = new HashSet<FileDescriptor>();
				for (FileDescriptor fd : files) {
					if (filesMap.containsKey(fd.getFile())) {
						// preferring internal file descriptors
						set.add(filesMap.get(fd.getFile()));
					}
					else {
						set.add(fd);
					}
				}
				return set;
			}
			return files;
		}
	}

}
