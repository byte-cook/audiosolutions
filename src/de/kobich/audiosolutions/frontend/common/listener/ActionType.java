package de.kobich.audiosolutions.frontend.common.listener;

public enum ActionType {
	/**
	 * Used to inform about file changes (new created files, modified files,
	 * deleted files). FileDelta contains all affected files.
	 * CollectionEditorDelta can be used to inform the active editor about file
	 * changes which allows to keep AudioData (e.g. rename action).
	 */
	FILE,
	/**
	 * Used to inform about file changes by external applications (only
	 * FileMonitor use this action type). FileDelta contains all affected files.
	 */
	FILE_MONITOR,
	/**
	 * Used to inform about FileDescriptors which AudioData has changed
	 * transiently (applies only the current editor). CollectionEditorDelta
	 * contains all FileDescriptors to update. This action type does not
	 * add/remove FileDescriptors.
	 */
	AUDIO_DATA,
	/**
	 * Used to inform about FileDescriptors which AudioData has changed
	 * persistently (stored in DB). CollectionEditorDelta contains all
	 * FileDescriptors to update. This action type does not add/remove
	 * FileDescriptors.
	 */
	AUDIO_SAVED,
	/**
	 * Used to inform about audio search results (applies only the current
	 * editor). CollectionEditorDelta contains all FileDescriptors to
	 * add/remove.
	 */
	AUDIO_SEARCH,
	/**
	 * Used to inform about Playlists which have been saved.
	 */
	PLAYLIST_SAVED,
	/**
	 * Used to inform about Playlists which have been deleted.
	 */
	PLAYLIST_DELETED;
}
