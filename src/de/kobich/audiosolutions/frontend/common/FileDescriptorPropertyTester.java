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
		if (receiver instanceof FileDescriptor fileDescriptor) {
			if (expectedValue == null) {
				return false;
			}

			if (HAS_AUDIO_DATA_PROP.equals(property)) {
				boolean expected = Boolean.parseBoolean(expectedValue.toString());
				boolean actual = fileDescriptor.hasMetaData(AudioData.class)
						&& !((AudioData) fileDescriptor.getMetaData()).getState().equals(AudioState.REMOVED);
				return actual == expected;
			}
			else if (EXISTS_PROP.equals(property)) {
				Boolean expected = Boolean.parseBoolean(expectedValue.toString());
				return expected.equals(fileDescriptor.getFile().exists());
			}
		}
		return false;
	}

}
