package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;

public class VariableNameContentProposalProvider extends SimpleContentProposalProvider {
	private List<String> variableNames;
	
	public VariableNameContentProposalProvider(int variableCount) {
		super(new String[0]);
		this.variableNames = new ArrayList<String>();
		for (int i = 1; i <= variableCount; ++ i) {
			String variableName = "<" + i + ">";
			variableNames.add(variableName);
		}
		setProposals(variableNames.toArray(new String[0]));
		setFiltering(false);
	}
	
	public void addToken(String token) {
		variableNames.add(0, token);
		setProposals(variableNames.toArray(new String[0]));
	}
	
	public String getToken(int index) {
		return variableNames.get(index);
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
