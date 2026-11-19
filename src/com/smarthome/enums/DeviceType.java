package com.smarthome.enums;

import java.util.Arrays;

public enum DeviceType {
    LIGHT,
    FAN,
    AC,
    CAMERA,
    DOOR_LOCK,
    SENSOR;

    /**
     * Parses user input such as "light" or " Door_Lock " into a DeviceType.
     * Throws IllegalArgumentException listing the valid values if the input doesn't match.
     */
    public static DeviceType fromString(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Device type cannot be null or empty");
        }
        String normalized = input.trim().toUpperCase();
        for (DeviceType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException(
                "Unknown device type '" + input.trim() + "'. Valid types: " + Arrays.toString(values()));
    }
}
