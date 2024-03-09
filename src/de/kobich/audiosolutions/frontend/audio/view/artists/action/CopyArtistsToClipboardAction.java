package de.kobich.audiosolutions.frontend.audio.view.artists.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kobich.audiosolutions.frontend.audio.view.artists.ArtistsView;
import de.kobich.audiosolutions.frontend.audio.view.artists.model.ArtistItem;
import de.kobich.audiosolutions.frontend.common.ui.ProgressDialog;
import de.kobich.commons.ui.jface.StatusLineUtils;

public class CopyArtistsToClipboardAction extends AbstractHandler {
	private static final String NEW_LINE = System.getProperty("line.separator"); 
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			ArtistsView view = (ArtistsView) window.getActivePage().findView(ArtistsView.ID);
			if (view == null) {
				return null;
			}
			
			ProgressDialog progressDialog = new ProgressDialog(view.getSite().getShell());
			RunnableWithProgress progressRunnable = new RunnableWithProgress();
			progressRunnable.artists = view.getArtistItems();
			progressRunnable.window = window;
			progressRunnable.viewPart = view;
			progressDialog.run(true, false, progressRunnable);
		}
		catch (Exception e) {
			String msg = "Error while searching tracks";
			MessageDialog.openError(window.getShell(), "Artists", msg + e.getMessage());
		}
		return null;
	}
	
	/**
	 * RunnableWithProgress
	 */
	private class RunnableWithProgress implements IRunnableWithProgress {
		public IWorkbenchWindow window;
		public IViewPart viewPart;
		public Set<ArtistItem> artists;
		
		public void run(IProgressMonitor monitor) {
			// sort artist names
			List<String> artistNames = new ArrayList<String>();
			for (ArtistItem item : artists) {
				artistNames.add(item.getArtist().getName());
			}
			Collections.sort(artistNames);
			// get clipboard content
			StringBuilder sb = new StringBuilder();
			for (String name : artistNames) {
				if (!sb.toString().isEmpty()) {
					sb.append(NEW_LINE);
				}
				sb.append(name);
			}
			final String content = sb.toString();

			if (StringUtils.isNotEmpty(content)) {
				// copy to clipboard
				window.getShell().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						Clipboard cb = new Clipboard(Display.getDefault());
						TextTransfer textTransfer = TextTransfer.getInstance();
						cb.setContents(new Object[] { content }, new Transfer[] { textTransfer });
						
						String msg = "Copy to clipboard succeeded.";
						StatusLineUtils.setStatusLineMessage(viewPart, msg, false);
					}
				});
			}
			else {
				window.getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						String msg = "No data available to copy into clipboard.";
						MessageDialog.openError(window.getShell(), "Clipboard", msg);
					}
				});
			}
		}
	}
}
