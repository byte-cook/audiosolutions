package de.kobich.audiosolutions.frontend.audio.view.mediums.action;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioStatistics;
import de.kobich.audiosolutions.core.service.medium.MediumService;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;
import de.kobich.audiosolutions.frontend.audio.view.mediums.MediumsView;
import de.kobich.audiosolutions.frontend.audio.view.mediums.model.MediumItem;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;

/**
 * Show medium statistics.
 */
public class ShowMediumStatisticsAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		MediumsView view = (MediumsView) window.getActivePage().findView(MediumsView.ID);
		if (view == null) {
			return null;
		}
		
		final Set<Medium> mediums = view.getSelectedMediumItems().stream().map(MediumItem::getMedium).collect(Collectors.toSet());
		if (mediums.isEmpty()) {
			return null;
		}
		
		JFaceThreadRunner runner = new JFaceThreadRunner("Opening Medium Statistics", window.getShell(), List.of(RunningState.WORKER_1, RunningState.UI_1)) {
			private AudioStatistics statistics;
			
			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case WORKER_1:
					MediumService mediumService = AudioSolutions.getService(MediumService.class);
					statistics = mediumService.getStatistics(mediums);
					break;
				case UI_1:
					AudioStatisticsDialog dialog = new AudioStatisticsDialog(super.getParent(), "Medium Statistics", statistics);
					dialog.open();
					break;
				case UI_ERROR:
					MessageDialog.openError(window.getShell(), super.getName(), super.getException().getMessage());
					break;
				default:
					break;
				}
			}
		};
		runner.runProgressMonitorDialog(true, true);
		return null;
	}
}
