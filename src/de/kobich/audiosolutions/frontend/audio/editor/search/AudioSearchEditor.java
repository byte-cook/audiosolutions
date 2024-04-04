package de.kobich.audiosolutions.frontend.audio.editor.search;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.persist.domain.Track;
import de.kobich.audiosolutions.core.service.search.AudioSearchQuery;
import de.kobich.audiosolutions.core.service.search.AudioTextSearchService;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import de.kobich.audiosolutions.frontend.audio.editor.search.action.AddSearchResultsToPlaylistSelectionAdapter;
import de.kobich.audiosolutions.frontend.audio.editor.search.action.DoSearchHyperlinkAdapter;
import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.audiosolutions.frontend.common.ui.editor.AbstractScrolledFormEditor;
import de.kobich.audiosolutions.frontend.common.util.DecoratorUtils;
import de.kobich.commons.ui.DelayListener;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.listener.DummySelectionProvider;

public class AudioSearchEditor extends AbstractScrolledFormEditor {
	public static final String ID = "de.kobich.audiosolutions.editor.audioSearchEditor";
	private static final Logger logger = Logger.getLogger(AudioSearchEditor.class);
	private static final Point SIZE_16 = new Point(16, 16);
	private AudioSearchEditorEventListener eventListener;
	private FormToolkit toolkit;
	private Form form;
	private ScrolledForm scrolledForm;
	private Text searchText;
	private Composite artistsComposite;
	private Composite albumsComposite;
	private Composite tracksComposite;
	private UUID currentSearchingId;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName("Search");
		
		this.eventListener = new AudioSearchEditorEventListener(this);
		this.eventListener.register();
	}

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		GridLayout gl = new GridLayout(1, true);
		gl.marginHeight = 0;
		form.getBody().setLayout(gl);
		
		DelayListener<TypedEvent> delayListener = new DelayListener<>(750, TimeUnit.MILLISECONDS) {
			@Override
			public void handleEvent(List<TypedEvent> events) {
				AudioSearchEditor.this.startSearch();
			}
		};

		// search
		Composite searchComposite = this.toolkit.createComposite(form.getBody(), SWT.NONE);
		GridLayout informationCompositeGridLayout = new GridLayout();
		informationCompositeGridLayout.marginHeight = 20;
		informationCompositeGridLayout.numColumns = 2;
		searchComposite.setLayout(informationCompositeGridLayout);
		searchComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// -- logo
		Image logoImage = Activator.getDefault().getImage(ImageKey.AUDIO_SEARCH_EDITOR);
		Label logoLabel = super.createLogo(this.toolkit, searchComposite, new Point(32, 32));
		logoLabel.setImage(logoImage);
		// -- search text
		searchText = new Text(searchComposite, SWT.BORDER | SWT.SEARCH);
		searchText.setMessage("Input your search text");
		searchText.setLayoutData(JFaceUtils.createGridDataWithSpan(GridData.FILL_HORIZONTAL, 1, 1));
		searchText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				delayListener.delayEvent(e);
			}
		});
		searchText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					searchText.setText("");
					delayListener.delayEvent(e);
				}
			}
		});
		IContentProposalProvider proposalProvider = new AudioSearchContentProposalProvider();
		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
			DecoratorUtils.createDecorator(searchText, "Press Ctrl+Space to see proposals", FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		}
		catch (ParseException exc) {
			logger.warn("Key stroke cannot be created", exc);
		}
		ContentProposalAdapter adapter = new ContentProposalAdapter(searchText, new TextContentAdapter(), proposalProvider, keyStroke, null);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);

		// scroll
		Composite scrollSection = new Composite(form.getBody(), SWT.NONE);
		scrollSection.setLayoutData(new GridData(GridData.FILL_BOTH));
		scrollSection.setLayout(new FillLayout());
		this.scrolledForm = toolkit.createScrolledForm(scrollSection);
		scrolledForm.getBody().setLayout(new TableWrapLayout());

		// results
		Composite body = scrolledForm.getBody();
		artistsComposite = super.createTableWrapSection(this.toolkit, body, "Artists", 3, Section.TITLE_BAR | Section.EXPANDED);
		albumsComposite = super.createTableWrapSection(this.toolkit, body, "Albums", 4, Section.TITLE_BAR | Section.EXPANDED);
		tracksComposite = super.createTableWrapSection(this.toolkit, body, "Tracks", 6, Section.TITLE_BAR | Section.EXPANDED);
		
		SelectionSupport.INSTANCE.registerEditor(this, DummySelectionProvider.INSTANCE);
		startSearch();
	}
	
	public void startSearch() {
		Display.getDefault().asyncExec(() -> {
			// use an id to make sure that only the results of the last jobs are visible 
			currentSearchingId = UUID.randomUUID();
			setResultsAsLoading();
			
			SearchArtistRunner artistRunner = new SearchArtistRunner(this, searchText.getText(), currentSearchingId);
			artistRunner.runBackgroundJob(0, false, true, null);
			SearchAlbumRunner albumRunner = new SearchAlbumRunner(AudioSearchEditor.this, searchText.getText(), currentSearchingId);
			albumRunner.runBackgroundJob(0, false, true, null);
			SearchTrackRunner trackRunner = new SearchTrackRunner(AudioSearchEditor.this, searchText.getText(), currentSearchingId);
			trackRunner.runBackgroundJob(0, false, true, null);
		});
	}
	
	private void setResultsAsLoading() {
		// artists
		for (Control c : artistsComposite.getChildren()) {
			c.dispose();
		}
		StyledText artistLoading = AudioSearchEditor.this.createStyledText(artistsComposite, false);
		artistLoading.setText("Searching for artists...");
		artistsComposite.layout();
		
		// albums
		for (Control c : albumsComposite.getChildren()) {
			c.dispose();
		}
		StyledText albumLoading = AudioSearchEditor.this.createStyledText(albumsComposite, false);
		albumLoading.setText("Searching for albums...");
		albumsComposite.layout();
		
		// tracks
		for (Control c : tracksComposite.getChildren()) {
			c.dispose();
		}
		StyledText trackLoading = AudioSearchEditor.this.createStyledText(tracksComposite, false);
		trackLoading.setText("Searching for tracks...");
		tracksComposite.layout();
		scrolledForm.reflow(true);
	}
	
	@Override
	public void setFocus() {
		searchText.setFocus();
	}
	
	@Override
	public boolean isDirty() {
		return false;
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}
	
	@Override
	public void dispose() {
		this.toolkit.dispose();
		this.form.dispose();
		this.scrolledForm.dispose();
		this.eventListener.deregister();
		this.searchText.dispose();
		this.artistsComposite.dispose();
		this.albumsComposite.dispose();
		this.tracksComposite.dispose();
		super.dispose();
	}
	
	private static class SearchArtistRunner extends JFaceThreadRunner {
		private final AudioSearchEditor editor;
		private final String searchText;
		private final UUID id;
		private List<Artist> artists;
		
		public SearchArtistRunner(AudioSearchEditor editor, String searchText, UUID id) {
			super("Searching for artists", editor.getSite().getShell(), List.of(RunningState.WORKER_1, RunningState.UI_1));
			this.editor = editor;
			this.searchText = searchText;
			this.id = id;
		}

		@Override
		protected void run(RunningState state) throws Exception {
			switch (state) {
			case WORKER_1:
				AudioTextSearchService searchService = AudioSolutions.getService(AudioTextSearchService.class);
				artists = searchService.searchArtists(searchText, 20, null);
				break;
			case UI_1:
				this.editor.updateArtists(artists, this.id);
				break;
			case UI_ERROR:
				logger.info(super.getException().getMessage(), super.getException());
				this.editor.updateArtists(List.of(), this.id);
				break;
			default:
				break;
			}
		}
		
	}

	private void updateArtists(final List<Artist> artists, UUID id) {
		if (!id.equals(currentSearchingId)) {
			return;
		}
		logger.info("Found artists: " + artists);
		// artists
		for (Control c : artistsComposite.getChildren()) {
			c.dispose();
		}
		for (Artist artist : artists) {
			Image artistImage = Activator.getDefault().getImage(ImageKey.ARTIST);
			Label albumCoverLabel = createLogo(AudioSearchEditor.this.toolkit, artistsComposite, SIZE_16);
			albumCoverLabel.setImage(artistImage);

			Hyperlink link = AudioSearchEditor.this.toolkit.createHyperlink(artistsComposite, artist.getName(), SWT.WRAP);
			link.addHyperlinkListener(new DoSearchHyperlinkAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().artistId(artist.getId()).artistName(artist.getName()).build()));
			link.setLayoutData(JFaceUtils.createGridDataWithWidth(SWT.NONE, 300));
			link.addMenuDetectListener(new MenuDetectListener() {
				@Override
				public void menuDetected(MenuDetectEvent e) {
					Menu menu = new Menu(link.getShell(), SWT.POP_UP);
					MenuItem item = new MenuItem(menu, SWT.NONE);
					item.setText("Add To Playlist");
					item.addSelectionListener(new AddSearchResultsToPlaylistSelectionAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().artistId(artist.getId()).artistName(artist.getName()).build()));
					menu.setVisible(true);
				}
			});
			
			Label artistDescriptionText = new Label(artistsComposite, SWT.NONE);
			artistDescriptionText.setText(StringUtils.defaultString(artist.getDescription()));
		}
		if (artistsComposite.getChildren().length == 0) {
			StyledText noResult = AudioSearchEditor.this.createStyledText(artistsComposite, false);
			noResult.setText("No artist found");
		}
		artistsComposite.layout();
		scrolledForm.reflow(true);
	}
	
	private static class SearchAlbumRunner extends JFaceThreadRunner {
		private final AudioSearchEditor editor;
		private final String searchText;
		private final UUID id;
		private List<Album> albums;
		
		public SearchAlbumRunner(AudioSearchEditor editor, String searchText, UUID id) {
			super("Searching for albums", editor.getSite().getShell(), List.of(RunningState.WORKER_1, RunningState.UI_1));
			this.editor = editor;
			this.searchText = searchText;
			this.id = id;
		}

		@Override
		protected void run(RunningState state) throws Exception {
			switch (state) {
			case WORKER_1:
				AudioTextSearchService searchService = AudioSolutions.getService(AudioTextSearchService.class);
				albums = searchService.searchAlbums(searchText, 20, null);
				break;
			case UI_1:
				this.editor.updateAlbums(albums, this.id);
				break;
			case UI_ERROR:
				logger.info(super.getException().getMessage(), super.getException());
				this.editor.updateAlbums(List.of(), this.id);
				break;
			default:
				break;
			}
		}
	}

	private void updateAlbums(final List<Album> albums, UUID id) {
		if (!id.equals(currentSearchingId)) {
			return;
		}
		logger.info("Found albums: " + albums);
		// albums
		for (Control c : albumsComposite.getChildren()) {
			c.dispose();
		}
		for (Album album : albums) {
			Image albumImage = Activator.getDefault().getImage(ImageKey.ALBUM);
			Label albumCoverLabel = createLogo(AudioSearchEditor.this.toolkit, albumsComposite, SIZE_16);
			albumCoverLabel.setImage(albumImage);
			
			Hyperlink link = AudioSearchEditor.this.toolkit.createHyperlink(albumsComposite, album.getName(), SWT.WRAP);
			link.setLayoutData(JFaceUtils.createGridDataWithWidth(SWT.NONE, 300));
			link.addHyperlinkListener(new DoSearchHyperlinkAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().albumId(album.getId()).albumName(album.getName()).build()));
			link.addMenuDetectListener(new MenuDetectListener() {
				@Override
				public void menuDetected(MenuDetectEvent e) {
					Menu menu = new Menu(link.getShell(), SWT.POP_UP);
					MenuItem item = new MenuItem(menu, SWT.NONE);
					item.setText("Add To Playlist");
					item.addSelectionListener(new AddSearchResultsToPlaylistSelectionAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().albumId(album.getId()).albumName(album.getName()).build()));
					menu.setVisible(true);
				}
			});
			
			Optional<Artist> artistOpt = album.getArtist();
			if (artistOpt.isPresent()) {
				Hyperlink artistLink = AudioSearchEditor.this.toolkit.createHyperlink(albumsComposite, artistOpt.get().getName(), SWT.WRAP);
				artistLink.setLayoutData(JFaceUtils.createGridDataWithWidth(SWT.NONE, 300));
				artistLink.addHyperlinkListener(new DoSearchHyperlinkAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().artistId(artistOpt.get().getId()).artistName(artistOpt.get().getName()).build()));
			}
			else {
				Label label = AudioSearchEditor.this.toolkit.createLabel(albumsComposite, "Various Artists");
				label.setLayoutData(JFaceUtils.createGridDataWithWidth(SWT.NONE, 300));
			}

			Hyperlink mediumLink = AudioSearchEditor.this.toolkit.createHyperlink(albumsComposite, album.getMedium().getName(), SWT.WRAP);
			mediumLink.addHyperlinkListener(new DoSearchHyperlinkAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().mediumId(album.getMedium().getId()).mediumName(album.getMedium().getName()).build()));

		}
		if (albumsComposite.getChildren().length == 0) {
			StyledText noResult = AudioSearchEditor.this.createStyledText(albumsComposite, false);
			noResult.setText("No album found");
		}
		albumsComposite.layout();
		scrolledForm.reflow(true);
	}
	
	private static class SearchTrackRunner extends JFaceThreadRunner {
		private final AudioSearchEditor editor;
		private final String searchText;
		private final UUID id;
		private List<Track> tracks;
		
		public SearchTrackRunner(AudioSearchEditor editor, String searchText, UUID id) {
			super("Searching for tracks", editor.getSite().getShell(), List.of(RunningState.WORKER_1, RunningState.UI_1));
			this.editor = editor;
			this.searchText = searchText;
			this.id = id;
		}

		@Override
		protected void run(RunningState state) throws Exception {
			switch (state) {
			case WORKER_1:
				AudioTextSearchService searchService = AudioSolutions.getService(AudioTextSearchService.class);
				tracks = searchService.searchTracks(searchText, 20, null);
				break;
			case UI_1:
				this.editor.updateTracks(tracks, this.id);
				break;
			case UI_ERROR:
				logger.info(super.getException().getMessage(), super.getException());
				this.editor.updateTracks(List.of(), this.id);
				break;
			default:
				break;
			}
		}
	}
	
	private void updateTracks(final List<Track> tracks, UUID id) {
		if (!id.equals(currentSearchingId)) {
			return;
		}
		logger.info("Found tracks: " + tracks);
		// tracks
		for (Control c : tracksComposite.getChildren()) {
			c.dispose();
		}
		for (Track track : tracks) {
			Image audioFileImage = Activator.getDefault().getImage(ImageKey.AUDIO_FILE);
			Label albumCoverLabel = createLogo(AudioSearchEditor.this.toolkit, tracksComposite, SIZE_16);
			albumCoverLabel.setImage(audioFileImage);

			Hyperlink link = AudioSearchEditor.this.toolkit.createHyperlink(tracksComposite, track.getName(), SWT.WRAP);
			link.setLayoutData(JFaceUtils.createGridDataWithWidth(SWT.NONE, 300));
			link.addHyperlinkListener(new DoSearchHyperlinkAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().trackId(track.getId()).trackName(track.getName()).build()));
			link.addMenuDetectListener(new MenuDetectListener() {
				@Override
				public void menuDetected(MenuDetectEvent e) {
					Menu menu = new Menu(link.getShell(), SWT.POP_UP);
					MenuItem item = new MenuItem(menu, SWT.NONE);
					item.setText("Add To Playlist");
					item.addSelectionListener(new AddSearchResultsToPlaylistSelectionAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().trackId(track.getId()).trackName(track.getName()).build()));
					menu.setVisible(true);
				}
			});
			
			Hyperlink artistLink = AudioSearchEditor.this.toolkit.createHyperlink(tracksComposite, track.getArtist().getName(), SWT.WRAP);
			artistLink.setLayoutData(JFaceUtils.createGridDataWithWidth(SWT.NONE, 300));
			artistLink.addHyperlinkListener(new DoSearchHyperlinkAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().artistId(track.getArtist().getId()).artistName(track.getArtist().getName()).build()));
			
			Hyperlink albumLink = AudioSearchEditor.this.toolkit.createHyperlink(tracksComposite, track.getAlbum().getName(), SWT.WRAP);
			albumLink.setLayoutData(JFaceUtils.createGridDataWithWidth(SWT.NONE, 300));
			albumLink.addHyperlinkListener(new DoSearchHyperlinkAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().albumId(track.getAlbum().getId()).albumName(track.getAlbum().getName()).build()));

			Hyperlink genreLink = AudioSearchEditor.this.toolkit.createHyperlink(tracksComposite, track.getGenre().getName(), SWT.WRAP);
			genreLink.setLayoutData(JFaceUtils.createGridDataWithWidth(SWT.NONE, 200));
			genreLink.addHyperlinkListener(new DoSearchHyperlinkAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().genreId(track.getGenre().getId()).genreName(track.getGenre().getName()).build()));
			
			Hyperlink mediumLink = AudioSearchEditor.this.toolkit.createHyperlink(tracksComposite, track.getAlbum().getMedium().getName(), SWT.WRAP);
			mediumLink.addHyperlinkListener(new DoSearchHyperlinkAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().mediumId(track.getAlbum().getMedium().getId()).mediumName(track.getAlbum().getMedium().getName()).build()));
		}
		if (tracksComposite.getChildren().length == 0) {
			StyledText noResult = AudioSearchEditor.this.createStyledText(tracksComposite, false);
			noResult.setText("No track found");
		}
		tracksComposite.layout();
		scrolledForm.reflow(true);
	}
}

