package de.kobich.audiosolutions.frontend.file.view.rename;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Source provider for rename view.
 */
public class RenameFilesViewSourceProvider extends AbstractSourceProvider {
	private static final String RENAME_TAB_ENABLED_STATE = "rename.tabEnabled";
	private static final String RENAME_PREVIEW_STATE = "rename.previewRenamed";
	private static final String RENAME_FILE_STATE = "rename.fileRenamed";
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] { RENAME_TAB_ENABLED_STATE, RENAME_PREVIEW_STATE, RENAME_FILE_STATE };
	
	private boolean tabEnabled;
	private boolean previewRenamed;
	private boolean fileRenamed;
	
	public static RenameFilesViewSourceProvider getInstance() {
		ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		return (RenameFilesViewSourceProvider) sourceProviderService.getSourceProvider(RENAME_TAB_ENABLED_STATE);
	}
	
	@Override
	public void initialize(final IServiceLocator locator) {
		tabEnabled = false;
		previewRenamed = false;
		fileRenamed = false;
	}
	
	@Override
	public void dispose() {}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getCurrentState() {
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		map.put(RENAME_TAB_ENABLED_STATE, this.tabEnabled);
		map.put(RENAME_PREVIEW_STATE, this.previewRenamed);
		map.put(RENAME_FILE_STATE, this.fileRenamed);
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}
	
	public void setTabEnabled(boolean b) {
		tabEnabled = b;
		fireSourceChanged(ISources.WORKBENCH, RENAME_TAB_ENABLED_STATE, b);
	}

	public void setPreviewRenamed(boolean b) {
		previewRenamed = b;
		fireSourceChanged(ISources.WORKBENCH, RENAME_PREVIEW_STATE, b);
	}

	public void setFileRenamed(boolean b) {
		fileRenamed = b;
		fireSourceChanged(ISources.WORKBENCH, RENAME_FILE_STATE, b);
	}

}
