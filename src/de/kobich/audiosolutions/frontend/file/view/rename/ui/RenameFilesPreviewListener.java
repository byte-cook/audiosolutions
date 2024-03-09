package de.kobich.audiosolutions.frontend.file.view.rename.ui;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.IHandlerService;

import de.kobich.audiosolutions.frontend.file.view.rename.RenameFilesView;
import de.kobich.audiosolutions.frontend.file.view.rename.action.RenamePreviewAction;
import de.kobich.audiosolutions.frontend.file.view.rename.action.ResetPreviewAction;
import de.kobich.commons.misc.rename.rule.IRenameRule;

/**
 * Updates preview after modifications.
 */
public class RenameFilesPreviewListener extends SelectionAdapter implements ModifyListener {
	private static final Logger logger = Logger.getLogger(RenameFilesPreviewListener.class);
	private final RenameFilesView view;
	private final IHandlerService handlerService;
//	private final DelayListener<TypedEvent> delayListener;
	
	/**
	 * Constructor
	 * @param view
	 */
	public RenameFilesPreviewListener(final RenameFilesView view) {
		this.view = view;
		this.handlerService = (IHandlerService) view.getSite().getService(IHandlerService.class);
		
		// do not use delay listener
		/*
		try {
//			final Command command = ((ICommandService) view.getSite().getService(ICommandService.class)).getCommand(RenamePreviewAction.ID);
//			IParameter background = command.getParameter(RenamePreviewAction.PARAM_BACKGROUND);
//			Parameterization[] parameters = new Parameterization[1];
//			parameters[0] = new Parameterization(background, Boolean.TRUE.toString());
//			final ParameterizedCommand pCommand = new ParameterizedCommand(command, parameters);
			
			// use delaylistener 
			this.delayListener = new DelayListener<TypedEvent>(-250, TimeUnit.MILLISECONDS) {
				@Override
				public void handleEvent(List<TypedEvent> events) {
					updatePreview();
				}
			};
		}
		catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		*/
	}

	@Override
	public void modifyText(ModifyEvent e) {
//		if (delayListener != null) {
//			this.delayListener.delayEvent(e);
//		}
		updatePreview();
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
//		if (delayListener != null) {
//			this.delayListener.delayEvent(e);
//		}
		updatePreview();
	}

	public void updatePreview() {
		if (!view.getSite().getShell().isVisible()) {
			// do nothing if view is not visible (at startup)
			return;
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					List<IRenameRule> renameRules = view.getRenameRules();
					if (renameRules.isEmpty()) {
						handlerService.executeCommand(ResetPreviewAction.ID, null);
					}
					else {
						handlerService.executeCommand(RenamePreviewAction.ID, null);
					}
				}
				catch (NotHandledException e) {
					// ignore if no handler can be found which can occur if handler is not active
					logger.warn("Cannot update preview: " + e.getMessage());
				}
				catch (Exception e) {
					// sometimes happens a NullPointerExc, reason is probably a bug in executeCommand()
					logger.warn("Cannot update preview: " + e.getMessage());
				}
			}
		});
	}
	
}
