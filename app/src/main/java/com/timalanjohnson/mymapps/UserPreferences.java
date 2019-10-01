package com.timalanjohnson.mymapps;

public class UserPreferences {

    private String travelMode; // Driving, walking, or public transport
    private Boolean metricMeasurements; // True for metric measurements, false for imperial

    public UserPreferences(){}

    public UserPreferences(String travelMode, Boolean metricMeasurements) {
        this.travelMode = travelMode;
        this.metricMeasurements = metricMeasurements;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    public Boolean getMetricMeasurements() {
        return metricMeasurements;
    }

    public void setMetricMeasurements(Boolean metricMeasurements) {
        this.metricMeasurements = metricMeasurements;
    }
}
