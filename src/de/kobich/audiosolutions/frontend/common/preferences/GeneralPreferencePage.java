package de.kobich.audiosolutions.frontend.common.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.kobich.audiosolutions.frontend.Activator;

public class GeneralPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String ID = "de.kobich.audiosolutions.preferences.general";
	public static final String USE_FILE_MONITOR = "audio.useFileMonitor";
	public static final String OPEN_CONSOLE_VIEW = "audio.openConsoleView";
	public static final String LOAD_COVERS_FROM_INTERNET = "audio.loadCoversFromInternet";
	
	public GeneralPreferencePage() {
		super(GRID);
	}

	public void createFieldEditors() {
		addField(new BooleanFieldEditor(USE_FILE_MONITOR, "Use file &monitor for collection editors", getFieldEditorParent()));
		addField(new BooleanFieldEditor(OPEN_CONSOLE_VIEW, "Open &console view for external tools", getFieldEditorParent()));
		addField(new BooleanFieldEditor(LOAD_COVERS_FROM_INTERNET, "Load &album covers from internet", getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("AudioSolutions Preferences");
	}
}
