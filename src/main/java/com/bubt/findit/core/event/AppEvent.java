package com.bubt.findit.core.event;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An application event. Segment 1 publishes them, other segments subscribe.
 *
 * <p>This is the "loose" integration channel: Segment 1 does not need to know
 * that Segment 4 exists in order to tell it "a user just registered".
 */
public final class AppEvent {

    private final String name;
    private final Long actorUserId;
    private final Map<String, Object> data;

    private AppEvent(String name, Long actorUserId, Map<String, Object> data) {
        this.name = name;
        this.actorUserId = actorUserId;
        this.data = data == null ? new LinkedHashMap<>() : new LinkedHashMap<>(data);
    }

    public static Builder of(String name) {
        return new Builder(name);
    }

    public String getName() {
        return name;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public Map<String, Object> getData() {
        return Collections.unmodifiableMap(data);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public String getString(String key, String fallback) {
        Object value = data.get(key);
        return value == null ? fallback : value.toString();
    }

    public Long getLong(String key) {
        Object value = data.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "AppEvent{" + name + ", actor=" + actorUserId + ", data=" + data + '}';
    }

    /** Small fluent builder so call sites stay readable. */
    public static final class Builder {

        private final String name;
        private final Map<String, Object> data = new LinkedHashMap<>();
        private Long actorUserId;

        private Builder(String name) {
            this.name = name;
        }

        public Builder actor(Long userId) {
            this.actorUserId = userId;
            return this;
        }

        public Builder with(String key, Object value) {
            data.put(key, value);
            return this;
        }

        public AppEvent build() {
            return new AppEvent(name, actorUserId, data);
        }
    }
}
