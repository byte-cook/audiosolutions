package de.kobich.audiosolutions.frontend.audio.view.id3;

import org.apache.commons.collections4.Predicate;

import de.kobich.component.file.FileDescriptor;

public class ID3TagViewPredicate implements Predicate<FileDescriptor> {
	public static final ID3TagViewPredicate INSTANCE = new ID3TagViewPredicate();
	
	private ID3TagViewPredicate() {}

	@Override
	public boolean evaluate(FileDescriptor fileDescriptor) {
		if (ID3TagView.MP3_FORMAT.equalsIgnoreCase(fileDescriptor.getExtension())) {
			return true;
		}
		return false;
	}

}
