package de.kobich.audiosolutions.frontend.audio.view.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.frontend.audio.view.search.action.SearchTracksAction;
import de.kobich.audiosolutions.frontend.common.AudioAttributeComparator;
import de.kobich.commons.ui.jface.JFaceUtils;

/**
 * Audio search view.
 */
public class AudioSearchView extends ViewPart {
	public static final String ID = "de.kobich.audiosolutions.view.audio.searchView";
	private Map<AudioAttribute, AudioAttributeElement> elements;
	private Button useActiveEditorButton;
	
	@Override
	public void init(IViewSite viewSite, IMemento memento) throws PartInitException {
		super.init(viewSite, memento);
		this.elements = new Hashtable<AudioAttribute, AudioAttributeElement>();
		elements.put(AudioAttribute.MEDIUM, new AudioAttributeElement("Medium:"));
		elements.put(AudioAttribute.GENRE, new AudioAttributeElement("Genre:"));
		elements.put(AudioAttribute.ARTIST, new AudioAttributeElement("Artist:"));
		elements.put(AudioAttribute.ALBUM, new AudioAttributeElement("Album:"));
		elements.put(AudioAttribute.TRACK, new AudioAttributeElement("Track:"));
		elements.put(AudioAttribute.TRACK_FORMAT, new AudioAttributeElement("Track Format:"));
	}

	/**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
	public void createPartControl(Composite parent) {
		parent.setLayout(JFaceUtils.createViewGridLayout(2, false, JFaceUtils.MARGIN_HEIGHT, JFaceUtils.MARGIN_WIDTH));
		
		Composite searchComposite = parent;
		
//		Composite searchComposite = new Composite(parent, SWT.NONE);
//		GridLayout gridLayout = new GridLayout();
//	    gridLayout.verticalSpacing = 5;
//	    gridLayout.numColumns = 2;
	    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
//	    gridData.grabExcessHorizontalSpace = true;
//		searchComposite.setLayoutData(gridData);
//		searchComposite.setLayout(gridLayout);
		
		// audio attributes
		Set<AudioAttribute> attributesSet = elements.keySet();
		List<AudioAttribute> attributes = new ArrayList<AudioAttribute>();
		attributes.addAll(attributesSet);
		Collections.sort(attributes, new AudioAttributeComparator());
		for (AudioAttribute attribute : attributes) {
			AudioAttributeElement element = elements.get(attribute);
			if (element.visible) {
				Label artistLabel = new Label(searchComposite, SWT.NULL);
				artistLabel.setText(element.label);
				final Text text = new Text(searchComposite, SWT.SINGLE | SWT.BORDER);
				text.setLayoutData(gridData);
				text.setEnabled(element.enabled);
				text.addKeyListener(new KeyAdapter() {
					public void keyReleased(KeyEvent e) {
						if (e.keyCode == SWT.ESC) {
							text.setText("");
						}
					}
				});
				text.addKeyListener(new KeyAdapter() {
					public void keyReleased(KeyEvent e) {
						if (e.character == SWT.CR) {
							String commandId = SearchTracksAction.ID;
							ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);
							IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
							try {
								Command command = commandService.getCommand(commandId);
								Map<String, String> parameters = new HashMap<String, String>();
								parameters.put(SearchTracksAction.CALLER_PARAM, SearchTracksAction.SEARCH_VIEW_CALLER_VALUE);
								ParameterizedCommand parameterizedCommand = ParameterizedCommand.generateCommand(command, parameters);
								handlerService.executeCommand(parameterizedCommand, null);
							} catch (Exception ex) {
								throw new RuntimeException("Command " + commandId + " not found");
							}

						}
					}
				});
				element.text = text;
			}
		}
		new Label(searchComposite, SWT.NONE);
		useActiveEditorButton = new Button(searchComposite, SWT.CHECK);
		useActiveEditorButton.setText("Show results in active editor if possible");
	}

	/**
	 * @return the medium
	 */
	public String getAudioAttribute(AudioAttribute attribute) {
		if (elements.containsKey(attribute)) {
			return elements.get(attribute).text.getText();
		}
		return null;
	}
	
	public boolean isUseActiveEditor() {
		return useActiveEditorButton.getSelection();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (elements.containsKey(AudioAttribute.ARTIST)) {
			elements.get(AudioAttribute.ARTIST).text.setFocus();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		this.elements = null;
		super.dispose();
	}
	
	private class AudioAttributeElement {
		private String label;
		private Text text;
		private boolean enabled;
		private boolean visible;
		
		public AudioAttributeElement(String label) {
			this.label = label;
			this.enabled = true;
			this.visible = true;
		}
	}
}