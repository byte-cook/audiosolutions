package de.kobich.audiosolutions.frontend.audio.view.artists.action;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.component.file.FileDescriptor;

/**
 * Opens all tracks of an artist.
 */
public class OpenTracksOfArtistAction extends AbstractHandler {
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
		final Wrapper<Set<FileDescriptor>> fileDescriptors = Wrapper.empty();
		JFaceExec.builder(window.getShell(), "Opening Tracks")
			.worker(ctx -> {
				AudioSearchService audioSearchService = AudioSolutions.getService(AudioSearchService.class);
				fileDescriptors.set(audioSearchService.searchByArtists(artistNamesF, ctx.getProgressMonitor()));
			})
			.ui(ctx -> {
				ArtistSearch search = new ArtistSearch(artistNamesF);
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
