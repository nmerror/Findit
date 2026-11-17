package com.bubt.findit.core.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A tiny synchronous publish/subscribe bus.
 *
 * <p>It is deliberately not a framework - FindIt is a Swing desktop app, so a
 * 40-line bus is enough and your teammates can read it in one minute.
 *
 * <p>Rules that keep the team out of trouble:
 * <ul>
 *   <li>A listener that throws must never break the publisher - exceptions are
 *       caught and logged here.</li>
 *   <li>Listeners run on the caller's thread. A Swing listener must therefore
 *       use {@code SwingUtilities.invokeLater} for UI updates.</li>
 * </ul>
 */
public class EventBus {

    private final Map<String, List<EventListener>> listeners = new ConcurrentHashMap<>();

    /** Subscribe to one event name, e.g. {@code Events.USER_REGISTERED}. */
    public void subscribe(String eventName, EventListener listener) {
        if (eventName == null || listener == null) {
            return;
        }
        listeners.computeIfAbsent(eventName, key -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /** Subscribe to every event that is published. */
    public void subscribeAll(EventListener listener) {
        subscribe(Events.ALL, listener);
    }

    public void publish(AppEvent event) {
        if (event == null) {
            return;
        }
        dispatch(event.getName(), event);
        dispatch(Events.ALL, event);
    }

    private void dispatch(String name, AppEvent event) {
        List<EventListener> registered = listeners.get(name);
        if (registered == null) {
            return;
        }
        for (EventListener listener : new ArrayList<>(registered)) {
            try {
                listener.onEvent(event);
            } catch (RuntimeException ex) {
                System.err.println("[EventBus] listener for '" + name + "' failed: " + ex.getMessage());
            }
        }
    }

    public void clear() {
        listeners.clear();
    }
}
