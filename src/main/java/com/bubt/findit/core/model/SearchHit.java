package com.bubt.findit.core.model;

/**
 * One hit of the global search.
 *
 * <p>The same shape is used for a person (Segment 1), an item (Segment 2) and
 * a claim (Segment 3/4), so the search screen can mix them in one list.
 */
public class SearchHit {

    private final String source;
    private final String title;
    private final String subtitle;
    private final String kind;
    private final Long referenceId;

    public SearchHit(String source, String title, String subtitle, String kind, Long referenceId) {
        this.source = source;
        this.title = title;
        this.subtitle = subtitle;
        this.kind = kind;
        this.referenceId = referenceId;
    }

    /** Which segment produced the hit, e.g. "USERS" or "ITEMS". */
    public String getSource() {
        return source;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    /** A short tag such as PERSON, ITEM or CLAIM. */
    public String getKind() {
        return kind;
    }

    /** The id of the row behind the hit, so the screen can open it. */
    public Long getReferenceId() {
        return referenceId;
    }

    @Override
    public String toString() {
        return kind + " · " + title;
    }
}
