package parking.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OneDayPass implements Pass {
    private final LocalDate day;
    public OneDayPass(LocalDate day) { this.day = day; }
    @Override public boolean covers(LocalDateTime entry, LocalDateTime exit) {
        return entry.toLocalDate().equals(day) && exit.toLocalDate().equals(day);   // valid only for that calendar day
    }
}
