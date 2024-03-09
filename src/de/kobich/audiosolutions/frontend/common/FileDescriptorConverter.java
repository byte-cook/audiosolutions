package de.kobich.audiosolutions.frontend.common;

import java.io.File;

import de.kobich.commons.converter.IConverter;
import de.kobich.component.file.FileDescriptor;

public class FileDescriptorConverter implements IConverter<FileDescriptor, File> {
	public static FileDescriptorConverter INSTANCE = new FileDescriptorConverter();

	@Override
	public File convert(FileDescriptor s) {
		return s.getFile();
	}

	@Override
	public FileDescriptor reconvert(File t) {
		return new FileDescriptor(t, t.getName());
	}

}
