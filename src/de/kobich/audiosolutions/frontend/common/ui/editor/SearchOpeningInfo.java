package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import de.kobich.audiosolutions.core.service.search.AudioSearchQuery;
import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor.CollectionEditorType;


public class SearchOpeningInfo implements IOpeningInfo {
	private static final long serialVersionUID = -136426121121508129L;
	private final String searchText;
	private StandardSearch standardSearch;
	private ArtistSearch artistSearch;
	private MediumSearch mediumSearch;
	
	public SearchOpeningInfo(StandardSearch standardSearch) {
		this.standardSearch = standardSearch;
		this.searchText = standardSearch.searchText;
	}
	
	public SearchOpeningInfo(ArtistSearch artistSearch) {
		this.artistSearch = artistSearch;
		this.searchText = artistSearch.searchText;
	}

	public SearchOpeningInfo(MediumSearch mediumSearch) {
		this.mediumSearch = mediumSearch;
		this.searchText = mediumSearch.searchText;
	}

	/**
	 * @return the directory
	 */
	public String getSearchText() {
		return searchText;
	}

	@Override
	public String getName() {
		return getSearchText();
	}

	@Override
	public CollectionEditorType getEditorType() {
		return CollectionEditorType.SEARCH;
	}
	
	public StandardSearch getStandardSearch() {
		return standardSearch;
	}

	public ArtistSearch getArtistSearch() {
		return artistSearch;
	}

	public MediumSearch getMediumSearch() {
		return mediumSearch;
	}

	public static class StandardSearch {
		public final AudioSearchQuery query;
		public final String searchText;
		
		public StandardSearch(AudioSearchQuery query) {
			this.query = query;
			
			StringBuilder sb = new StringBuilder();
			String[] texts = {query.getTrackName(), query.getMediumName(), query.getArtistName(), query.getGenreName(), query.getAlbumName()};
			for (String text : texts) {
				if (StringUtils.isNotEmpty(text)) {
					sb.append("<" + text + "> ");
				}
			}
			if (sb.toString().isEmpty()) {
				sb.append("<>");
			}
			this.searchText = sb.toString();
		}
		
	}
	
	public static class ArtistSearch {
		public final Set<String> artistNames;
		public final String searchText;

		public ArtistSearch(Set<String> artistNames) {
			this.artistNames = artistNames;
			
			StringBuilder sb = new StringBuilder();
			for (String text : this.artistNames) {
				if (StringUtils.isNotEmpty(text)) {
					sb.append("<" + text + "> ");
				}
			}
			this.searchText = sb.toString();
		}
	}
	
	public static class MediumSearch {
		public final Set<String> mediumNames;
		public final String searchText;

		public MediumSearch(Set<String> mediumNames) {
			this.mediumNames = mediumNames;
			
			StringBuilder sb = new StringBuilder();
			for (String text : this.mediumNames) {
				if (StringUtils.isNotEmpty(text)) {
					sb.append("<" + text + "> ");
				}
			}
			this.searchText = sb.toString();
		}
	}
}
