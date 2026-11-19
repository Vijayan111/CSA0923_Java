public class DuplicateRegistrationException extends Exception {
    public DuplicateRegistrationException(String studentId, String event) { super("Student " + studentId + " is already registered for " + event + "."); }
}
