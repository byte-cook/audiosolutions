package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.info.AudioInfoService;
import de.kobich.audiosolutions.core.service.info.FileInfo;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.common.preferences.GeneralPreferencePage;
import de.kobich.audiosolutions.frontend.common.proxy.RCPProxyProvider;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;

public class LogoImagePostSelectionListener implements ISelectionChangedListener {
	private static final Logger logger = Logger.getLogger(LogoImagePostSelectionListener.class);
	private final ICollectionEditor editor;
	private final CollectionEditorViewerFilter filter;
	private FileDescriptor currentFile;
	
	public LogoImagePostSelectionListener(ICollectionEditor editor, CollectionEditorViewerFilter filter) {
		this.editor = editor;
		this.filter = filter;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		FileDescriptorSelection selection = new FileDescriptorSelection(event.getSelection(), filter);
		List<FileDescriptor> files = new ArrayList<>(selection.getFileDescriptors());
		if (files.isEmpty()) {
			editor.showDefaultLogo();
			return;
		}
		// TODO: sort files as in UI 
		Collections.sort(files, new DefaultFileDescriptorComparator());
		currentFile = files.get(0);
		
		JFaceThreadRunner.cancel(LogoImagePostSelectionListener.class);

		final AudioInfoService audioInfoService = AudioSolutions.getService(AudioInfoService.class);
		final File coverArtRootDir = AudioSolutions.getCoverArtRootDir();
		
		JFaceThreadRunner runner = new JFaceThreadRunner("Checking Audio Files", editor.getSite().getShell(), List.of(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2)) {
			private FileInfo fileInfo;
			private FileDescriptor myFile;

			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case UI_1:
					myFile = currentFile;
					editor.showDefaultLogo();
					break;
				case WORKER_1:
					boolean loadCoversFromInternet = Activator.getDefault().getPreferenceStore().getBoolean(GeneralPreferencePage.LOAD_COVERS_FROM_INTERNET);
					this.fileInfo = audioInfoService.getFileInfo(List.of(myFile), loadCoversFromInternet, coverArtRootDir, new RCPProxyProvider(), null).orElse(null);
					break;
				case UI_2:
					// ensure that this thread belongs to the current selected file
					if (!myFile.equals(currentFile)) {
						break;
					}
					
					if (fileInfo != null) {
						editor.showLogo(fileInfo);
					}
					else {
						editor.showDefaultLogo();
					}
					break;
				case UI_ERROR:
					logger.warn(this.getException());
					editor.showDefaultLogo();
					break;
				default:
					break;
				}
			}
			
		};
		runner.runBackgroundJob(0, false, true, null, LogoImagePostSelectionListener.class);
	}

}
