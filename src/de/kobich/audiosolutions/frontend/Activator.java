package de.kobich.audiosolutions.frontend;

import org.apache.log4j.BasicConfigurator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.kobich.audiosolutions.core.AudioSolutions;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.kobich.audiosolutions";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		// init log4j
		BasicConfigurator.configure();

		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		
		AudioSolutions.shutdown();
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	// The image constants
	public static enum ImageKey {
		FILE("/icons/file/file.png"),
		COMMON_FILE("/icons/file/common-file.png"),
		FOLDER("/icons/file/folder.png"),
		ARTIST("/icons/audio/artist.png"),
		ALBUM("/icons/audio/album.png"),
		LAYOUT_FLAT("/icons/file/layout-flat.png"),
		LAYOUT_HIERARCHICAL("/icons/file/layout-hierarchical.png"),
		AUDIO_FILE_NEW_WARN("/icons/audio/audio-file-new-warn.png"),
		AUDIO_FILE_NEW("/icons/audio/audio-file-new.png"),
		AUDIO_FILE_WARN("/icons/audio/audio-file-warn.png"),
		AUDIO_FILE("/icons/audio/audio-file.png"),
		AUDIO_FILE_EDIT_WARN("/icons/audio/audio-file-edit-warn.png"),
		AUDIO_FILE_EDIT("/icons/audio/audio-file-edit.png"),
		AUDIO_FILE_REMOVE("/icons/audio/audio-file-remove.png"),
		AUDIO_COLLECTION_EDITOR("/icons/audio/audio-collection-128.png"),
		AUDIO_SEARCH_EDITOR("/icons/audio/search.png"),
		AUDIO_PLAY("icons/audio/audio-play.png"),
		AUDIO_PLAY_AS_NEXT("icons/audio/audio-play-as-next.png"),
		AUDIO_PLAY_ADD("icons/audio/audio-play-add.png"),
		FILE_COLLECTION_EDITOR("/icons/file/file-collection-128.png"),
		PLAYLIST("icons/audio/playlist.png"),
		PLAYLIST_NEW("icons/audio/playlist-add.png"),
		OPEN_MENU("icons/file/open-menu.png"),
		;
		
		private final String path;
		
		private ImageKey(String path) {
			this.path = path;
		}
	}
	
	public Image getImage(ImageKey key) {
		return getImageRegistry().get(key.name());
	}
	
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		for (ImageKey key : ImageKey.values()) {
			reg.put(key.name(), imageDescriptorFromPlugin(PLUGIN_ID, key.path));
		}
	}
}
