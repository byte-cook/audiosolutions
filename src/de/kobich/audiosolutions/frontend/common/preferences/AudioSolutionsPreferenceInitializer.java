package de.kobich.audiosolutions.frontend.common.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.kobich.audiosolutions.frontend.Activator;

public class AudioSolutionsPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(GeneralPreferencePage.USE_FILE_MONITOR, false);
		store.setDefault(GeneralPreferencePage.OPEN_CONSOLE_VIEW, true);
		store.setDefault(GeneralPreferencePage.LOAD_COVERS_FROM_INTERNET, true);
	}

}
