package com.smartcampus.models;

public class SensorReading {
    private String id;       // UUID — always auto-generated server-side
    private long timestamp;  // epoch ms — always auto-set server-side
    private double value;

    public SensorReading() {}

    public SensorReading(String id, long timestamp, double value) {
        this.id = id; this.timestamp = timestamp; this.value = value;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
