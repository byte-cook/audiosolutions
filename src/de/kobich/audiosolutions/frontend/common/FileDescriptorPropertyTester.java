package de.kobich.audiosolutions.frontend.common;

import org.eclipse.core.expressions.PropertyTester;

import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioState;
import de.kobich.component.file.FileDescriptor;

/**
 * Tests properties of <code>FileDescriptor</code>. 
 */
public class FileDescriptorPropertyTester extends PropertyTester {
	private static final String HAS_AUDIO_DATA_PROP = "hasAudioData";
	private static final String EXISTS_PROP = "exists";

	public FileDescriptorPropertyTester() {}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		FileDescriptor fileDescriptor = (FileDescriptor) receiver;
		if (HAS_AUDIO_DATA_PROP.equals(property)) {
			boolean expected = true;
			if (expectedValue != null) {
				expected = Boolean.parseBoolean(expectedValue.toString());
			}

			boolean actual = fileDescriptor.hasMetaData(AudioData.class)
					&& !((AudioData) fileDescriptor.getMetaData()).getState().equals(AudioState.REMOVED);
			if (actual == expected) {
				return true;
			}
		}
		else if (EXISTS_PROP.equals(property)) {
			if (expectedValue != null) {
				Boolean expected = Boolean.parseBoolean(expectedValue.toString());
				boolean test = expected.equals(fileDescriptor.getFile().exists());
				if (test) {
					return true;
				}
			}
		}
		return false;
	}

}
