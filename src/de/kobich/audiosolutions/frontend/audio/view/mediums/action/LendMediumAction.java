package de.kobich.audiosolutions.frontend.audio.view.mediums.action;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.medium.MediumResponse;
import de.kobich.audiosolutions.core.service.medium.MediumService;
import de.kobich.audiosolutions.frontend.audio.view.mediums.MediumsView;

/**
 * Lends a medium.
 */
public class LendMediumAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(LendMediumAction.class);
	private IWorkbenchWindow window;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			MediumsView view = (MediumsView) window.getActivePage().findView(MediumsView.ID);
			if (view == null) {
				return null;
			}

			LendMediumDialog dialog = LendMediumDialog.createDialog(window.getShell());
			if (view.getSelectedMediumItem() != null) {
				dialog.setMediumName(view.getSelectedMediumItem().getMedium().getName());
			}
			int status = dialog.open();
			if (status == IDialogConstants.OK_ID) {
				String borrower = dialog.getBorrower();
				Set<String> mediumNames = new HashSet<String>();
				mediumNames.add(dialog.getMediumName());
				MediumService lentMediumService = AudioSolutions.getService(MediumService.class);
				MediumResponse response = lentMediumService.lendMediums(mediumNames, borrower);
				if (!response.getFailedMediumNames().isEmpty()) {
					MessageDialog.openError(window.getShell(), "Lend Mediums", "Medium could not be lend.\nPlease check the medium name.");
					return null;
				}

				MediumsView lentMediumsView = (MediumsView) window.getActivePage().findView(MediumsView.ID);
				if (lentMediumsView != null) {
					lentMediumsView.refresh();
				}
			}
		}
		catch (Exception e) {
			String msg = "Error while lending mediums";
			logger.error(msg, e);
			MessageDialog.openError(window.getShell(), "Lend Mediums", msg + e.getMessage());
		}
		return null;
	}
}
