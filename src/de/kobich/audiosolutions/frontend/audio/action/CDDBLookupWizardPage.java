package de.kobich.audiosolutions.frontend.audio.action;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.cddb.AudioCDDBService;
import de.kobich.audiosolutions.core.service.cddb.AudioCDDBService.SearchDepth;
import de.kobich.audiosolutions.core.service.cddb.ICDDBRelease;
import de.kobich.audiosolutions.frontend.audio.AudioDataContentProposalProvider;
import de.kobich.audiosolutions.frontend.common.proxy.RCPProxyProvider;
import de.kobich.audiosolutions.frontend.common.util.DecoratorUtils;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.net.IProxyProvider;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.memento.ComboSerializer;
import de.kobich.commons.ui.jface.memento.DialogSettingsAdapter;
import de.kobich.commons.ui.jface.table.ViewerColumn;
import de.kobich.commons.ui.jface.table.ViewerColumnManager;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializable;

public class CDDBLookupWizardPage extends WizardPage implements Listener, IMementoItemSerializable {
	private static final Logger logger = Logger.getLogger(CDDBLookupWizardPage.class);
	public static final ViewerColumn COLUMN_RELEASE = new ViewerColumn("Release", 35);
	public static final ViewerColumn COLUMN_ARTIST = new ViewerColumn("Artist", 35);
	public static final ViewerColumn COLUMN_PUBLICATION = new ViewerColumn("Publication", 15);
	public static final ViewerColumn COLUMN_TRACK_COUNT = new ViewerColumn("Track Count", 15);
	public static final ViewerColumnManager COLUMNS = new ViewerColumnManager(COLUMN_RELEASE, COLUMN_ARTIST, COLUMN_PUBLICATION, COLUMN_TRACK_COUNT);
	private static final String STATE_ARTIST = "artist";
	private static final String STATE_ALBUM = "album";
	private IDialogSettings dialogSettings;
	private Combo artistCombo;
	private Combo albumCombo;
	private Button lookupButton;
	private TableViewer albumListViewer;

	public CDDBLookupWizardPage() {
		super("CDDBLookup");
		super.setTitle("Lookup Audio Data");
		super.setMessage("Search for audio data by using the service of MusicBrainz");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		// search
		Composite searchComposite = new Composite(composite, SWT.NONE);
		searchComposite.setLayout(new GridLayout(2, false));
		searchComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		TraverseListener returnListener = new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					lookupCDDB();
				}
			}
		};
		// artist
		Label artistLabel = new Label(searchComposite, SWT.NONE);
		artistLabel.setText("Artist:");
		artistCombo = new Combo(searchComposite, SWT.BORDER);
		artistCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		artistCombo.addTraverseListener(returnListener);
		KeyStroke artistKeyStroke = null;
		try {
			artistKeyStroke = KeyStroke.getInstance("Ctrl+Space");
			DecoratorUtils.createDecorator(artistCombo, "Press Ctrl+Space to see proposals", FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		}
		catch (ParseException exc) {
			logger.warn("Key stroke cannot be created", exc);
		}
		IContentProposalProvider artistProposalProvider = new AudioDataContentProposalProvider(AudioAttribute.ARTIST);
		ContentProposalAdapter artistAdapter = new ContentProposalAdapter(artistCombo, new ComboContentAdapter(), artistProposalProvider, artistKeyStroke, null);
		artistAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		// album
		Label albumLabel = new Label(searchComposite, SWT.NONE);
		albumLabel.setText("Album:");
		albumCombo = new Combo(searchComposite, SWT.BORDER);
		albumCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		albumCombo.addTraverseListener(returnListener);
		new Label(searchComposite, SWT.NONE);
		lookupButton = new Button(searchComposite, SWT.NONE);
		lookupButton.setText("CDDB Lookup");
		lookupButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				lookupCDDB();
			}
		});
		KeyStroke albumKeyStroke = null;
		try {
			albumKeyStroke = KeyStroke.getInstance("Ctrl+Space");
			DecoratorUtils.createDecorator(albumCombo, "Press Ctrl+Space to see proposals", FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		}
		catch (ParseException exc) {
			logger.warn("Key stroke cannot be created", exc);
		}
		IContentProposalProvider albumProposalProvider = new AudioDataContentProposalProvider(AudioAttribute.ALBUM);
		ContentProposalAdapter albumAapter = new ContentProposalAdapter(albumCombo, new ComboContentAdapter(), albumProposalProvider, albumKeyStroke, null);
		albumAapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		
		// album list
		Composite resultComposite = new Composite(composite, SWT.NONE);
		resultComposite.setLayout(new GridLayout());
		resultComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		JFaceUtils.createHorizontalSeparator(resultComposite, 2);
		Label selectLabel = new Label(resultComposite, SWT.NONE);
		selectLabel.setText("Select a release:");
		selectLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite tableComposite = new Composite(resultComposite, SWT.NONE);
		tableComposite.setLayout(new GridLayout());
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 150;
		tableComposite.setLayoutData(gd);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		albumListViewer = new TableViewer(tableComposite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		for (final ViewerColumn column : COLUMNS) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(albumListViewer, SWT.NONE, COLUMNS.indexOf(column));
			tableColumnLayout.setColumnData(viewerColumn.getColumn(), new ColumnWeightData(column.getWidthPercent(), column.getMinimumWidth()));
			viewerColumn.getColumn().setText(column.getName());
		}
		Table table = albumListViewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		albumListViewer.setContentProvider(ArrayContentProvider.getInstance());
		albumListViewer.setLabelProvider(new AlbumListLabelProvider());
		table.addListener(SWT.Selection, this);
		
		restoreState();
		setPageComplete(false);
		super.setControl(composite);
	}

	@Override
	public void handleEvent(Event event) {
		boolean albumSelected = !albumListViewer.getSelection().isEmpty();
		setPageComplete(albumSelected);
		
		getWizard().getContainer().updateButtons();
	}
	
	private void lookupCDDB() {
		setErrorMessage(null);
		
		List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
		JFaceThreadRunner runner = new JFaceThreadRunner("Lookup CDDB", getShell(), states) {
			private String artistName;
			private String albumName;
			private List<ICDDBRelease> releases;
			
			@Override
			protected void run(RunningState state) throws Exception {
				switch (state) {
				case UI_1:
					this.artistName = artistCombo.getText();
					this.albumName = albumCombo.getText();
					break;
				case WORKER_1:
					IServiceProgressMonitor progressMonitor = super.getProgressMonitor();
					
					// run search
					AudioCDDBService cddbService = AudioSolutions.getService(AudioCDDBService.class);
					IProxyProvider proxyProvider = new RCPProxyProvider();
					this.releases = cddbService.search(artistName, albumName, SearchDepth.MEDIUM, proxyProvider, progressMonitor);
					break;
				case UI_2:
					// refresh viewer
					albumListViewer.setInput(releases);
					if (releases.isEmpty()) {
						setErrorMessage("No Audio Data found. Please change your search parameters and internet connection.");
					}
					else {
						setMessage(releases.size() + " album releases found");
					}
					break;
				case UI_ERROR:
					if (super.getProgressMonitor().isCanceled()) {
						return;
					}
					Exception e = super.getException();
					logger.error(e.getMessage(), e);
					setErrorMessage(e.getMessage());
					break;
				default: 
					break;
				}
			}
		};
		runner.runProgressMonitorDialog(true, true);
	}

	public ICDDBRelease getSelectedRelease() {
		ISelection selection = albumListViewer.getSelection();
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		for (Object obj : structuredSelection.toList()) {
			if (obj instanceof ICDDBRelease) {
				return (ICDDBRelease) obj;
			}
		}
		return null;
	}
	
	private static class AlbumListLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof ICDDBRelease) {
				ICDDBRelease release = (ICDDBRelease) element;
				ViewerColumn colum = COLUMNS.getByIndex(columnIndex);
				if (COLUMN_RELEASE.equals(colum)) {
					return release.getAlbum();
				}
				else if (COLUMN_ARTIST.equals(colum)) {
					return release.getArtist();
				}
				else if (COLUMN_PUBLICATION.equals(colum)) {
					return release.getPublication();
				}
				else if (COLUMN_TRACK_COUNT.equals(colum)) {
					return "" + release.getTrackCount();
				}
			}
			return null;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	
	public void setDialogBoundsSettings(IDialogSettings dialogSettings) {
		this.dialogSettings = dialogSettings;
	}

	@Override
	public void saveState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(dialogSettings);
		ComboSerializer artistMemento = new ComboSerializer(STATE_ARTIST, "");
		artistMemento.save(artistCombo, mementoItem);
		ComboSerializer albumMemento = new ComboSerializer(STATE_ALBUM, "");
		albumMemento.save(albumCombo, mementoItem);
	}

	@Override
	public void restoreState() {
		IMementoItem mementoItem = new DialogSettingsAdapter(dialogSettings);
		ComboSerializer artistMemento = new ComboSerializer(STATE_ARTIST, "");
		artistMemento.restore(artistCombo, mementoItem);
		ComboSerializer albumMemento = new ComboSerializer(STATE_ALBUM, "");
		albumMemento.restore(albumCombo, mementoItem);
	}

}
