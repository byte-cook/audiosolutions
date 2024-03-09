package de.kobich.audiosolutions.frontend.audio.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;

import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor;

/**
 * Action to set editor layout.
 */
public class SetAudioCollectionEditorLayoutAction extends AbstractHandler {
	public static final String ID = "de.kobich.audiosolutions.commands.setAudioCollectionEditorLayout";

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (HandlerUtil.matchesRadioState(event)) {
			return null;
		}
		// change layout for all visible editors
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);

		// save current state
		String currentState = event.getParameter(RadioState.PARAMETER_ID);
		HandlerUtil.updateRadioState(event.getCommand(), currentState);

		// switch layout 
		for (IEditorReference editorReference : window.getActivePage().getEditorReferences()) {
			IEditorPart editorPart = editorReference.getEditor(false);
			if (editorPart instanceof ICollectionEditor) {
				ICollectionEditor editor = (ICollectionEditor) editorPart;
				editor.switchLayout();
			}
		}
		return null;
	}

}
