package de.kobich.audiosolutions.frontend.audio.view.id3;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.services.IServiceLocator;

/**
 * ID3 tag view source provider.
 */
public class ID3TagViewSourceProvider extends AbstractSourceProvider {
	private static Map<String, Boolean> map;
	private static String[] sourceNames;
	// variables
	public static final String FILE_SELECTED_STATE = "id3TagView.fileSelected";
	
	@Override
	public void initialize(final IServiceLocator locator) {
		map = new HashMap<String, Boolean>();
		map.put(FILE_SELECTED_STATE, Boolean.FALSE);
	}
	
	@Override
	public void dispose() {}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getCurrentState() {
		return getMap();
	}

	@Override
	public String[] getProvidedSourceNames() {
		if (sourceNames == null) {
			sourceNames = new String[] {FILE_SELECTED_STATE };
		}
		return sourceNames;
	}

	/**
	 * Changes a given state
	 * @param state
	 * @param value
	 */
	public void changeState(String state, Boolean value) {
		getMap().put(state, value);
		fireSourceChanged(ISources.WORKBENCH, state, value);
	}
	
	/**
	 * Returns the state map
	 * @return
	 */
	protected static Map<String, Boolean> getMap() {
		return map;
	}
}
