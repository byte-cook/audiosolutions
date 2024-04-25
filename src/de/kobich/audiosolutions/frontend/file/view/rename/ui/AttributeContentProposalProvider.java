package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;

import de.kobich.audiosolutions.core.service.descriptor.RenameFileDescriptorAttributeType;

public class AttributeContentProposalProvider extends SimpleContentProposalProvider {
	private List<String> variableNames;
	
	public AttributeContentProposalProvider(int variableCount) {
		super(new String[0]);
		this.variableNames = new ArrayList<String>();
		this.variableNames.addAll(RenameFileDescriptorAttributeType.getNames());
		Collections.sort(this.variableNames);
		setProposals(variableNames.toArray(new String[0]));
		setFiltering(false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.fieldassist.SimpleContentProposalProvider#getProposals(java.lang.String, int)
	 */
	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		String prefix = contents;
		int startIndex = prefix.lastIndexOf("<");
		int endIndex = prefix.lastIndexOf(">");
		if (startIndex > endIndex) {
			prefix = contents.substring(startIndex);
			
			List<IContentProposal> contentProposals = new ArrayList<IContentProposal>();
			for (String proposal : variableNames) {
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
