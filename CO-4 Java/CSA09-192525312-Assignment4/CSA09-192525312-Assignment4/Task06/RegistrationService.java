import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** SERVICE layer: validation, duplicate rule, paging and export. No Swing classes here. */
public class RegistrationService {
    public record Result(boolean ok, String message) { }
    public record PageData(List<Registration> rows, int total) { }
    private final RegistrationDao dao;
    public RegistrationService(RegistrationDao dao) { this.dao = dao; }

    public List<String> events() throws Exception { return dao.events(); }

    /** @return null when valid, otherwise the first problem, worded for the user. */
    public String validate(String studentId, String name, String email, String event) {
        if (studentId == null || !studentId.trim().matches("\\d{9}")) return "Student ID must be exactly 9 digits.";
        if (name == null || !name.trim().matches("[A-Za-z][A-Za-z .]{1,59}")) return "Name must be 2-60 letters (spaces and dots allowed).";
        if (email == null || !email.trim().matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+")) return "Enter a valid e-mail address, e.g. name@simats.edu.";
        if (event == null || event.isBlank()) return "Select an event.";
        return null;
    }
    public Result register(String studentId, String name, String email, String event) {
        String err = validate(studentId, name, email, event);
        if (err != null) return new Result(false, err);
        try {
            if (dao.exists(studentId.trim(), event)) return new Result(false, new DuplicateRegistrationException(studentId.trim(), event).getMessage());
            dao.insert(studentId.trim(), name.trim(), email.trim(), event);
            return new Result(true, "Registered " + name.trim() + " for " + event + ".");
        } catch (DuplicateRegistrationException d) { return new Result(false, d.getMessage()); }   // lost a race: the UNIQUE key decided
        catch (Exception ex) { return new Result(false, "Database error: " + ex.getMessage()); }
    }
    public PageData page(String event, int page, int size) throws Exception {
        return new PageData(dao.findPage(event, page * size, size), dao.count(event));
    }
    /** Writes every matching registration as CSV and returns the number of rows written. */
    public int exportCsv(String event, Path file) throws Exception {
        List<Registration> all = dao.findAll(event);
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write("id,student_id,name,email,event,registered_at"); w.newLine();
            for (Registration r : all) { w.write(r.id() + "," + q(r.studentId()) + "," + q(r.name()) + "," + q(r.email()) + "," + q(r.event()) + "," + r.registeredAt()); w.newLine(); }
        }
        return all.size();
    }
    private static String q(String s) { return "\"" + s.replace("\"", "\"\"") + "\""; }
}
