package de.kobich.audiosolutions.frontend.audio.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AlbumIdentity;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioFileDescriptorComparator;
import de.kobich.audiosolutions.core.service.search.AudioSearchQuery;
import de.kobich.audiosolutions.core.service.search.AudioSearchQuery.AudioSearchQueryBuilder;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo.StandardSearch;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.component.file.FileDescriptor;

public class OpenTracksOfSelectionAction extends AbstractHandler {
	public static final String TYPE_PARAM = "de.kobich.audiosolutions.commands.audio.openTracksOfSelection.type";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final String type = event.getParameter(TYPE_PARAM);
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart editorPart = window.getActivePage().getActiveEditor();
		if (editorPart instanceof AudioCollectionEditor audioCollectionEditor) {
			Set<FileDescriptor> fileDescriptors = audioCollectionEditor.getFileDescriptorSelection().getFileDescriptors();
			List<FileDescriptor> fileDescriptorList = new ArrayList<>(fileDescriptors); 
			Collections.sort(fileDescriptorList, new AudioFileDescriptorComparator());
			final AudioSearchQueryBuilder builder = AudioSearchQuery.builder();
			for (FileDescriptor fileDescriptor : fileDescriptorList) {
				AudioData audioData = fileDescriptor.getMetaDataOptional(AudioData.class).orElse(null);
				if (audioData != null) {
					// artist
					if ("artist".equalsIgnoreCase(type)) {
						String artistName = audioData.getArtistIfNotDefault().orElse(null);
						if (artistName != null) {
							builder.artistName(artistName);
							break;
						}
					}
					// album
					else if ("album".equalsIgnoreCase(type)) {
						Long albumId = audioData.getAlbumIdentifier().flatMap(AlbumIdentity::getPersistentId).orElse(null);
						if (albumId != null) {
							builder.albumId(albumId).albumName(audioData.getAlbum().orElse(AudioData.DEFAULT_VALUE));
							break;
						}
					}
					// track
					else if ("track".equalsIgnoreCase(type)) {
						String trackName = audioData.getTrackIfNotDefault().orElse(null);
						if (trackName != null) {
							builder.trackName("*" + trackName + "*");
							break;
						}
					}
					else {
						throw new IllegalStateException("Unknown type: " + type);
					}
				}
			}
			
			final AudioSearchQuery query = builder.build();
			final Wrapper<Set<FileDescriptor>> FILES = Wrapper.empty();
			JFaceExec.builder(window.getShell(), "Open Albums")
				.worker(ctx -> {
					AudioSearchService searchService = AudioSolutions.getService(AudioSearchService.class);
					FILES.set(searchService.search(query, ctx.getProgressMonitor()));
				})
				.ui(ctx -> {
					// open new editor
					StandardSearch standardSearch = new StandardSearch(query);
					SearchOpeningInfo openingInfo = new SearchOpeningInfo(standardSearch);
					FileCollection audioCollection = new FileCollection(openingInfo, FILES.get());
					IWorkbenchPage page = window.getActivePage();
					page.openEditor(audioCollection, AudioCollectionEditor.ID);
				})
				.exceptionalDialog("Could not open albums")
				.runProgressMonitorDialog(true, true);
	
		}
		return null;
	}

}
