import java.time.LocalDateTime;
public record Registration(int id, String studentId, String name, String email, String event, LocalDateTime registeredAt) { }
