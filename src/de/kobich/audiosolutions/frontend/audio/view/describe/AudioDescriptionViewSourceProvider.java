package de.kobich.audiosolutions.frontend.audio.view.describe;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Audio description view source provider.
 */
public class AudioDescriptionViewSourceProvider extends AbstractSourceProvider {
	private static final String MODIFIED_STATE = "audioDescriptionView.modified";
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] { MODIFIED_STATE };
	
	private boolean modified;
	
	public static AudioDescriptionViewSourceProvider getInstance() {
		ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		return (AudioDescriptionViewSourceProvider) sourceProviderService.getSourceProvider(MODIFIED_STATE);
	}
	
	@Override
	public void initialize(final IServiceLocator locator) {
		this.modified = false;
	}
	
	@Override
	public void dispose() {}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getCurrentState() {
		Map<String, Boolean> map = new HashMap<>();
		map.put(MODIFIED_STATE, this.modified);
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}

	/**
	 * Changes a given state
	 * @param state
	 * @param value
	 */
	public void setModified(boolean value) {
		this.modified = value;
		fireSourceChanged(ISources.WORKBENCH, MODIFIED_STATE, value);
	}
	
}
