import java.util.*;

/** Loaded ONCE into a Map<department, doctors>; a department change is then a hash lookup, not a query. */
public class DoctorDirectory {
    private final Map<String, List<Doctor>> byDept = new TreeMap<>();
    public DoctorDirectory(List<Doctor> all) {
        for (Doctor d : all) byDept.computeIfAbsent(d.department(), k -> new ArrayList<>()).add(d);
    }
    public static DoctorDirectory sample() {
        return new DoctorDirectory(List.of(
            new Doctor("Dr. Anitha Rao", "Cardiology"), new Doctor("Dr. Karthik S", "Cardiology"),
            new Doctor("Dr. Meena Iyer", "Dermatology"), new Doctor("Dr. Farhan Ali", "Dermatology"),
            new Doctor("Dr. Suresh Kumar", "Orthopaedics"), new Doctor("Dr. Lakshmi N", "Orthopaedics"),
            new Doctor("Dr. Priya Nair", "Paediatrics"), new Doctor("Dr. Vikram Das", "Paediatrics"), new Doctor("Dr. Joseph T", "Paediatrics")));
    }
    public Set<String> departments() { return byDept.keySet(); }
    public List<Doctor> doctorsIn(String dept) { return byDept.getOrDefault(dept, List.of()); }
}
