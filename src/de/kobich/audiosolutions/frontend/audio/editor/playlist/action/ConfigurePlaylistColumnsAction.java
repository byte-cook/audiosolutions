package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.frontend.audio.editor.playlist.PlaylistEditor;
import de.kobich.commons.ui.jface.JFaceExec;
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
				JFaceExec.builder(window.getShell(), "Select Visible Columns")
					.worker(ctx -> {
						columnManager.setVisibleColumnsByObjectArray(dialog.getResult());
						columnManager.saveState();
					})
					.ui(ctx -> {
						columnManager.updateColumns();
						playlistEditor.refresh();
					})
					.exceptionalDialog("Could not set columns")
					.runProgressMonitorDialog(true, false);
			}
		}
		return null;
	}

}
