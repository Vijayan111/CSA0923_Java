import java.io.IOException;

class ExamException extends Exception {                       // checked base of the portal
    ExamException(String m) { super(m); }
    ExamException(String m, Throwable c) { super(m, c); }
}
class ExamWindowClosedException extends ExamException { ExamWindowClosedException(String m) { super(m); } }
class NotRegisteredException   extends ExamException { NotRegisteredException(String m)   { super(m); } }
class FileTooLargeException    extends ExamException { FileTooLargeException(String m)    { super(m); } }
class SubmissionStorageException  extends ExamException { SubmissionStorageException(String m, Throwable c) { super(m, c); } }
class PortalConfigException extends RuntimeException {          // unchecked: programmer/config error
    PortalConfigException(String m) { super(m); }
}

class StorageLayer {
    void write(String file, long kb) throws SubmissionStorageException {
        try {
            if (kb > 5000) throw new IOException("No space left on device (quota)");
            System.out.println("  [storage] saved " + file);
        } catch (IOException e) {
            throw new SubmissionStorageException("Could not store " + file, e);   // chaining
        }
    }
}
class ServiceLayer {
    final StorageLayer storage = new StorageLayer();
    void submit(String student, String course, String file, long kb, int hour) throws ExamException {
        if (course == null) throw new PortalConfigException("course id missing in request");
        if (hour >= 17) throw new ExamWindowClosedException("Exam window closed at 17:00 (now " + hour + ":00)");
        if (!student.startsWith("S")) throw new NotRegisteredException(student + " is not registered for " + course);
        if (kb > 500 && kb <= 5000) throw new FileTooLargeException(file + " is " + kb + " KB; limit is 500 KB");
        storage.write(file, kb);
    }
}
class ControllerLayer {
    final ServiceLayer service = new ServiceLayer();
    void handle(String id, String st, String co, String f, long kb, int h) {
        System.out.println(id + ": " + st + ", " + co + ", " + f + ", " + kb + " KB, " + h + ":00");
        try { service.submit(st, co, f, kb, h); System.out.println("  Result: submission accepted"); }
        catch (ExamWindowClosedException | NotRegisteredException | FileTooLargeException e) {
            System.out.println("  Student sees: " + e.getMessage());
        } catch (SubmissionStorageException e) {
            System.out.println("  Student sees: Sorry, your file could not be saved. Please retry.");
            System.out.println("  Log: " + e + "  <-- cause: " + e.getCause());
        } catch (ExamException e) { System.out.println("  Student sees: " + e.getMessage()); }
        catch (RuntimeException e) { System.out.println("  Student sees: internal error (" + e.getMessage() + ")"); }
    }
}
public class Portal {
    public static void main(String[] args) throws Exception {
        ControllerLayer c = new ControllerLayer();
        c.handle("S1", "S101", "CSA09", "a1.pdf", 120, 10);
        c.handle("S2", "S101", "CSA09", "a2.pdf", 120, 18);
        c.handle("S3", "X777", "CSA09", "a3.pdf", 120, 10);
        c.handle("S4", "S101", "CSA09", "a4.pdf", 800, 10);
        c.handle("S5", "S101", "CSA09", "a5.pdf", 9000, 10);
        c.handle("S6", "S101", null,    "a6.pdf", 100, 10);
        System.out.println("\nFull chained stack trace for S5:");
        try { new ServiceLayer().submit("S101", "CSA09", "a5.pdf", 9000, 10); }
        catch (ExamException e) { e.printStackTrace(System.out); }
        System.out.println("\nSame failure re-thrown WITHOUT the cause:");
        try {
            try { new StorageLayer().write("a5.pdf", 9000); }
            catch (SubmissionStorageException e) { throw new ExamException("Could not store a5.pdf"); }
        } catch (ExamException e) { e.printStackTrace(System.out); System.out.println("getCause() = " + e.getCause()); }
    }
}
