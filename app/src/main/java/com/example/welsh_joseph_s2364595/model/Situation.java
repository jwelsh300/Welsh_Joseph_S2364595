package com.example.welsh_joseph_s2364595.model;

public class Situation {

    private String id = "";
    private String category = "";
    private String recordType = "";
    private String typeDetail = "";
    private String comment = "";
    private String startTime = "";
    private String endTime = "";
    private String direction = "";
    private boolean urgent = false;

    private LocationPoint from = new LocationPoint();

    private LocationPoint to = new LocationPoint();

    private Impact impact = new Impact();

    public Situation() {}

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getRecordType() {
        return recordType;
    }

    public String getTypeDetail() {
        return typeDetail;
    }

    public String getComment() {
        return comment;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public LocationPoint getFrom() {
        return from;
    }

    public LocationPoint getTo() {
        return to;
    }

    public Impact getImpact() {
        return impact;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public void setTypeDetail(String typeDetail) {
        this.typeDetail = typeDetail;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setFrom(LocationPoint from) {
        this.from = from;
    }

    public void setTo(LocationPoint to) {
        this.to = to;
    }

    public void setImpact(Impact impact) {
        this.impact = impact;
    }

    public String getRoadName() {
        return from.getRoadName();
    }

    @Override
    public String toString() {
        return "Situation{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", recordType='" + recordType + '\'' +
                ", typeDetail='" + typeDetail + '\'' +
                ", comment='" + comment + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", direction='" + direction + '\'' +
                ", urgent=" + urgent +
                ", from=" + from +
                ", to=" + to +
                ", impact=" + impact +
                '}';
    }
}
