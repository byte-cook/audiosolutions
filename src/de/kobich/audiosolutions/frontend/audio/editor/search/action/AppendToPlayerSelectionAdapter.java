package de.kobich.audiosolutions.frontend.audio.editor.search.action;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.IWorkbenchWindow;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.search.AudioSearchQuery;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.frontend.audio.view.play.AudioPlayView;
import de.kobich.audiosolutions.frontend.common.util.PlatformUtil;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.component.file.FileDescriptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AppendToPlayerSelectionAdapter extends SelectionAdapter {
	private static final Logger logger = Logger.getLogger(AppendToPlayerSelectionAdapter.class);
	private final IWorkbenchWindow window;
	private final AudioSearchQuery query;
	private final boolean playAsNext;
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		try {
			PlatformUtil.showView(AudioPlayView.ID);
			AudioPlayView audioPlayView = (AudioPlayView) window.getActivePage().findView(AudioPlayView.ID);
			if (audioPlayView == null) {
				return;
			}
	
			final Wrapper<Set<File>> addedFiles = Wrapper.empty();
			JFaceExec.builder(window.getShell(), "Append Tracks To Player")
				.worker(ctx -> {
					AudioSearchService searchService = AudioSolutions.getService(AudioSearchService.class);
					Set<FileDescriptor> fileDescriptors = searchService.search(query, ctx.getProgressMonitor());
					addedFiles.set(fileDescriptors.stream().map(FileDescriptor::getFile).collect(Collectors.toSet()));
				})
				.ui(ctx -> {
					Set<File> files = addedFiles.orElse(Set.of());
					audioPlayView.appendFiles(files, playAsNext);
				})
				.exceptionalDialog("Files cannot be played")
				.runProgressMonitorDialog(true, false);
		}
		catch (Exception exc) {
			String msg = "Cannot play file:\n";
			logger.error(msg, exc);
			MessageDialog.openError(window.getShell(), "Audio Player", msg + exc.getMessage());
			
		}
	}
}
