package com.bubt.findit.core.model;

import java.time.LocalDateTime;

/**
 * Lightweight projection of an item, used by Segment 1's dashboard so it can
 * list "recently lost / recently found" items without depending on Segment 2's
 * entity classes.
 */
public class ItemSummary {

    private Long id;
    private String title;
    private String category;
    private ItemType type;
    private String locationLabel;
    private String postedByName;
    private Long postedByUserId;
    private String status;
    private LocalDateTime createdAt;
    private String imagePath;

    public ItemSummary() {
    }

    public ItemSummary(Long id, String title, String category, ItemType type,
                       String locationLabel, String postedByName, Long postedByUserId,
                       String status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.type = type;
        this.locationLabel = locationLocation(locationLabel);
        this.postedByName = postedByName;
        this.postedByUserId = postedByUserId;
        this.status = status;
        this.createdAt = createdAt;
    }

    private static String locationLocation(String value) {
        return value == null ? "Unknown" : value;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public ItemType getType() {
        return type;
    }

    public String getLocationLabel() {
        return locationLabel;
    }

    public String getPostedByName() {
        return postedByName;
    }

    public Long getPostedByUserId() {
        return postedByUserId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** The file path of the photo the user attached, or null. */
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
