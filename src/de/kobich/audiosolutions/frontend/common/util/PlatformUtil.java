package de.kobich.audiosolutions.frontend.common.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.common.preferences.GeneralPreferencePage;

public class PlatformUtil {
	private static final Logger logger = Logger.getLogger(PlatformUtil.class);
	private static final Map<String, Integer> consoleNameCache = new HashMap<String, Integer>();

	/**
	 * Returns the source provider for a given state
	 * @param state
	 * @return
	 */
	public static ISourceProvider getSourceProvider(String state) {
		ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		return sourceProviderService.getSourceProvider(state);
	}
	
	/**
	 * Finds a console by given name
	 * @param name console name
	 * @return console
	 */
	public static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				return (MessageConsole) existing[i];
			}
		}
		return createNewConsole(name);
	}
	
	public static void showView(String viewId) throws PartInitException {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId);
	}

	/**
	 * Creates a new console by given name prefix
	 * @param name console name prefix
	 * @return console
	 */
	public static MessageConsole createNewConsole(String name) {
		String uniqueName = name;
		if (!consoleNameCache.containsKey(name)) {
			consoleNameCache.put(name, 1);
		}
		uniqueName += "-" + consoleNameCache.get(name);
		int newIndex = consoleNameCache.get(name) + 1;
		consoleNameCache.put(name, newIndex);
		
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
//		IConsole[] existing = conMan.getConsoles();
//		for (int i = 0; i < existing.length; i++) {
//			if (uniqueName.equals(existing[i].getName())) {
//				return (MessageConsole) existing[i];
//			}
//		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(uniqueName, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		
		myConsole.clearConsole();
		boolean openConsoleView = Activator.getDefault().getPreferenceStore().getBoolean(GeneralPreferencePage.OPEN_CONSOLE_VIEW);
		if (openConsoleView) {
			myConsole.activate();
		}
		return myConsole;
	}

	/**
	 * Returns a proxy tracker
	 * @return
	 */
	public static ServiceTracker<?, ?> getProxyTracker() {
		return new ServiceTracker<Object, Object>(FrameworkUtil.getBundle(PlatformUtil.class).getBundleContext(), IProxyService.class.getName(), null);
	}

	/**
	 * Returns the proxy service
	 * @return
	 */
	public static IProxyService getProxyService(ServiceTracker<?, ?> proxyTracker) {
		return (IProxyService) proxyTracker.getService();
	}

	/**
	 * Prints all available views
	 */
	public static void printAllViews() {
		for (IViewCategory cat : PlatformUI.getWorkbench().getViewRegistry().getCategories()) {
			System.out.println("View Category: " + cat.getId());
			for (IViewDescriptor view : cat.getViews()) {
				System.out.println("  View id: " + view.getId());
			}
		}
	}
	
	/**
	 * Prints all available preferences
	 */
	public static void printAllPreferences() {
		for (IPreferenceNode node : PlatformUI.getWorkbench().getPreferenceManager().getRootSubNodes()) {
			logger.info("View Category: " + node.getId());
			for (IPreferenceNode subNode : node.getSubNodes()) {
				logger.info("  View id: " + subNode.getId());
			}
		}
	}
}
