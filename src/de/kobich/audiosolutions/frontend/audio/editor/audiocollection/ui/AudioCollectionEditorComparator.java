package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.AlbumTreeNode;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.ArtistTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileDescriptorTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.RelativePathTreeNode;
import de.kobich.commons.collections.NaturalSortStringComparator;

/**
 * Audio files sorter.
 */
public class AudioCollectionEditorComparator extends ViewerComparator {
	public static enum Direction {
		ASCENDING, DESCENDING
	}

	private Direction direction;
	private AudioCollectionEditorColumn column;

	/**
	 * Constructor
	 * @param defaultColumn
	 */
	public AudioCollectionEditorComparator(AudioCollectionEditorColumn defaultColumn) {
		this.column = defaultColumn;
	}

	/**
	 * Sets the column to sort
	 * @param column
	 */
	public Direction setSortColumn(AudioCollectionEditorColumn column) {
		if (column.equals(this.column)) {
			// Same column as last sort; toggle the direction
			if (Direction.ASCENDING.equals(direction)) {
				direction = Direction.DESCENDING;
			}
			else {
				direction = Direction.ASCENDING;
			}
		}
		else {
			// New column; do an ascending sort
			this.column = column;
			direction = Direction.ASCENDING;
		}
		return direction;
	}

	/**
	 * Orders the items in such a way that books appear before moving boxes, which appear before board games.
	 */
	@Override
	public int category(Object element) {
		if (element instanceof RelativePathTreeNode) {
			return 1;
		}
		if (element instanceof FileDescriptorTreeNode) {
			return 2;
		}
		if (element instanceof AlbumTreeNode) {
			return 3;
		}
		if (element instanceof ArtistTreeNode) {
			return 4;
		}
		return 10;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int cat1 = category(e1);
		int cat2 = category(e2);
		if (cat1 != cat2) {
			return cat1 - cat2;
		}

		int rc = 0;
		if (e1 instanceof RelativePathTreeNode && e2 instanceof RelativePathTreeNode) {
			RelativePathTreeNode file1 = (RelativePathTreeNode) e1;
			RelativePathTreeNode file2 = (RelativePathTreeNode) e2;

			// Determine which column and do the appropriate sort
			switch (column) {
				case FILE_NAME:
					rc = file1.getContent().compareToIgnoreCase(file2.getContent());
					break;
				default:
					rc = 0;
					break;
			}
		}
		else if (e1 instanceof AlbumTreeNode && e2 instanceof AlbumTreeNode) {
			AlbumTreeNode file1 = (AlbumTreeNode) e1;
			AlbumTreeNode file2 = (AlbumTreeNode) e2;

			// Determine which column and do the appropriate sort
			switch (column) {
				case FILE_NAME:
					rc = file1.getContent().compareToIgnoreCase(file2.getContent());
					break;
				default:
					rc = 0;
					break;
			}
		}
		else if (e1 instanceof ArtistTreeNode && e2 instanceof ArtistTreeNode) {
			ArtistTreeNode file1 = (ArtistTreeNode) e1;
			ArtistTreeNode file2 = (ArtistTreeNode) e2;
			
			// Determine which column and do the appropriate sort
			switch (column) {
				case FILE_NAME:
					rc = file1.getContent().compareToIgnoreCase(file2.getContent());
					break;
				default:
					rc = 0;
					break;
			}
		}
		else if (e1 instanceof FileDescriptorTreeNode && e2 instanceof FileDescriptorTreeNode) {
			FileDescriptorTreeNode file1 = (FileDescriptorTreeNode) e1;
			FileDescriptorTreeNode file2 = (FileDescriptorTreeNode) e2;

			if (AudioCollectionEditorColumn.FILE_NAME.equals(column)) {
				rc = file1.getContent().getFileName().compareToIgnoreCase(file2.getContent().getFileName());
			}
			else if (file1.getContent().hasMetaData(AudioData.class) && file2.getContent().hasMetaData(AudioData.class)) {
				AudioData audioData1 = (AudioData) file1.getContent().getMetaData();
				AudioData audioData2 = (AudioData) file2.getContent().getMetaData();
				
				// Determine which column and do the appropriate sort
				switch (column) {
					case TRACK:
						rc = compareAudioAttributes(audioData1, audioData2, AudioAttribute.TRACK, false);
						break;
					case TRACK_FORMAT:
						rc = compareAudioAttributes(audioData1, audioData2, AudioAttribute.TRACK_FORMAT, false);
						break;
					case TRACK_NO:
						rc = compareAudioAttributes(audioData1, audioData2, AudioAttribute.TRACK_NO, true);
						break;
					case ARTIST:
						rc = compareAudioAttributes(audioData1, audioData2, AudioAttribute.ARTIST, false);
						break;
					case ALBUM:
						rc = compareAudioAttributes(audioData1, audioData2, AudioAttribute.ALBUM, false);
						break;
					case ALBUM_PUBLICATION:
						rc = compareAudioAttributes(audioData1, audioData2, AudioAttribute.ALBUM_PUBLICATION, false);
						break;
					case DISK:
						rc = compareAudioAttributes(audioData1, audioData2, AudioAttribute.DISK, true);
						break;
					case GENRE:
						rc = compareAudioAttributes(audioData1, audioData2, AudioAttribute.GENRE, false);
						break;
					case MEDIUM:
						rc = compareAudioAttributes(audioData1, audioData2, AudioAttribute.MEDIUM, true);
						break;
					default:
						break;
				}
			}
			else if (file1.getContent().hasMetaData(AudioData.class)) {
				rc = 1;
			}
			else if (file2.getContent().hasMetaData(AudioData.class)) {
				rc = -1;
			}
		}

		// If descending order, flip the direction
		if (Direction.DESCENDING.equals(direction)) {
			rc = -rc;
		}

		return rc;
	}
	
	/**
	 * Compares audio data by attribute
	 * @param audioData1
	 * @param audioData2
	 * @param attribute
	 * @return
	 */
	private int compareAudioAttributes(AudioData audioData1, AudioData audioData2, AudioAttribute attribute, boolean naturalSort) {
		int rc = 0;
		if (audioData1.hasAttribute(attribute) && audioData2.hasAttribute(attribute)) {
			if (AudioAttribute.TRACK_NO.equals(attribute)) {
				Integer trackNo1 = audioData1.getAttribute(attribute, Integer.class);
				Integer trackNo2 = audioData2.getAttribute(attribute, Integer.class);
				if (trackNo1 != null && trackNo2 != null) {
					rc = trackNo1.compareTo(trackNo2);
				}
			}
			else {
				String v1 = audioData1.getAttribute(attribute);
				String v2 = audioData2.getAttribute(attribute);
				if (naturalSort) {
					rc = NaturalSortStringComparator.INSTANCE.compare(v1.toLowerCase(), v2.toLowerCase());
				}
				else {
					rc = v1.compareToIgnoreCase(v2);
				}
			}
		}
		else if (audioData1.hasAttribute(attribute)) {
			rc = 1;
		}
		else if (audioData2.hasAttribute(attribute)) {
			rc = -1;
		}
		return rc;
	}
}
