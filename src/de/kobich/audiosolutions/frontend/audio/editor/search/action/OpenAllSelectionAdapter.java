package de.kobich.audiosolutions.frontend.audio.editor.search.action;

import java.util.Set;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo;
import de.kobich.audiosolutions.frontend.common.ui.editor.SearchOpeningInfo.TextSearch;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.component.file.FileDescriptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpenAllSelectionAdapter extends SelectionAdapter {
	private final IWorkbenchWindow window;
	private final String search;
	private final AudioAttribute attribute;
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		final Wrapper<Set<FileDescriptor>> FILES = Wrapper.empty();
		JFaceExec.builder(window.getShell(), "Show All")
			.worker(ctx -> {
				AudioSearchService searchService = AudioSolutions.getService(AudioSearchService.class);
				FILES.set(searchService.searchByText(search, attribute, ctx.getProgressMonitor()));
			})
			.ui(ctx -> {
				// open new editor
				TextSearch textSearch = new TextSearch(search, attribute);
				SearchOpeningInfo openingInfo = new SearchOpeningInfo(textSearch);
				FileCollection audioCollection = new FileCollection(openingInfo, FILES.get());
				IWorkbenchPage page = window.getActivePage();
				page.openEditor(audioCollection, AudioCollectionEditor.ID);
			})
			.runProgressMonitorDialog(true, true);
	}

}
