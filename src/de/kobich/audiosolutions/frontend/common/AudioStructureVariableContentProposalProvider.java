package de.kobich.audiosolutions.frontend.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;

import de.kobich.audiosolutions.core.service.AudioAttribute2StructureVariableMapper;
import de.kobich.commons.misc.extract.StructureVariable;

public class AudioStructureVariableContentProposalProvider extends SimpleContentProposalProvider {
	private final List<String> proposals;
	
	public AudioStructureVariableContentProposalProvider() {
		super(new String[0]);
		this.proposals = new ArrayList<String>();
		AudioAttribute2StructureVariableMapper mapper = AudioAttribute2StructureVariableMapper.getInstance();
		StructureVariable[] variables = mapper.getVariables().toArray(new StructureVariable[0]);
		Arrays.sort(variables, mapper.getVariableComparator());
		for (StructureVariable variable : variables) {
			proposals.add(variable.getName());
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
		int prefixIndex = prefix.lastIndexOf("<");
		int postfixIndex = prefix.lastIndexOf(">");
		if (prefixIndex != -1) {
			prefix = prefix.substring(prefixIndex);
		}
		
		if (prefixIndex > postfixIndex) {
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
