package de.kobich.audiosolutions.frontend.file.view.rename;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Source provider for rename view.
 */
public class RenameFilesViewSourceProvider extends AbstractSourceProvider {
	private static Map<String, Boolean> map;
	private static String[] sourceNames;
	// variables
	public static final String RENAME_TAB_ENABLED_STATE = "rename.tabEnabled";
	public static final String RENAME_PREVIEW_STATE = "rename.previewRenamed";
	public static final String RENAME_FILE_STATE = "rename.fileRenamed";
	
	@Override
	public void initialize(final IServiceLocator locator) {
		map = new HashMap<String, Boolean>();
		map.put(RENAME_TAB_ENABLED_STATE, Boolean.FALSE);
		map.put(RENAME_PREVIEW_STATE, Boolean.FALSE);
		map.put(RENAME_FILE_STATE, Boolean.FALSE);
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
			sourceNames = new String[] {RENAME_TAB_ENABLED_STATE, RENAME_PREVIEW_STATE, RENAME_FILE_STATE};
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
