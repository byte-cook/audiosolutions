package de.kobich.audiosolutions.frontend;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(800, 700));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
		
		// Set the preference toolbar to the left place
		// If other menus exists then this will be on the left of them
//		IPreferenceStore apiStore = PlatformUI.getPreferenceStore();
//		apiStore.setValue(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR, "TOP_LEFT");

		configurer.setShowProgressIndicator(true);
	}

//	@Override
//	public void postWindowOpen() {
//		IStatusLineManager statusLine = getWindowConfigurer().getActionBarConfigurer().getStatusLineManager();
//		statusLine.setMessage("Ready");
//	}

}
