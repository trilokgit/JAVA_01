import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

import com.smarthome.enums.DeviceType;
import com.smarthome.model.Device;
import com.smarthome.model.Home;
import com.smarthome.model.Room;
import com.smarthome.registry.HomeRegistry;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final HomeRegistry registry = new HomeRegistry();

    public static void main(String[] args) {

        boolean running = true;

        while (running) {
            showMenu();

            System.out.print("\nEnter your choice: ");
            int choice = readInt();

            try {
                switch (choice) {
                    case 1 -> addHome();
                    case 2 -> addRoom();
                    case 3 -> registerDevice();
                    case 4 -> listDevicesInHome();
                    case 5 -> findDeviceBySerial();
                    case 6 -> moveDevice();
                    case 7 -> deactivateDevice();
                    case 8 -> viewAllHomes();
                    case 9 -> removeHome();
                    case 0 -> {
                        running = false;
                        System.out.println("Goodbye!");
                    }
                    default -> System.out.println("Invalid choice. Please enter a number from 0 to 9.");
                }
            } catch (NoSuchElementException e) {
                System.out.println("[Not found] " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("[Error] " + e.getMessage());
            }
        }
    }

    // ---------- Menu actions ----------

    private static void addHome() {
        String name = readLine("Enter Home Name: ");
        String address = readLine("Enter Home Address: ");

        Home home = new Home(name, address);
        registry.addHome(home);
        System.out.println("Home registered: " + home);
    }

    private static void addRoom() {
        Home home = chooseHome();
        if (home == null) return;

        Room room = new Room(readLine("Enter Room Name: "));
        registry.addRoom(home.getId(), room);
        System.out.println("Room '" + room.getName() + "' added to home '" + home.getName() + "'.");
    }

    private static void registerDevice() {
        Home home = chooseHome();
        if (home == null) return;
        Room room = chooseRoom(home);
        if (room == null) return;

        String name = readLine("Enter Device Name: ");
        DeviceType type = DeviceType.fromString(readLine("Enter Device Type " + List.of(DeviceType.values()) + ": "));
        String serial = readLine("Enter Serial Number: ");

        Device device = new Device(name, type, serial);
        registry.registerDevice(home.getId(), room.getId(), device);
        System.out.println("Device registered in '" + room.getName() + "': " + device);
    }

    private static void listDevicesInHome() {
        Home home = chooseHome();
        if (home == null) return;

        List<Device> devices = registry.listDevices(home.getId());
        if (devices.isEmpty()) {
            System.out.println("No devices in home '" + home.getName() + "'.");
            return;
        }
        System.out.println("Devices in '" + home.getName() + "':");
        for (Room room : home.getRooms()) {
            for (Device device : room.getDevices()) {
                System.out.println("  - " + device + " in " + room.getName());
            }
        }
    }

    private static void findDeviceBySerial() {
        String serial = readLine("Enter Serial Number: ");

        Optional<Device> found = registry.findDeviceBySerial(serial);
        if (found.isPresent()) {
            Home home = registry.getHomeOfDevice(serial);
            System.out.println("Found: " + found.get() + " in home '" + home.getName() + "'");
        } else {
            System.out.println("No device found with serial number '" + serial + "'.");
        }
    }

    private static void moveDevice() {
        String serial = readLine("Enter Serial Number of the device to move: ");
        Home home = registry.getHomeOfDevice(serial); // throws NoSuchElementException if unknown

        System.out.println("Choose the target room:");
        Room target = chooseRoom(home);
        if (target == null) return;

        registry.moveDevice(serial, target.getId());
        System.out.println("Device '" + serial + "' moved to room '" + target.getName() + "'.");
    }

    private static void deactivateDevice() {
        String serial = readLine("Enter Serial Number of the device to deactivate: ");
        registry.deactivateDevice(serial);
        System.out.println("Device '" + serial + "' has been deactivated.");
    }

    private static void viewAllHomes() {
        if (registry.getHomeCount() == 0) {
            System.out.println("No homes registered yet.");
            return;
        }
        System.out.println("All registered homes (" + registry.getHomeCount() + "):");
        for (Home home : registry.getAllHomes()) {
            System.out.println("Home: " + home);
            for (Room room : home.getRooms()) {
                System.out.println("  Room: " + room.getName());
                for (Device device : room.getDevices()) {
                    System.out.println("    Device: " + device);
                }
            }
        }
    }

    private static void removeHome() {
        Home home = chooseHome();
        if (home == null) return;

        registry.removeHome(home.getId());
        System.out.println("Home '" + home.getName() + "' removed.");
    }

    private static void showMenu() {
        System.out.println("\n========== SMART HOME REGISTRY ==========");
        System.out.println("1. Add Home");
        System.out.println("2. Add Room to a Home");
        System.out.println("3. Register Device in a Room");
        System.out.println("4. List Devices in a Home");
        System.out.println("5. Find Device by Serial Number");
        System.out.println("6. Move Device to Another Room");
        System.out.println("7. Deactivate Device");
        System.out.println("8. View All Homes");
        System.out.println("9. Remove Home");
        System.out.println("0. Exit");
        System.out.println("=========================================");
    }

    // ---------- Input helpers ----------

    /** Shows a numbered list of homes and returns the chosen one, or null if there are none. */
    private static Home chooseHome() {
        List<Home> homes = registry.getAllHomes();
        if (homes.isEmpty()) {
            System.out.println("No homes registered yet. Add a home first (option 1).");
            return null;
        }
        for (int i = 0; i < homes.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + homes.get(i));
        }
        return homes.get(readChoice("Select home: ", homes.size()) - 1);
    }

    /** Shows a numbered list of the home's rooms and returns the chosen one, or null if there are none. */
    private static Room chooseRoom(Home home) {
        List<Room> rooms = home.getRooms();
        if (rooms.isEmpty()) {
            System.out.println("Home '" + home.getName() + "' has no rooms yet. Add a room first (option 2).");
            return null;
        }
        for (int i = 0; i < rooms.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + rooms.get(i));
        }
        return rooms.get(readChoice("Select room: ", rooms.size()) - 1);
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /** Keeps asking until the user types a number between 1 and max. */
    private static int readChoice(String prompt, int max) {
        System.out.print(prompt);
        while (true) {
            int value = readInt();
            if (value >= 1 && value <= max) {
                return value;
            }
            System.out.print("Please enter a number between 1 and " + max + ": ");
        }
    }

    /** Keeps asking until the user types a valid whole number. */
    private static int readInt() {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}
