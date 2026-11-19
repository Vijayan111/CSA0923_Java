import java.util.*;

/** Booking rules, independent of any window. */
public class AppointmentService {
    public record Result(boolean ok, String message) { }
    private final Map<String, String> booked = new HashMap<>();     // "doctor|slot" -> patient
    public static final String[] SLOTS = {"09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "14:00", "14:30"};

    public Result book(String patientId, String dept, Doctor doctor, String slot) {
        if (patientId == null || !patientId.trim().matches("P\\d{5}")) return new Result(false, "Patient ID must look like P10234.");
        if (dept == null) return new Result(false, "Select a department.");
        if (doctor == null) return new Result(false, "Select a doctor.");
        if (slot == null) return new Result(false, "Select a time slot.");
        String key = doctor.name() + "|" + slot;
        if (booked.containsKey(key)) return new Result(false, doctor.name() + " is already booked at " + slot + ".");
        booked.put(key, patientId.trim());
        return new Result(true, "Booked: " + patientId.trim() + " with " + doctor.name() + " (" + dept + ") at " + slot);
    }
}
