package de.kobich.audiosolutions.frontend.audio.editor.audiocollection.ui;

import java.util.Date;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.model.AlbumTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.FileDescriptorTreeNode;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.model.RelativePathTreeNode;
import de.kobich.commons.collections.NaturalSortStringComparator;
import de.kobich.commons.utils.CompareUtils;

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
					rc = file1.getLabel().compareToIgnoreCase(file2.getLabel());
					break;
				case ARTIST:
					rc = CompareUtils.compare(file1.getArtistName().orElse(null), file2.getArtistName().orElse(null), true);
					break;
				case ALBUM:
					rc = CompareUtils.compare(file1.getAlbumName().orElse(null), file2.getAlbumName().orElse(null), true);
					break;
				case ALBUM_PUBLICATION:
					Date publicationDate1 = file1.getAlbumPublication().orElse(null);
					Date publicationDate2 = file2.getAlbumPublication().orElse(null);
					rc = CompareUtils.compare(publicationDate1, publicationDate2, true);
					break;
				default:
					rc = 0;
					break;
			}
			
			// by default, filter by name
			if (rc == 0) {
				rc = file1.getLabel().compareToIgnoreCase(file2.getLabel());
			}
		}
		else if (e1 instanceof FileDescriptorTreeNode && e2 instanceof FileDescriptorTreeNode) {
			FileDescriptorTreeNode file1 = (FileDescriptorTreeNode) e1;
			FileDescriptorTreeNode file2 = (FileDescriptorTreeNode) e2;

			switch (column) {
				case FILE_NAME:
					rc = file1.getContent().getFileName().compareToIgnoreCase(file2.getContent().getFileName());
					break;
				case EXISTS:
					rc = Boolean.valueOf(file1.getContent().getFile().exists()).compareTo(Boolean.valueOf(file2.getContent().getFile().exists()));
					break;
				case RELATIVE_PATH:
					rc = file1.getContent().getRelativePath().compareToIgnoreCase(file2.getContent().getRelativePath());
					break;
				case EXTENSION:
					rc = file1.getContent().getExtension().compareToIgnoreCase(file2.getContent().getExtension());
					break;
				case SIZE:
					rc = file1.getContent().getFile().length() > file2.getContent().getFile().length() ? 1 : -1;
					break;
				case LAST_MODIFIED:
					rc = file1.getContent().getFile().lastModified() > file2.getContent().getFile().lastModified() ? 1 : -1;
					break;
				case TRACK:
					rc = compareAudioAttributes(file1, file2, AudioAttribute.TRACK, false);
					break;
				case TRACK_FORMAT:
					rc = compareAudioAttributes(file1, file2, AudioAttribute.TRACK_FORMAT, false);
					break;
				case TRACK_NO:
					rc = compareAudioAttributes(file1, file2, AudioAttribute.TRACK_NO, true);
					break;
				case ARTIST:
					rc = compareAudioAttributes(file1, file2, AudioAttribute.ARTIST, false);
					break;
				case ALBUM:
					rc = compareAudioAttributes(file1, file2, AudioAttribute.ALBUM, false);
					break;
				case ALBUM_PUBLICATION:
					rc = compareAudioAttributes(file1, file2, AudioAttribute.ALBUM_PUBLICATION, false);
					break;
				case DISK:
					rc = compareAudioAttributes(file1, file2, AudioAttribute.DISK, true);
					break;
				case GENRE:
					rc = compareAudioAttributes(file1, file2, AudioAttribute.GENRE, false);
					break;
				case MEDIUM:
					rc = compareAudioAttributes(file1, file2, AudioAttribute.MEDIUM, true);
					break;
				default:
					break;
			}
			
			// by default, filter by name
			if (rc == 0) {
				rc = file1.getContent().getFileName().compareToIgnoreCase(file2.getContent().getFileName());
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
	 * @return
	 */
	private int compareAudioAttributes(FileDescriptorTreeNode file1, FileDescriptorTreeNode file2, AudioAttribute attribute, boolean naturalSort) {
		if (file1.getContent().hasMetaData(AudioData.class) && file2.getContent().hasMetaData(AudioData.class)) {
			AudioData audioData1 = file1.getContent().getMetaData(AudioData.class);
			AudioData audioData2 = file2.getContent().getMetaData(AudioData.class);
			
			if (audioData1.hasAttribute(attribute) && audioData2.hasAttribute(attribute)) {
				if (AudioAttribute.TRACK_NO.equals(attribute)) {
					Integer trackNo1 = audioData1.getAttribute(attribute, Integer.class);
					Integer trackNo2 = audioData2.getAttribute(attribute, Integer.class);
					if (trackNo1 != null && trackNo2 != null) {
						return trackNo1.compareTo(trackNo2);
					}
				}
				else {
					String v1 = audioData1.getAttribute(attribute);
					String v2 = audioData2.getAttribute(attribute);
					if (naturalSort) {
						return NaturalSortStringComparator.INSTANCE.compare(v1.toLowerCase(), v2.toLowerCase());
					}
					else {
						return v1.compareToIgnoreCase(v2);
					}
				}
			}
			else if (audioData1.hasAttribute(attribute)) {
				return -1;
			}
			else if (audioData2.hasAttribute(attribute)) {
				return 1;
			}
		}
		else if (file1.getContent().hasMetaData(AudioData.class)) {
			return -1;
		}
		else if (file2.getContent().hasMetaData(AudioData.class)) {
			return 1;
		}
		return 0;
	}
}
