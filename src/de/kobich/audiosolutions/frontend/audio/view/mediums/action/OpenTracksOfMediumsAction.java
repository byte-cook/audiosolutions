package de.kobich.audiosolutions.frontend.audio.view.mediums.action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
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
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;

/**
 * Opens all tracks on a medium.
 * 
 */
public class OpenTracksOfMediumsAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(OpenTracksOfMediumsAction.class);
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
		List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
		JFaceThreadRunner runner = new JFaceThreadRunner("Search Tracks", window.getShell(), states) {
			private Set<FileDescriptor> fileDescriptors;

			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case UI_1:
					break;
				case WORKER_1:
					AudioSearchService audioSearchService = AudioSolutions.getService(AudioSearchService.class);
					fileDescriptors = audioSearchService.searchByMediums(mediumNamesF, super.getProgressMonitor());
					break;
				case UI_2:
					MediumSearch search = new MediumSearch(mediumNamesF);
					SearchOpeningInfo openingInfo = new SearchOpeningInfo(search);
					FileCollection audioCollection = new FileCollection(openingInfo, fileDescriptors);

					// Open editor
					try {
						IWorkbenchPage page = window.getActivePage();
						page.openEditor(audioCollection, AudioCollectionEditor.ID);
					}
					catch (final Exception exc) {
						MessageDialog.openError(window.getShell(), "Search Tracks", "Error while searching tracks: \n" + exc.getMessage());
					}
					break;
				case UI_ERROR:
					if (super.getProgressMonitor().isCanceled()) {
						return;
					}
					Exception e = super.getException();
					logger.error(e.getMessage(), e);
					MessageDialog.openError(super.getParent(), super.getName(), "Error while searching tracks: \n" + e.getMessage());
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
