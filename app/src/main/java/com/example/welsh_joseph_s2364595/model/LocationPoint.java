package com.example.welsh_joseph_s2364595.model;

public class LocationPoint {

    private double latitude = 0.0;
    private double longitude = 0.0;
    private String roadName = "";
    private String secondRoadName = "";
    private String junctionName = "";
    private String placeName = "";

    public LocationPoint() {}

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public String getSecondRoadName() {
        return secondRoadName;
    }

    public void setSecondRoadName(String secondRoadName) {
        this.secondRoadName = secondRoadName;
    }

    public String getJunctionName() {
        return junctionName;
    }

    public void setJunctionName(String junctionName) {
        this.junctionName = junctionName;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public boolean hasCoordinates() {
        return latitude != 0.0 && longitude != 0.0;
    }

    @Override
    public String toString() {
        return "LocationPoint{" +
                "lat=" + latitude +
                ", lon=" + longitude +
                ", road='" + roadName + '\'' +
                ", secondRoad='" + secondRoadName + '\'' +
                ", junction='" + junctionName + '\'' +
                ", place='" + placeName + '\'' +
                '}';
    }
}
