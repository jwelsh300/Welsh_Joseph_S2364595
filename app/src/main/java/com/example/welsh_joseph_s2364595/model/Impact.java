package com.example.welsh_joseph_s2364595.model;

public class Impact {

    private String delayType = "";
    private int lanesRestricted = -1;
    private int operationalLanes = -1;

    public Impact() {}

    public String getDelayType() {
        return delayType;
    }

    public void setDelayType(String delayType) {
        this.delayType = delayType;
    }

    public int getLanesRestricted() {
        return lanesRestricted;
    }

    public void setLanesRestricted(int lanesRestricted) {
        this.lanesRestricted = lanesRestricted;
    }

    public int getOperationalLanes() {
        return operationalLanes;
    }

    public void setOperationalLanes(int operationalLanes) {
        this.operationalLanes = operationalLanes;
    }

    public boolean hasDelays() {
        return !delayType.isEmpty();
    }

    public boolean isRoadClosed() {
        return operationalLanes == 0;
    }

    @Override
    public String toString() {
        return "Impact{" +
                "delayType='" + delayType + '\'' +
                ", lanesRestricted=" + lanesRestricted +
                ", operationalLanes=" + operationalLanes +
                '}';
    }
}
