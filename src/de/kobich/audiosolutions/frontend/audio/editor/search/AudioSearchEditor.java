package de.kobich.audiosolutions.frontend.audio.editor.search;

import java.util.List;
import java.util.Optional;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.persist.domain.Track;
import de.kobich.audiosolutions.core.service.search.AudioSearchQuery;
import de.kobich.audiosolutions.core.service.search.AudioTextSearchService;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.Activator.ImageKey;
import de.kobich.audiosolutions.frontend.audio.editor.search.action.DoSearchHyperlinkAdapter;
import de.kobich.audiosolutions.frontend.audio.editor.search.action.OpenAllSelectionAdapter;
import de.kobich.audiosolutions.frontend.audio.editor.search.action.OpenMenuHyperlinkAdapter;
import de.kobich.audiosolutions.frontend.common.selection.SelectionSupport;
import de.kobich.audiosolutions.frontend.common.ui.editor.AbstractScrolledFormEditor;
import de.kobich.audiosolutions.frontend.common.util.DecoratorUtils;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.DelayListener;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.listener.DummySelectionProvider;

public class AudioSearchEditor extends AbstractScrolledFormEditor {
	public static final String ID = "de.kobich.audiosolutions.editor.audioSearchEditor";
	private static final Logger logger = Logger.getLogger(AudioSearchEditor.class);
	private static final int DEFAULT_MAX_RESULTS = 20;
	private AudioSearchEditorEventListener eventListener;
	private FormToolkit toolkit;
	private Form form;
	private ScrolledForm scrolledForm;
	private Text searchText;
	private Composite artistsComposite;
	private Composite albumsComposite;
	private Composite tracksComposite;

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
	
	public void startSearch() {
		Display.getDefault().asyncExec(() -> {
			setResultsAsLoading();
			
			// cancel previous jobs 
			JFaceExec.cancelJobs(this);
			startArtistJob();
			startAlbumJob();
			startTrackJob();
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
	
	private void startArtistJob() {
		final String search = searchText.getText();
		final Wrapper<List<Artist>> ARTISTS = Wrapper.empty();
		JFaceExec.builder(getSite().getShell(), "Searching for artists")
			.worker(ctx -> {
				AudioTextSearchService searchService = AudioSolutions.getService(AudioTextSearchService.class);
				ARTISTS.set(searchService.searchArtists(search, DEFAULT_MAX_RESULTS));
			})
			.ui(ctx -> updateArtists(ARTISTS.get(), search))
			.exceptionally((ctx, exc) -> {
				logger.info(exc.getMessage(), exc);
				updateArtists(List.of(), search);
				ctx.setCanceled(true);
			})
			.runBackgroundJob(0, false, true, null, this);
	}
	
	private void updateArtists(final List<Artist> artists, final String searchText) {
		logger.info("Found artists: " + artists);
		// artists
		for (Control c : artistsComposite.getChildren()) {
			c.dispose();
		}
		for (Artist artist : artists) {
			ImageHyperlink menuLink = this.toolkit.createImageHyperlink(artistsComposite, SWT.WRAP);
			menuLink.setImage(Activator.getDefault().getImage(ImageKey.OPEN_MENU));
			menuLink.addHyperlinkListener(new OpenMenuHyperlinkAdapter(menuLink.getShell(), getSite().getWorkbenchWindow(), AudioSearchQuery.builder().artistId(artist.getId()).artistName(artist.getName()).build()));

			ImageHyperlink link = this.toolkit.createImageHyperlink(artistsComposite, SWT.WRAP);
			link.setText(artist.getName());
			link.setImage(Activator.getDefault().getImage(ImageKey.ARTIST));
			link.addHyperlinkListener(new DoSearchHyperlinkAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().artistId(artist.getId()).artistName(artist.getName()).build()));
			link.setLayoutData(JFaceUtils.createGridDataWithWidth(SWT.NONE, 300));
			
			Label artistDescriptionText = new Label(artistsComposite, SWT.NONE);
			artistDescriptionText.setText(StringUtils.defaultString(artist.getDescription()));
		}
		if (!artists.isEmpty() && StringUtils.isNoneBlank(searchText)) {
			this.toolkit.createLabel(artistsComposite, "");
			
			Button openAllButton = this.toolkit.createButton(artistsComposite, "Open all artists", SWT.PUSH);
			openAllButton.addSelectionListener(new OpenAllSelectionAdapter(getSite().getWorkbenchWindow(), searchText, AudioAttribute.ARTIST));
		}
		
		if (artistsComposite.getChildren().length == 0) {
			StyledText noResult = AudioSearchEditor.this.createStyledText(artistsComposite, false);
			noResult.setText("No artist found");
		}
		artistsComposite.layout();
		scrolledForm.reflow(true);
	}
	
	private void startAlbumJob() {
		final String search = searchText.getText();
		final Wrapper<List<Album>> ALBUMS = Wrapper.empty();
		JFaceExec.builder(getSite().getShell(), "Searching for albums")
			.worker(ctx -> {
				AudioTextSearchService searchService = AudioSolutions.getService(AudioTextSearchService.class);
				ALBUMS.set(searchService.searchAlbums(search, DEFAULT_MAX_RESULTS));
			})
			.ui(ctx -> updateAlbums(ALBUMS.get(), search))
			.exceptionally((ctx, exc) -> {
				logger.info(exc.getMessage(), exc);
				updateAlbums(List.of(), search);
				ctx.setCanceled(true);
			})
			.runBackgroundJob(0, false, true, null, this);
	}

	private void updateAlbums(final List<Album> albums, final String searchText) {
		logger.info("Found albums: " + albums);
		// albums
		for (Control c : albumsComposite.getChildren()) {
			c.dispose();
		}
		for (Album album : albums) {
			ImageHyperlink menuLink = AudioSearchEditor.this.toolkit.createImageHyperlink(albumsComposite, SWT.WRAP);
			menuLink.setImage(Activator.getDefault().getImage(ImageKey.OPEN_MENU));
			menuLink.addHyperlinkListener(new OpenMenuHyperlinkAdapter(menuLink.getShell(), getSite().getWorkbenchWindow(), AudioSearchQuery.builder().albumId(album.getId()).albumName(album.getName()).build()));

			ImageHyperlink link = AudioSearchEditor.this.toolkit.createImageHyperlink(albumsComposite, SWT.WRAP);
			link.setText(album.getName());
			link.setImage(Activator.getDefault().getImage(ImageKey.ALBUM));
			link.setLayoutData(JFaceUtils.createGridDataWithWidth(SWT.NONE, 300));
			link.addHyperlinkListener(new DoSearchHyperlinkAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().albumId(album.getId()).albumName(album.getName()).build()));
			
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
		if (!albums.isEmpty() && StringUtils.isNoneBlank(searchText)) {
			this.toolkit.createLabel(albumsComposite, "");
			
			Button openAllButton = this.toolkit.createButton(albumsComposite, "Open all albums", SWT.PUSH);
			openAllButton.addSelectionListener(new OpenAllSelectionAdapter(getSite().getWorkbenchWindow(), searchText, AudioAttribute.ALBUM));
		}
		
		if (albumsComposite.getChildren().length == 0) {
			StyledText noResult = AudioSearchEditor.this.createStyledText(albumsComposite, false);
			noResult.setText("No album found");
		}
		albumsComposite.layout();
		scrolledForm.reflow(true);
	}
	
	private void startTrackJob() {
		final String search = searchText.getText();
		final Wrapper<List<Track>> TRACKS = Wrapper.empty();
		JFaceExec.builder(getSite().getShell(), "Searching for tracks")
			.worker(ctx -> {
				AudioTextSearchService searchService = AudioSolutions.getService(AudioTextSearchService.class);
				TRACKS.set(searchService.searchTracks(search, DEFAULT_MAX_RESULTS));
			})
			.ui(ctx -> updateTracks(TRACKS.get(), search))
			.exceptionally((ctx, exc) -> {
				logger.info(exc.getMessage(), exc);
				updateTracks(List.of(), search);
				ctx.setCanceled(true);
			})
			.runBackgroundJob(0, false, true, null, this);
	}
	
	private void updateTracks(final List<Track> tracks, final String searchText) {
		logger.info("Found tracks: " + tracks);
		// tracks
		for (Control c : tracksComposite.getChildren()) {
			c.dispose();
		}
		for (Track track : tracks) {
			ImageHyperlink menuLink = AudioSearchEditor.this.toolkit.createImageHyperlink(tracksComposite, SWT.WRAP);
			menuLink.setImage(Activator.getDefault().getImage(ImageKey.OPEN_MENU));
			menuLink.addHyperlinkListener(new OpenMenuHyperlinkAdapter(menuLink.getShell(), getSite().getWorkbenchWindow(), AudioSearchQuery.builder().trackId(track.getId()).trackName(track.getName()).build()));

			ImageHyperlink link = AudioSearchEditor.this.toolkit.createImageHyperlink(tracksComposite, SWT.WRAP);
			link.setText(track.getName());
			link.setImage(Activator.getDefault().getImage(ImageKey.AUDIO_FILE));
			link.setLayoutData(JFaceUtils.createGridDataWithWidth(SWT.NONE, 300));
			link.addHyperlinkListener(new DoSearchHyperlinkAdapter(getSite().getWorkbenchWindow(), AudioSearchQuery.builder().trackId(track.getId()).trackName(track.getName()).build()));
			
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
		if (!tracks.isEmpty() && StringUtils.isNoneBlank(searchText)) {
			this.toolkit.createLabel(tracksComposite, "");
			
			Button openAllButton = this.toolkit.createButton(tracksComposite, "Open all tracks", SWT.PUSH);
			openAllButton.addSelectionListener(new OpenAllSelectionAdapter(getSite().getWorkbenchWindow(), searchText, AudioAttribute.TRACK));
		}
		
		if (tracksComposite.getChildren().length == 0) {
			StyledText noResult = AudioSearchEditor.this.createStyledText(tracksComposite, false);
			noResult.setText("No track found");
		}
		tracksComposite.layout();
		scrolledForm.reflow(true);
	}
}

