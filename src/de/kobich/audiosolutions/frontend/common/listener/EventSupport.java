package de.kobich.audiosolutions.frontend.common.listener;

import de.kobich.commons.ListenerList;

/**
 * Support for property change events of editors.
 */
public class EventSupport {
	private final ListenerList<IUIEventListener> list;
	public static EventSupport INSTANCE = new EventSupport();
	
	private EventSupport() {
		this.list = new ListenerList<IUIEventListener>();
	}
	
	public void addListener(IUIEventListener l) {
		this.list.addListener(l);
	}
	public void removeListener(IUIEventListener l) {
		this.list.removeListener(l);
	}
	public void fireEvent(UIEvent event) {
		for (IUIEventListener l : this.list) {
			l.eventFired(event);
		}
	}
}
