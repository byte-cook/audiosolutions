package de.kobich.audiosolutions.frontend.audio.view.mediums.action;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.audio.view.mediums.MediumsView;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo.MediumSearch;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.component.file.FileDescriptor;

/**
 * Opens all tracks on a medium.
 */
public class OpenTracksOfMediumsAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.view.mediums.openTracksOfMediums";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		MediumsView view = (MediumsView) window.getActivePage().findView(MediumsView.ID);
		if (view == null) {
			return null;
		}

		Set<String> mediumNames = new HashSet<String>();
		for (Medium item : view.getSelectedMediums()) {
			mediumNames.add(item.getName());
		}
		if (mediumNames.isEmpty()) {
			return null;
		}

		final Set<String> mediumNamesF = mediumNames;
		final Wrapper<Set<FileDescriptor>> fileDescriptors = Wrapper.empty();
		JFaceExec.builder(window.getShell(), "Opening Tracks")
			.worker(ctx -> {
				AudioSearchService audioSearchService = AudioSolutions.getService(AudioSearchService.class);
				fileDescriptors.set(audioSearchService.searchByMediums(mediumNamesF, ctx.getProgressMonitor()));
			})
			.ui(ctx -> {
				MediumSearch search = new MediumSearch(mediumNamesF);
				SearchOpeningInfo openingInfo = new SearchOpeningInfo(search);
				FileCollection audioCollection = new FileCollection(openingInfo, fileDescriptors.orElse(Set.of()));
				
				// Open editor
				IWorkbenchPage page = window.getActivePage();
				page.openEditor(audioCollection, AudioCollectionEditor.ID);
			})
			.exceptionalDialog("Error while opening tracks")
			.runProgressMonitorDialog(true, true);
		
		return null;
	}

}
