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
import de.kobich.audiosolutions.frontend.common.ui.editor.FileCollection;
import de.kobich.audiosolutions.frontend.common.ui.editor.FileOpeningInfo;
import de.kobich.audiosolutions.frontend.common.ui.editor.IOpeningInfo;
import de.kobich.audiosolutions.frontend.file.editor.filecollection.FileCollectionEditor;
import de.kobich.audiosolutions.frontend.file.view.browse.BrowseFilesView;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.descriptor.FileDescriptorService;

/**
 * Opens file collection editor.
 * 
 */
public class OpenFileCollectionEditorAction extends AbstractHandler {
	private static final Logger logger = Logger.getLogger(OpenFileCollectionEditorAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		BrowseFilesView view = (BrowseFilesView) window.getActivePage().findView(BrowseFilesView.ID);
		File startDir = view.getSelectedFile();

		// add files
		if (startDir != null) {
			if (startDir.isFile()) {
				startDir = startDir.getParentFile();
			}
			logger.debug("Add files from dir: " + startDir.getPath());
			final File startDirectory = startDir;

			List<RunningState> states = Arrays.asList(RunningState.WORKER_1, RunningState.UI_2);
			JFaceThreadRunner runner = new JFaceThreadRunner("Open File Collection Editor", window.getShell(), states) {
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
						page.openEditor(fileCollection, FileCollectionEditor.ID);
						break;
					case UI_ERROR:
						if (super.getProgressMonitor().isCanceled()) {
							return;
						}
						Exception e = super.getException();
						logger.error(e.getMessage(), e);
						MessageDialog.openError(window.getShell(), super.getName(), "Reading directory failed: \n" + e.getMessage());
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
