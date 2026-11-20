import java.io.*;
import java.sql.*;

public class Main {
    public static void main(String[] a) throws Exception {
        String mode = a[0], file = a[1];
        if (mode.equals("save")) {
            try (Connection con = DriverManager.getConnection("jdbc:sqlite::memory:")) {
                Hostel h = new Hostel("Block-A"); Room r1 = new Room(101), r2 = new Room(102);
                Resident x = new Resident("Arun", 1, "secret1", con), y = new Resident("Divya", 2, "secret2", con);
                r1.residents.add(x); r1.residents.add(y); r2.residents.add(x);        // Arun is referenced from TWO rooms
                h.rooms.add(r1); h.rooms.add(r2);
                try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(file))) { o.writeObject(h); }
                System.out.println("saved " + file + " (" + new File(file).length() + " bytes) with class " + Resident.class.getSimpleName());
            }
        } else {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                Hostel h = (Hostel) in.readObject();
                System.out.println("loaded " + h.name + ": rooms=" + h.rooms.size());
                for (Room r : h.rooms) System.out.println("  room " + r.no + " -> " + r.residents);
                System.out.println("  same Arun object in both rooms after reload: " + (h.rooms.get(0).residents.get(0) == h.rooms.get(1).residents.get(0)));
            } catch (InvalidClassException e) {
                System.out.println("FAILED: java.io.InvalidClassException: " + e.getMessage());
            }
        }
    }
}
