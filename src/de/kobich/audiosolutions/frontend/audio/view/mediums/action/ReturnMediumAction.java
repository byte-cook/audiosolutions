package de.kobich.audiosolutions.frontend.audio.view.mediums.action;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.medium.MediumResponse;
import de.kobich.audiosolutions.core.service.medium.MediumService;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;
import de.kobich.audiosolutions.frontend.audio.view.mediums.MediumsView;

/**
 * Returns a medium.
 */
public class ReturnMediumAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(ReturnMediumAction.class);
	private IWorkbenchWindow window;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			MediumsView view = (MediumsView) window.getActivePage().findView(MediumsView.ID);
			if (view == null) {
				return null;
			}
			
			Set<Medium> mediums = view.getSelectedMediums();
			Set<String> mediumNames = new HashSet<String>();
			for (Medium item : mediums) {
				if (item.isLent()) {
					mediumNames.add(item.getName());
				}
			}
			
			MediumService lentMediumService = AudioSolutions.getService(MediumService.class);
			MediumResponse response = lentMediumService.returnMediums(mediumNames);
			if (!response.getFailedMediumNames().isEmpty()) {
				MessageDialog.openError(window.getShell(), "Lend Mediums", "Medium could not be returned.\nPlease check the medium name.");
				return null;
			}

			view.refresh();
		}
		catch (Exception e) {
			String msg = "Error while lending mediums";
			logger.error(msg, e);
			MessageDialog.openError(window.getShell(), "Lend Mediums", msg + e.getMessage());
		}
		return null;
	}
}
