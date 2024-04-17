package de.kobich.audiosolutions.frontend.audio.editor.playlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import lombok.Getter;

@Getter
public class PlaylistSelection {
	public static final PlaylistSelection EMPTY = new PlaylistSelection(List.of(), List.of());
	private final List<EditablePlaylistFile> files;
	private final List<EditablePlaylistFolder> folders;
	private final List<EditablePlaylistFile> allFiles;
	private final List<EditablePlaylistFile> existingFiles;
	
	public PlaylistSelection(List<EditablePlaylistFile> files, List<EditablePlaylistFolder> folders) {
		this.files = files;
		this.folders  = folders;
		// all files
		List<EditablePlaylistFile> allFilesTmp = new ArrayList<>();
		allFilesTmp.addAll(this.files);
		this.folders.forEach(f -> allFilesTmp.addAll(f.getFiles()));
		this.allFiles = Collections.unmodifiableList(allFilesTmp);
		// existing files
		List<EditablePlaylistFile> existingFilesTmp = new ArrayList<>();
		for (EditablePlaylistFile file : getAllFiles()) {
			if (file.getFile().exists()) {
				existingFilesTmp.add(file);
			}
		}
		this.existingFiles = Collections.unmodifiableList(existingFilesTmp);
	}
	
	public boolean isEmpty() {
		return files.isEmpty() && folders.isEmpty();
	}
}