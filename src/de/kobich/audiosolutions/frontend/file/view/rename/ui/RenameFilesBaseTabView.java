package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import de.kobich.audiosolutions.frontend.file.view.rename.RenameFilesViewSourceProvider;
import de.kobich.commons.misc.rename.rule.IRenameRule;
import de.kobich.commons.misc.rename.rule.RenamePositionType;
import de.kobich.commons.ui.jface.JFaceUtils;

/**
 * Base class for renaming tab views.
 */
public abstract class RenameFilesBaseTabView {
	private static final Logger logger = Logger.getLogger(RenameFilesBaseTabView.class);
	protected final RenameFilesPreviewListener previewListener;
	private TabItem tabItem;
	private String originalTabItemText;
	private Button enabled;
	private Label descriptionLabel;
	private static int enabledTabCount = 0;

	/**
	 * Constructor
	 * @param tabItem
	 */
	public RenameFilesBaseTabView(RenameFilesPreviewListener previewListener) {
		this.previewListener = previewListener;
	}

	/**
	 * @param tabItem the tabItem to set
	 */
	public void setTabItem(TabItem tabItem) {
		this.tabItem = tabItem;
		this.originalTabItemText = tabItem.getText();
	}

	/**
	 * Gets the control for tab one
	 * @param tabFolder the parent tab folder
	 * @return Control
	 */
	public Control getTabControl(TabFolder tabFolder) {
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite topComposite = new Composite(composite, SWT.NONE);
		topComposite.setLayout(new GridLayout(2, false));
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		enabled = new Button(topComposite, SWT.CHECK);
		enabled.setText("Enabled");
		enabled.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean selected = ((Button) e.widget).getSelection();
				handleEnabledChanged(selected);
			}
		});
		enabled.addSelectionListener(previewListener);

		descriptionLabel = new Label(topComposite, SWT.NONE);
		descriptionLabel.setForeground(JFaceUtils.getInfoTextForegroundColor());
		descriptionLabel.setText(getDescription());

		JFaceUtils.createHorizontalSeparator(topComposite, 2);
		
		createMainTabControl(composite);
		reset();

		return composite;
	}

	/**
	 * Creates a new position combo box 
	 * @param parent
	 * @return combo box
	 */
	protected Combo createPositionCombo(Composite parent) {
		Combo position = new Combo(parent, SWT.READ_ONLY);
		for (RenamePositionType pos : RenamePositionType.values()) {
			position.add(pos.name());
		}
		position.select(0);
		return position;
	}
	
	/**
	 * Create main tab controls
	 * @param composite
	 */
	public abstract void createMainTabControl(Composite composite);
	
	/**
	 * Called if enabled changed
	 * @param selected
	 */
	public abstract void enabledChanged(boolean selected);
	
	/**
	 * Returns the rename rules
	 * @return
	 */
	public abstract IRenameRule[] getRenameRules();
	
	/**
	 * Returns a description
	 * @return
	 */
	public abstract String getDescription();
	
	/**
	 * Returns the title
	 * @return
	 */
	public abstract String getTitle();
	
	/**
	 * Resets this tab view
	 */
	public abstract void reset();
	
	public void dispose() {
		this.descriptionLabel.dispose();
		this.enabled.dispose();
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled.getSelection();
	}
	
	/**
	 * @param b the enabled
	 */
	public void setEnabled(boolean b) {
		if (enabled.getSelection() != b) {
			enabled.setSelection(b);
			
			handleEnabledChanged(b);
		}
	}
	
	/**
	 * Called if the enabled attribute is changed 
	 * @param selected
	 */
	protected void handleEnabledChanged(boolean selected) {
		enabledChanged(selected);
		if (selected) {
			tabItem.setText("*" + originalTabItemText);
			enabledTabCount ++;
		}
		else {
			tabItem.setText(originalTabItemText);
			enabledTabCount --;
		}
		
		// fire event
		ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		RenameFilesViewSourceProvider sourceProvider = (RenameFilesViewSourceProvider) sourceProviderService.getSourceProvider(RenameFilesViewSourceProvider.RENAME_TAB_ENABLED_STATE);
		if (enabledTabCount == 0) {
			sourceProvider.changeState(RenameFilesViewSourceProvider.RENAME_TAB_ENABLED_STATE, Boolean.FALSE);
		}
		else if (enabledTabCount > 0) {
			sourceProvider.changeState(RenameFilesViewSourceProvider.RENAME_TAB_ENABLED_STATE, Boolean.TRUE);
		}
		else if (enabledTabCount < 0) {
			sourceProvider.changeState(RenameFilesViewSourceProvider.RENAME_TAB_ENABLED_STATE, Boolean.FALSE);
			enabledTabCount = 0;
			logger.error("Enabled tab count is negative");
		}
	}
}
