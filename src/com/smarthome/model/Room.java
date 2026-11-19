package com.smarthome.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Room {

    private final UUID id;
    private final String name;
    private final List<Device> devices;

    public Room(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Room name cannot be null or empty");
        }
        this.id = UUID.randomUUID();
        this.name = name.trim();
        this.devices = new ArrayList<>();
    }

    public void addDevice(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null");
        }
        if (devices.contains(device)) {
            throw new IllegalArgumentException("Device '" + device.getName() + "' is already in room '" + name + "'");
        }
        devices.add(device);
    }

    public boolean removeDevice(Device device) {
        return devices.remove(device);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Read-only view; use addDevice/removeDevice to change the room's devices. */
    public List<Device> getDevices() {
        return List.copyOf(devices);
    }

    @Override
    public String toString() {
        return name + " (" + devices.size() + " device(s))";
    }
}
