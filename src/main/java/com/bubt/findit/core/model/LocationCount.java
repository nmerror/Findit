package com.bubt.findit.core.model;

/** "Library - 3 items lost" style counter, used by the dashboard charts. */
public class LocationCount {

    private final String label;
    private final int count;

    public LocationCount(String label, int count) {
        this.label = label;
        this.count = count;
    }

    public String getLabel() {
        return label;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return label + " (" + count + ")";
    }
}
