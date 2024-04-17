package de.kobich.audiosolutions.frontend.audio.view.mediums.action;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioStatistics;
import de.kobich.audiosolutions.core.service.medium.MediumService;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;
import de.kobich.audiosolutions.frontend.audio.view.mediums.MediumsView;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;

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
		
		final Set<Medium> mediums = view.getSelectedMediums().stream().collect(Collectors.toSet());
		if (mediums.isEmpty()) {
			return null;
		}
		
		Wrapper<AudioStatistics> statistics = Wrapper.empty();
		JFaceExec.builder(window.getShell(), "Opening Medium Statistics")
			.worker(ctx -> {
				MediumService mediumService = AudioSolutions.getService(MediumService.class);
				statistics.set(mediumService.getStatistics(mediums));
			})
			.ui(ctx -> {
				AudioStatisticsDialog dialog = new AudioStatisticsDialog(ctx.getParent(), "Medium Statistics", statistics.get());
				dialog.open();
			})
			.exceptionalDialog("Cannot open statistics")
			.runProgressMonitorDialog(true, false);
		
		return null;
	}
}
