package de.kobich.audiosolutions.frontend.common.listener;

import java.util.HashSet;
import java.util.Set;

/**
 * Support for property change events of editors.
 */
public class EventSupport {
	private final Set<IUIEventListener> list;
	public static EventSupport INSTANCE = new EventSupport();
	
	private EventSupport() {
		this.list = new HashSet<>();
	}
	
	public void addListener(IUIEventListener l) {
		this.list.add(l);
	}
	public void removeListener(IUIEventListener l) {
		this.list.remove(l);
	}
	public void fireEvent(UIEvent event) {
		Set<IUIEventListener> tmpList = new HashSet<>(this.list);
		for (IUIEventListener l : tmpList) {
			l.eventFired(event);
		}
	}
}
