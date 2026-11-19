package com.smarthome.model;

import com.smarthome.enums.DeviceType;
import java.util.UUID;

public class Device {

    private final UUID id;
    private final String name;
    private final DeviceType type;
    private final String serialNumber;
    private boolean active;

    public Device(String name, DeviceType type, String serialNumber) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Device name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Device type cannot be null");
        }
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("Device serial number cannot be null or empty");
        }

        this.id = UUID.randomUUID();
        this.name = name.trim();
        this.type = type;
        this.serialNumber = serialNumber.trim();
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DeviceType getType() {
        return type;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        if (!active) {
            throw new IllegalStateException("Device '" + serialNumber + "' is already inactive");
        }
        this.active = false;
    }

    @Override
    public String toString() {
        return name + " [" + type + ", serial=" + serialNumber + ", " + (active ? "ACTIVE" : "INACTIVE") + "]";
    }
}
