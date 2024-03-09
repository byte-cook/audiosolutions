package de.kobich.audiosolutions.frontend.file.view.browse.action;

import java.io.File;
import java.util.Arrays;
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
import de.kobich.audiosolutions.frontend.audio.editor.audiocollection.AudioCollectionEditor;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileOpeningInfo;
import de.kobich.audiosolutions.frontend.common.ui.editor.IOpeningInfo;
import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.descriptor.FileDescriptorService;

public class OpenAudioCollectionEditorAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(OpenAudioCollectionEditorAction.class);

	private IWorkbenchWindow window;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		BrowseFilesView view = (BrowseFilesView) window.getActivePage().findView(BrowseFilesView.ID);
		File startDir = view.getSelectedFile();

		// add files
		if (startDir != null) {
			if (startDir.isFile()) {
				startDir = startDir.getParentFile();
			}
			final File startDirectory = startDir;

			logger.debug("Add files from dir: " + startDir.getPath());

			List<RunningState> states = Arrays.asList(RunningState.WORKER_1, RunningState.UI_2);
			JFaceThreadRunner runner = new JFaceThreadRunner("Open Audio Collection Editor", window.getShell(), states) {
				private Set<FileDescriptor> fileDescriptors;

				@Override
				protected void run(RunningState state) throws Exception {
					switch (state) {
					case WORKER_1:
						FileDescriptorService fileDescriptorService = AudioSolutions.getService(FileDescriptorService.class);
						fileDescriptors = fileDescriptorService.readFiles(startDirectory, null, super.getProgressMonitor());
						break;
					case UI_2:
						if (fileDescriptors == null || fileDescriptors.isEmpty()) {
							MessageDialog.openInformation(super.getParent(), super.getName(), "No files could be found in directroy: "
									+ startDirectory.getPath());
							return;
						}

						// open editor
						IOpeningInfo openingInfo = new FileOpeningInfo(startDirectory, null);
						FileCollection fileCollection = new FileCollection(openingInfo, fileDescriptors);
						IWorkbenchPage page = window.getActivePage();
						page.openEditor(fileCollection, AudioCollectionEditor.ID);
						break;
					case UI_ERROR:
						if (super.getProgressMonitor().isCanceled()) {
							return;
						}
						Exception e = super.getException();
						logger.error(e.getMessage(), e);
						MessageDialog.openError(super.getParent(), super.getName(), "Reading directory failed: \n" + e.getMessage());
						break;
					default: 
						break;
					}
				}
			};
			runner.runProgressMonitorDialog(true, true);
		}
		return null;
	}

}
