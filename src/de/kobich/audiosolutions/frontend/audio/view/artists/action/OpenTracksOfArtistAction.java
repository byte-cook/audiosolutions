package de.kobich.audiosolutions.frontend.audio.view.artists.action;

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
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.audio.view.artists.ArtistsView;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo.ArtistSearch;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;

/**
 * Opens all tracks of an artist.
 * 
 */
public class OpenTracksOfArtistAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(OpenTracksOfArtistAction.class);
	public static final String ID = "de.kobich.audiosolutions.commands.view.artists.openTracksOfArtists";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		ArtistsView view = (ArtistsView) window.getActivePage().findView(ArtistsView.ID);
		if (view == null) {
			return null;
		}

		Set<String> artistNames = new HashSet<String>();
		for (Artist artist : view.getSelectedArtists()) {
			artistNames.add(artist.getName());
		}
		if (artistNames.isEmpty()) {
			return null;
		}

		final Set<String> artistNamesF = artistNames;
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
					fileDescriptors = audioSearchService.searchByArtists(artistNamesF, super.getProgressMonitor());
					break;
				case UI_2:
					ArtistSearch search = new ArtistSearch(artistNamesF);
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
