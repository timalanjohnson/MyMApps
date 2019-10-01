package com.timalanjohnson.mymapps;

import java.io.Serializable;

public class Trip implements Serializable {

    private String origin;
    private String destination;
    private String travelMode;
    private String distance;
    private String duration;
    private String time;

    public Trip() {}

    public Trip(String origin, String destination, String travelMode, String distance, String duration, String time) {
        this.origin = origin;
        this.destination = destination;
        this.travelMode = travelMode;
        this.distance = distance;
        this.duration = duration;
        this.time = time;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
