package com.bubt.findit.core.model;

/**
 * Dashboard counters for one user.
 *
 * <p>Everything that is not produced by Segment 1 itself (items, matches,
 * claims) is filled in through the SPI interfaces in {@code core.spi}. If a
 * teammate's segment is not implemented yet, the corresponding counter simply
 * stays at zero and the dashboard still renders.
 */
public class DashboardStats {

    // --- Segment 1 data -------------------------------------------------
    private int totalUsers;
    private int totalStudents;
    private int totalAdmins;
    private int activeUsers;
    private int suspendedUsers;
    private int myLostReports;
    private int myFoundReports;

    // --- Segment 2/3/4 data (filled via SPI, 0 until they land) --------
    private int totalLostItems;
    private int totalFoundItems;
    private int openClaims;
    private int pendingVerification;
    private int approvedClaims;
    private int returnedItems;
    private int newMatches;

    private java.util.List<ItemSummary> recentItems = java.util.Collections.emptyList();
    private java.util.List<LocationCount> topLocations = java.util.Collections.emptyList();

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(int totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }

    public int getSuspendedUsers() {
        return suspendedUsers;
    }

    public void setSuspendedUsers(int suspendedUsers) {
        this.suspendedUsers = suspendedUsers;
    }

    public int getMyLostReports() {
        return myLostReports;
    }

    public void setMyLostReports(int myLostReports) {
        this.myLostReports = myLostReports;
    }

    public int getMyFoundReports() {
        return myFoundReports;
    }

    public void setMyFoundReports(int myFoundReports) {
        this.myFoundReports = myFoundReports;
    }

    public int getTotalLostItems() {
        return totalLostItems;
    }

    public void setTotalLostItems(int totalLostItems) {
        this.totalLostItems = totalLostItems;
    }

    public int getTotalFoundItems() {
        return totalFoundItems;
    }

    public void setTotalFoundItems(int totalFoundItems) {
        this.totalFoundItems = totalFoundItems;
    }

    public int getOpenClaims() {
        return openClaims;
    }

    public void setOpenClaims(int openClaims) {
        this.openClaims = openClaims;
    }

    public int getPendingVerification() {
        return pendingVerification;
    }

    public void setPendingVerification(int pendingVerification) {
        this.pendingVerification = pendingVerification;
    }

    public int getApprovedClaims() {
        return approvedClaims;
    }

    public void setApprovedClaims(int approvedClaims) {
        this.approvedClaims = approvedClaims;
    }

    public int getReturnedItems() {
        return returnedItems;
    }

    public void setReturnedItems(int returnedItems) {
        this.returnedItems = returnedItems;
    }

    public int getNewMatches() {
        return newMatches;
    }

    public void setNewMatches(int newMatches) {
        this.newMatches = newMatches;
    }

    public java.util.List<ItemSummary> getRecentItems() {
        return recentItems;
    }

    public void setRecentItems(java.util.List<ItemSummary> recentItems) {
        this.recentItems = recentItems == null ? java.util.Collections.emptyList() : recentItems;
    }

    public java.util.List<LocationCount> getTopLocations() {
        return topLocations;
    }

    public void setTopLocations(java.util.List<LocationCount> topLocations) {
        this.topLocations = topLocations == null ? java.util.Collections.emptyList() : topLocations;
    }
}
