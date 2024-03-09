package de.kobich.audiosolutions.frontend.common.selection;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Can be used to get only post selection events.
 */
public class PostSelectionAdapter implements Listener {
	private static int TIME = 500;
	private final Display display;
	private final int[] count = new int[1];
	
	public PostSelectionAdapter(Display display) {
		this.display = display;
	}

	@Override
	public void handleEvent(final Event event) {
		count[0]++;
//		System.out.println("count:" +count[0]);
		final int id = count[0];
		display.timerExec(TIME, new Runnable() {
            public void run() {
                if (id == count[0]) {
//                	System.out.println("postcount:" +count[0]);
                	count[0] = 0;
                	handlePostEvent(new SelectionEvent(event));
                }
            }
        });
	}
	public void handlePostEvent(SelectionEvent event) {
	}
}
