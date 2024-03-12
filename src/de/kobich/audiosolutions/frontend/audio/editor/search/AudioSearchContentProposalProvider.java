package de.kobich.audiosolutions.frontend.audio.editor.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;

import de.kobich.audiosolutions.core.service.search.AudioTextSearchToken.SearchTokenType;

public class AudioSearchContentProposalProvider extends SimpleContentProposalProvider {
	private final List<String> proposals;
	
	public AudioSearchContentProposalProvider() {
		super(new String[0]);
		this.proposals = new ArrayList<String>();
		SearchTokenType[] variables =  SearchTokenType.values();
		Arrays.sort(variables, (v1, v2) -> v1.getKeyWord().compareTo(v2.getKeyWord()));
		for (SearchTokenType variable : variables) {
			if (SearchTokenType.UNDEFINED.equals(variable)) {
				continue;
			}
			proposals.add(variable.getKeyWord() + ": ");
		}
		setProposals(proposals.toArray(new String[0]));
		setFiltering(false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.fieldassist.SimpleContentProposalProvider#getProposals(java.lang.String, int)
	 */
	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		String prefix = contents.substring(0, position);
		int prefixIndex = prefix.lastIndexOf(" ");
		if (prefixIndex != -1) {
			prefix = prefix.substring(prefixIndex).trim();
		}
		System.out.println(prefix);
		
		if (StringUtils.isNotBlank(prefix)) {
			List<IContentProposal> contentProposals = new ArrayList<IContentProposal>();
			for (String proposal : proposals) {
				if (proposal.startsWith(prefix)) {
					contentProposals.add(new ContentProposal(proposal.substring(prefix.length()), proposal, null));
				}
			}
			return contentProposals.toArray(new IContentProposal[contentProposals.size()]);
		}
		else {
			return super.getProposals(contents, position);
		}
	}
}
