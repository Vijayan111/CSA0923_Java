import java.io.Serializable;
import java.util.*;
public class Hostel implements Serializable {
    private static final long serialVersionUID = 1L;
    String name; List<Room> rooms = new ArrayList<>();
    Hostel(String name) { this.name = name; }
}
