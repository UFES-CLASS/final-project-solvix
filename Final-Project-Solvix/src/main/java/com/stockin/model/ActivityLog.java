package com.stockin.model;

/**
 * Merepresentasikan satu baris pada "Recent Activity Log" di Dashboard.
 * actor  -> bagian tebal di depan pesan, contoh: "Owner", "Staff", "Logout"
 * action -> sisa kalimat setelah actor, contoh: "Started Production Run #102"
 * type   -> menentukan ikon yang ditampilkan (LOGIN, LOGOUT, INCOMING, PRODUCTION)
 */
public class ActivityLog {

    private int activityId;
    private String actor;
    private String action;
    private String type;
    private String activityDate;
    private String activityTime;

    public ActivityLog() {
    }

    public ActivityLog(String actor, String action, String type, String activityDate, String activityTime) {
        this.actor = actor;
        this.action = action;
        this.type = type;
        this.activityDate = activityDate;
        this.activityTime = activityTime;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(String activityDate) {
        this.activityDate = activityDate;
    }

    public String getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(String activityTime) {
        this.activityTime = activityTime;
    }

}
