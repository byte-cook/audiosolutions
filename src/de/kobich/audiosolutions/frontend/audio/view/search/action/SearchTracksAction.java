package de.kobich.audiosolutions.frontend.audio.view.search.action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.search.AudioSearchQuery;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.audio.editor.search.AudioSearchEditor;
import de.kobich.audiosolutions.frontend.audio.editor.search.AudioSearchEditorInput;
import de.kobich.audiosolutions.frontend.audio.view.search.AudioSearchView;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor.CollectionEditorType;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo.StandardSearch;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;

/**
 * Searches for audio tracks.
 * 
 */
public class SearchTracksAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(SearchTracksAction.class);
	public static final String ID = "de.kobich.audiosolutions.commands.audio.searchTracks";
	public static final String CALLER_PARAM = "de.kobich.audiosolutions.commands.audio.searchTracks.caller";
	public static final String SEARCH_VIEW_CALLER_VALUE = "searchView";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String caller = event.getParameter(CALLER_PARAM);

		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		AudioSearchQuery query = null;
		boolean useActiveEditor = false;
		if (SEARCH_VIEW_CALLER_VALUE.equals(caller)) {
			AudioSearchView view = (AudioSearchView) window.getActivePage().findView(AudioSearchView.ID);
			String trackName = view.getAudioAttribute(AudioAttribute.TRACK);
			trackName = addWildcards(trackName);
			String mediumName = view.getAudioAttribute(AudioAttribute.MEDIUM);
			mediumName = addWildcards(mediumName);
			String artistName = view.getAudioAttribute(AudioAttribute.ARTIST);
			artistName = addWildcards(artistName);
			String genreName = view.getAudioAttribute(AudioAttribute.GENRE);
			genreName = addWildcards(genreName);
			String albumName = view.getAudioAttribute(AudioAttribute.ALBUM);
			albumName = addWildcards(albumName);
			String trackFormat = view.getAudioAttribute(AudioAttribute.TRACK_FORMAT);
			query = AudioSearchQuery.builder().trackName(trackName).mediumName(mediumName).artistName(artistName).genreName(genreName).albumName(albumName).trackFormat(trackFormat).build();
			useActiveEditor = view.isUseActiveEditor();
		}
		else {
			try {
				window.getActivePage().openEditor(AudioSearchEditorInput.INSTANCE, AudioSearchEditor.ID);
			}
			catch (PartInitException e) {
				logger.error(e.getMessage(), e);
				MessageDialog.openError(window.getShell(), "Search Tracks", "Error while opening search editor: \n" + e.getMessage());
			}
			return null;
		}

		final AudioSearchQuery queryF = query;
		final boolean useActiveEditorF = useActiveEditor;
		List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
		JFaceThreadRunner runner = new JFaceThreadRunner("Search Tracks", window.getShell(), states) {
			private Set<FileDescriptor> fileDescriptors;

			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case UI_1:
					break;
				case WORKER_1:
					AudioSearchService searchService = AudioSolutions.getService(AudioSearchService.class);
					fileDescriptors = searchService.search(queryF, super.getProgressMonitor());
					break;
				case UI_2:
					if (fileDescriptors == null || fileDescriptors.isEmpty()) {
						MessageDialog dialog = new MessageDialog(super.getParent(), super.getName(), null, "No tracks could be found", MessageDialog.INFORMATION, 0,
								new String[] {IDialogConstants.RETRY_LABEL, IDialogConstants.CANCEL_LABEL});
						int status = dialog.open();
						if (status == MessageDialog.OK) {
							SearchTracksAction.this.execute(event);
						}						
						return;
					}

					// Get the view
					IWorkbenchPage page = window.getActivePage();
					IEditorPart activeEditor = page.getActiveEditor();
					boolean searchEditor = false;
					if (activeEditor != null && activeEditor instanceof AudioCollectionEditor) {
						searchEditor = ((AudioCollectionEditor) activeEditor).getFileCollection().getEditorType().equals(CollectionEditorType.SEARCH);
					}

					StandardSearch standardSearch = new StandardSearch(queryF);
					SearchOpeningInfo openingInfo = new SearchOpeningInfo(standardSearch);
					if (useActiveEditorF && searchEditor) {
						// show results in active editor
						AudioCollectionEditor audioCollectionEditor = (AudioCollectionEditor) activeEditor;
						FileCollection editorCollection = audioCollectionEditor.getFileCollection();
						editorCollection.setOpeningInfo(openingInfo);

						Set<FileDescriptor> files2Remove = new HashSet<FileDescriptor>();
						files2Remove.addAll(editorCollection.getFileDescriptors());
						files2Remove.removeAll(fileDescriptors);

						UIEvent uiEvent = new UIEvent(ActionType.AUDIO_SEARCH, audioCollectionEditor);
						uiEvent.getEditorDelta().getRemoveItems().addAll(files2Remove);
						uiEvent.getEditorDelta().getAddItems().addAll(fileDescriptors);
						EventSupport.INSTANCE.fireEvent(uiEvent);
					}
					else {
						// open new editor
						FileCollection audioCollection = new FileCollection(openingInfo, fileDescriptors);
						page.openEditor(audioCollection, AudioCollectionEditor.ID);
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

	/**
	 * Adds wildcards to search text
	 * 
	 * @param text
	 * @return
	 */
	private String addWildcards(String text) {
		if (text != null && !text.isEmpty()) {
			return "*" + text + "*";
		}
		return null;
	}
}
