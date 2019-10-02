package com.timalanjohnson.mymapps;

public class UserPreferences {

    public static String travelMode; // Driving, walking, or public transport
    public static String units; // True for metric measurements, false for imperial

    public UserPreferences(){}

    public UserPreferences(String travelMode, String units) {
        this.travelMode = travelMode;
        this.units = units;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
