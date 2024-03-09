package de.kobich.audiosolutions.frontend.file.view.browse.ui;

import java.io.File;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.program.Program;

import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;

/**
 * Provides images for files.
 */
public class FileIconProvider {
	private static Image FOLDER_IMG = Activator.getDefault().getImage(ImageKey.FOLDER);
	private static Image FILE_IMG = Activator.getDefault().getImage(ImageKey.COMMON_FILE);
	private final BrowseFilesView view;
	private final ImageRegistry imageRegistry;

	/**
	 * Constructor
	 * @param view
	 */
	public FileIconProvider(BrowseFilesView view) {
		this.view = view;
		this.imageRegistry = new ImageRegistry();
	}

	/**
	 * Returns an icon representing the specified file.
	 * @param file
	 * @return
	 */
	public Image getImage(File file) {
		if (file.isDirectory()) {
			return FOLDER_IMG;
		}

		int lastDotPos = file.getName().lastIndexOf('.');
		if (lastDotPos == -1) {
			return FILE_IMG;
		}
		Image image = getImageByExtension(file.getName().substring(lastDotPos + 1));
		return image;
	}

	/**
	 * Returns the icon for the file type with the specified extension.
	 * @param extension
	 * @return
	 */
	private Image getImageByExtension(String extension) {
		Image image = imageRegistry.get(extension);
		if (image != null) {
			return image;
		}

		Program program = Program.findProgram(extension);
		ImageData imageData = program == null ? null : program.getImageData();
		if (imageData != null) {
			image = new Image(view.getViewSite().getShell().getDisplay(), imageData);
			imageRegistry.put(extension, image);
		}
		else {
			image = FILE_IMG;
		}
		return image;
	}
	
	public void dispose() {
		this.imageRegistry.dispose();
	}

}
