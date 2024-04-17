package de.kobich.audiosolutions.frontend.audio.view.id3;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * ID3 tag view source provider.
 */
public class ID3TagViewSourceProvider extends AbstractSourceProvider {
	private static final String FILE_SELECTED_STATE = "id3TagView.fileSelected";
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] { FILE_SELECTED_STATE };
	
	private boolean fileSelected;
	
	public static ID3TagViewSourceProvider getInstance() {
		ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		return (ID3TagViewSourceProvider) sourceProviderService.getSourceProvider(FILE_SELECTED_STATE);
	}
	
	@Override
	public void initialize(final IServiceLocator locator) {
		fileSelected = false;
	}
	
	@Override
	public void dispose() {}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getCurrentState() {
		HashMap<String, Boolean> map = new HashMap<>();
		map.put(FILE_SELECTED_STATE, fileSelected);
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}
	
	public void setFileSelected(boolean b) {
		fileSelected = b;
		fireSourceChanged(ISources.WORKBENCH, FILE_SELECTED_STATE, b);
	}
	
}
