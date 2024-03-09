package de.kobich.audiosolutions.frontend.audio;

import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;

public class AudioDataContentProposalProvider extends SimpleContentProposalProvider {
	private static final int MAX_PROPOSALS = 5;
	private final AudioAttribute attribute;
	private final AudioSearchService searchService;

	public AudioDataContentProposalProvider(AudioAttribute attribute) {
		super(new String[0]);
		this.attribute = attribute;
		this.searchService = AudioSolutions.getService(AudioSearchService.class);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.fieldassist.SimpleContentProposalProvider#getProposals(java.lang.String, int)
	 */
	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		try {
			List<String> proposals = searchService.searchProposals(attribute, contents, MAX_PROPOSALS);
			if (proposals != null) {
				super.setProposals(proposals.toArray(new String[0]));
			}
		} catch (Exception e) { e.printStackTrace(); }
		return super.getProposals(contents, position);
	}
}
