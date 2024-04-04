package de.kobich.audiosolutions.frontend.audio.editor.search.action;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.search.AudioSearchQuery;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo.StandardSearch;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DoSearchHyperlinkAdapter extends HyperlinkAdapter {
	private static final Logger logger = Logger.getLogger(DoSearchHyperlinkAdapter.class);
	private final IWorkbenchWindow window;
	private final AudioSearchQuery query;
	
	@Override
	public void linkActivated(HyperlinkEvent e) {
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
					fileDescriptors = searchService.search(query, super.getProgressMonitor());
					break;
				case UI_2:
					// open new editor
					StandardSearch standardSearch = new StandardSearch(query);
					SearchOpeningInfo openingInfo = new SearchOpeningInfo(standardSearch);
					FileCollection audioCollection = new FileCollection(openingInfo, fileDescriptors);
					IWorkbenchPage page = window.getActivePage();
					page.openEditor(audioCollection, AudioCollectionEditor.ID);
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
	}
}
