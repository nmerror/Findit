package com.bubt.findit.core.event;

/** Receives events published on the {@link EventBus}. */
@FunctionalInterface
public interface EventListener {

    void onEvent(AppEvent event);
}
