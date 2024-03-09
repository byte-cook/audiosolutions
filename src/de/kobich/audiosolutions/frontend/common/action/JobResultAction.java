package de.kobich.audiosolutions.frontend.common.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;

import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.audiosolutions.frontend.common.ui.FileResultDialog;

public class JobResultAction extends Action {
	private final Shell parent;
	private final AudioFileResult result;
	
	public JobResultAction(String name, Shell parent, AudioFileResult result) {
		super(name);
		this.parent = parent;
		this.result = result;
	}

	@Override
	public void run() {
		FileResultDialog d = FileResultDialog.createDialog(parent, super.getText(), result);
		d.open();
	}
}
