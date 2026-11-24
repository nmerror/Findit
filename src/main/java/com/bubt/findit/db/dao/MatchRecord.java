package com.bubt.findit.db.dao;

/**
 * A row of the {@code matches} table on its way into the database - a lost item
 * paired with a found item and the score the matching engine gave them.
 */
public class MatchRecord {

    private Long lostItemId;
    private Long foundItemId;
    private double score;
    private String status = "NEW";
    private String algorithm;

    public Long getLostItemId() {
        return lostItemId;
    }

    public void setLostItemId(Long lostItemId) {
        this.lostItemId = lostItemId;
    }

    public Long getFoundItemId() {
        return foundItemId;
    }

    public void setFoundItemId(Long foundItemId) {
        this.foundItemId = foundItemId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
