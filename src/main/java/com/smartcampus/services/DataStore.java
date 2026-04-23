package com.smartcampus.services;

import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataStore {

    public static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    public static final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    static {
        // --- Seed Rooms ---
        Room r1 = new Room("LIB-301", "Library Quiet Study", 40);
        Room r2 = new Room("LAB-102", "Computer Lab 102", 30);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        // --- Seed Sensors ---
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "MAINTENANCE", 0.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "ACTIVE", 12.0, "LAB-102");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        // --- Bidirectional links ---
        r1.getSensorIds().add("TEMP-001");
        r1.getSensorIds().add("CO2-001");
        r2.getSensorIds().add("OCC-001");

        // --- Seed readings for TEMP-001 ---
        long now = System.currentTimeMillis();
        List<SensorReading> temp001Readings = new CopyOnWriteArrayList<>();
        temp001Readings.add(new SensorReading("READ-001", now - 3_600_000, 21.0));
        temp001Readings.add(new SensorReading("READ-002", now,             22.5));
        readings.put("TEMP-001", temp001Readings);
        readings.put("CO2-001",  new CopyOnWriteArrayList<>());
        readings.put("OCC-001",  new CopyOnWriteArrayList<>());
    }

    private DataStore() {}
}
