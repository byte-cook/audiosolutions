package de.kobich.audiosolutions.frontend.common.ui.editor;

import org.eclipse.core.commands.Command;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RadioState;

import de.kobich.audiosolutions.frontend.audio.action.SetAudioCollectionEditorLayoutAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LayoutType {
	FLAT("Flat Layout"), HIERARCHICAL("Hierarchical Layout"), ALBUM("Album Layout"), ARTIST("Artist Layout");
	
	@Getter
	private final String label;
	
	public static LayoutType getLayoutType(String name) {
		for (LayoutType type : values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return FLAT;
	}
	
	public static LayoutType getCurrentLayoutType() {
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand(SetAudioCollectionEditorLayoutAction.ID);
		String defaultLayout = (String) command.getState(RadioState.STATE_ID).getValue();
		return getLayoutType(defaultLayout);
	}
}