package parking.model;

import java.time.LocalDateTime;

public class MonthlyPass implements Pass {
    @Override public boolean covers(LocalDateTime entry, LocalDateTime exit) { return true; }   // same behaviour as the old boolean
}
