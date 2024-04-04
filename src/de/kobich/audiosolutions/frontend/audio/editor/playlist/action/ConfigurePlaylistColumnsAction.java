package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.commons.ui.jface.tree.TreeColumnData;
import de.kobich.commons.ui.jface.tree.TreeColumnLayoutManager;

public class ConfigurePlaylistColumnsAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.editor.configurePlaylistColumns";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart activeEditor = window.getActivePage().getActiveEditor(); //SelectionSupport.INSTANCE.getActiveEditor();
		
		if (activeEditor instanceof PlaylistEditor playlistEditor) {
			final TreeColumnLayoutManager columnManager = playlistEditor.getColumnManager();
			
			final ListSelectionDialog dialog = ListSelectionDialog.of(columnManager.getHideableColumns())
					.title("Configure Columns")
					.contentProvider(new ArrayContentProvider())
					.labelProvider(LabelProvider.createTextProvider(o -> ((TreeColumnData) o).getText())).message("Select visisble columns:").create(window.getShell());
			dialog.setInitialElementSelections(columnManager.getVisibleColumns());
			int status = dialog.open();
			if (status == IDialogConstants.OK_ID) {
				JFaceThreadRunner runner = new JFaceThreadRunner("Select visisble columns", window.getShell(), List.of(RunningState.WORKER_1, RunningState.UI_2)) {
					@Override
					protected void run(RunningState state) throws Exception {
						switch (state) {
							case WORKER_1:
								columnManager.setVisibleColumnsByObjectArray(dialog.getResult());
								break;
							case UI_2:
								columnManager.updateColumns();
								playlistEditor.refresh();
								break;
							case UI_ERROR:
								Exception e = super.getException();
								MessageDialog.openError(window.getShell(), super.getName(), e.getMessage());
								break;
							default:
								break;
						}
					}
				};
				runner.runProgressMonitorDialog(true, false);
			}
		}
		return null;
	}

}
