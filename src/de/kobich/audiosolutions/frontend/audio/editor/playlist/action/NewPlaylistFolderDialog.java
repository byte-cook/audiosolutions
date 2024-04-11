package de.kobich.audiosolutions.frontend.audio.editor.playlist.action;

import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.common.util.DecoratorUtils;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.MementoUtils;
import de.kobich.commons.ui.jface.memento.DialogSettingsAdapter;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;
import lombok.Getter;

public class NewPlaylistFolderDialog extends TitleAreaDialog implements IMementoItemSerializable {
	private static final String STATE_MOVE_TO_ENABLED = "moveToEnabled";
	private final String title;
	private final Set<String> proposals;
	private Text folderNameText;
	@Getter
	private String folderName;
	private Button moveToEnabledButton;
	@Getter
	private boolean moveToEnabled;
	
	public NewPlaylistFolderDialog(Shell parentShell, Set<String> proposals) {
		super(parentShell);
		super.setShellStyle(getShellStyle() | SWT.RESIZE);
		this.title = "New Playlist Folder";
		this.proposals = proposals;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		this.setTitle(title);
		setMessage("Create a new folder for the playlist", IMessageProvider.INFORMATION);
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		JFaceUtils.createHorizontalSeparator(parent, 1);
		
		Composite contentComposite = new Composite(parent, SWT.NONE);
		contentComposite.setLayout(JFaceUtils.createDialogGridLayout(1, false, JFaceUtils.MARGIN_LEFT, JFaceUtils.MARGIN_RIGHT, JFaceUtils.MARGIN_TOP, JFaceUtils.MARGIN_BOTTOM));
		contentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label pathLabel = new Label(contentComposite, SWT.NONE);
		pathLabel.setText("Folder name:");
		folderNameText = new Text(contentComposite, SWT.BORDER);
		folderNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		folderNameText.setMessage("e.g. /my/folder");

		if (!proposals.isEmpty()) {
			try {
				ArrayList<String> proposalsList = new ArrayList<>(proposals);
				Collections.sort(proposalsList);
				SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(proposalsList.toArray(new String[0]));
				proposalProvider.setFiltering(true);
				KeyStroke keyStroke = KeyStroke.getInstance("Ctrl+Space");
				DecoratorUtils.createDecorator(folderNameText, "Press Ctrl+Space to see proposals", FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
				ContentProposalAdapter adapter = new ContentProposalAdapter(folderNameText, new TextContentAdapter(), proposalProvider, keyStroke, null);
				adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			}
			catch (ParseException exc) {
			}
		}
		
		moveToEnabledButton = new Button(contentComposite, SWT.CHECK);
		moveToEnabledButton.setText("Move selected file to the new folder");
		moveToEnabledButton.setEnabled(!proposals.isEmpty());

		JFaceUtils.createHorizontalSeparator(parent, 1);
		restoreState();
		
		return parent;
	}
	
	protected void okPressed() {
		if (StringUtils.isBlank(folderNameText.getText())) {
			this.setErrorMessage("Please define a folder name");
			return;
		}
		try {
			EditablePlaylistFolder.normalizeAndValidatePath(folderNameText.getText());
		}
		catch (InvalidPathException e) {
			this.setErrorMessage("Illegal folder name: " + e.getMessage());
			return;
		}

		this.folderName = folderNameText.getText();
		this.moveToEnabled = moveToEnabledButton.getSelection() && !proposals.isEmpty();
		saveState();
		super.okPressed();
	}
	
	@Override
	public void restoreState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		String moveToEnabled = mementoItem.getString(STATE_MOVE_TO_ENABLED, Boolean.FALSE.toString());
		moveToEnabledButton.setSelection(Boolean.parseBoolean(moveToEnabled));
	}

	@Override
	public void saveState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(getDialogBoundsSettings());
		mementoItem.putString(STATE_MOVE_TO_ENABLED, "" + moveToEnabledButton.getSelection());
	}

	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		return MementoUtils.getDialogBoundsSettings(dialogSettings, NewPlaylistFolderDialog.class.getName());
	}
	
}
