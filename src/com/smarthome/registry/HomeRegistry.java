package com.smarthome.registry;

import com.smarthome.model.Device;
import com.smarthome.model.Home;
import com.smarthome.model.Room;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * Central store for all homes. Homes are kept in a Map keyed by their UUID;
 * rooms and devices live inside their Home / Room objects.
 */
public class HomeRegistry {

    // LinkedHashMap keeps insertion order, so homes are always listed in the order they were added.
    private final Map<UUID, Home> homes = new LinkedHashMap<>();

    // ---------- Homes ----------

    public void addHome(Home home) {
        if (home == null) {
            throw new IllegalArgumentException("Home cannot be null");
        }
        if (homes.containsKey(home.getId())) {
            throw new IllegalArgumentException("Home '" + home.getName() + "' is already registered");
        }
        homes.put(home.getId(), home);
    }

    /** @throws NoSuchElementException if no home has this ID */
    public Home getHome(UUID homeId) {
        Home home = homes.get(homeId);
        if (home == null) {
            throw new NoSuchElementException("No home found with ID " + homeId);
        }
        return home;
    }

    public List<Home> getAllHomes() {
        return List.copyOf(homes.values());
    }

    /** @throws NoSuchElementException if no home has this ID */
    public void removeHome(UUID homeId) {
        if (homes.remove(homeId) == null) {
            throw new NoSuchElementException("No home found with ID " + homeId);
        }
    }

    public int getHomeCount() {
        return homes.size();
    }

    // ---------- Rooms ----------

    /**
     * @throws NoSuchElementException   if the home doesn't exist
     * @throws IllegalArgumentException if the home already has a room with this name
     */
    public void addRoom(UUID homeId, Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }
        Home home = getHome(homeId);

        // The same Room object must not be shared between two homes.
        for (Home h : homes.values()) {
            for (Room r : h.getRooms()) {
                if (r.getId().equals(room.getId())) {
                    throw new IllegalArgumentException("Room '" + room.getName() + "' already belongs to home '" + h.getName() + "'");
                }
            }
        }

        home.addRoom(room); // Home enforces unique room names
    }

    // ---------- Devices ----------

    /**
     * @throws NoSuchElementException   if the home doesn't exist, or the room isn't in that home
     * @throws IllegalArgumentException if any home already has a device with this serial number
     */
    public void registerDevice(UUID homeId, UUID roomId, Device device) {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null");
        }
        Room room = getHome(homeId).getRoom(roomId);

        if (locateDevice(device.getSerialNumber()).isPresent()) {
            throw new IllegalArgumentException(
                    "A device with serial number '" + device.getSerialNumber() + "' is already registered");
        }

        room.addDevice(device);
    }

    /** @throws NoSuchElementException if the home doesn't exist */
    public List<Device> listDevices(UUID homeId) {
        List<Device> result = new ArrayList<>();
        for (Room room : getHome(homeId).getRooms()) {
            result.addAll(room.getDevices());
        }
        return result;
    }

    /** Serial number match is case-insensitive. Returns empty if no device matches. */
    public Optional<Device> findDeviceBySerial(String serialNumber) {
        return locateDevice(serialNumber).map(DeviceLocation::device);
    }

    /** @throws NoSuchElementException if no device has this serial number */
    public Home getHomeOfDevice(String serialNumber) {
        return requireDevice(serialNumber).home();
    }

    /**
     * Moves a device to another room in the same home.
     *
     * @throws NoSuchElementException if no device has this serial number,
     *                                or the target room isn't in the device's home
     */
    public void moveDevice(String serialNumber, UUID targetRoomId) {
        DeviceLocation location = requireDevice(serialNumber);
        Room target = location.home().getRoom(targetRoomId);

        if (target == location.room()) {
            throw new IllegalArgumentException("Device is already in room '" + target.getName() + "'");
        }

        location.room().removeDevice(location.device());
        target.addDevice(location.device());
    }

    /**
     * @throws NoSuchElementException if no device has this serial number
     * @throws IllegalStateException  if the device is already inactive
     */
    public void deactivateDevice(String serialNumber) {
        requireDevice(serialNumber).device().deactivate();
    }

    // ---------- Helpers ----------

    /** Where a device lives: which home and which room. */
    private record DeviceLocation(Home home, Room room, Device device) {}

    private Optional<DeviceLocation> locateDevice(String serialNumber) {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("Serial number cannot be null or empty");
        }
        String serial = serialNumber.trim();

        for (Home home : homes.values()) {
            for (Room room : home.getRooms()) {
                for (Device device : room.getDevices()) {
                    if (device.getSerialNumber().equalsIgnoreCase(serial)) {
                        return Optional.of(new DeviceLocation(home, room, device));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private DeviceLocation requireDevice(String serialNumber) {
        return locateDevice(serialNumber)
                .orElseThrow(() -> new NoSuchElementException(
                        "No device found with serial number '" + serialNumber.trim() + "'"));
    }
}
