package parking.model;

import java.time.LocalDateTime;

/** A pass decides whether parking is free for a given exit time. */
public interface Pass {
    boolean covers(LocalDateTime entry, LocalDateTime exit);
}
