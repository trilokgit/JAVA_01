package com.smarthome.model;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class Home {

    private final UUID id;
    private final String name;
    private final String address;
    private final List<Room> rooms;

    public Home(String name, String address) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Home name cannot be null or empty");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Home address cannot be null or empty");
        }

        this.id = UUID.randomUUID();
        this.name = name.trim();
        this.address = address.trim();
        this.rooms = new ArrayList<>();
    }

    /** Adds a room. Room names must be unique within this home (case-insensitive). */
    public void addRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }
        for (Room existing : rooms) {
            if (existing.getName().equalsIgnoreCase(room.getName())) {
                throw new IllegalArgumentException(
                        "Home '" + name + "' already has a room named '" + room.getName() + "'");
            }
        }
        rooms.add(room);
    }

    public Room getRoom(UUID roomId) {
        for (Room room : rooms) {
            if (room.getId().equals(roomId)) {
                return room;
            }
        }
        throw new NoSuchElementException("No room with ID " + roomId + " in home '" + name + "'");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    /** Read-only view; use addRoom to change the home's rooms. */
    public List<Room> getRooms() {
        return List.copyOf(rooms);
    }

    @Override
    public String toString() {
        return name + " (" + address + ")";
    }
}
