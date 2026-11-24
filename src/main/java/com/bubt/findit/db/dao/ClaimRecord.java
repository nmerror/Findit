package com.bubt.findit.db.dao;

/**
 * A row of the {@code claims} table on its way into the database.
 *
 * <p>Segment 3 owns the matching, Segment 4 owns the verification - this bean
 * is only the data carrier used by the shared backend.
 */
public class ClaimRecord {

    private Long itemId;
    private Long claimantId;
    private Long matchId;
    private String descriptionOfItem;
    private String proofDetails;
    private String status = "SUBMITTED";

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(Long claimantId) {
        this.claimantId = claimantId;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public String getDescriptionOfItem() {
        return descriptionOfItem;
    }

    public void setDescriptionOfItem(String descriptionOfItem) {
        this.descriptionOfItem = descriptionOfItem;
    }

    public String getProofDetails() {
        return proofDetails;
    }

    public void setProofDetails(String proofDetails) {
        this.proofDetails = proofDetails;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
