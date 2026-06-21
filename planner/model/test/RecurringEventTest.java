package planner.model.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import planner.model.RecurringEvent;

/**
 * Unit tests for the RecurringEvent model class.
 * Validates the chronological calculation logic for various recurrence types.
 */
public class RecurringEventTest {

    /**
     * Tests that the next occurrence for a DAILY event is exactly 24 hours (1 day) after the start date.
     */
    @Test
    public void testCalculateNextOccurrence_Daily() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 21, 10, 0);
        RecurringEvent daily = new RecurringEvent("Daily", start, "Local", "Desc", "Cat", 10, RecurringEvent.RecurrenceType.DAILY);
        
        assertEquals(start.plusDays(1), daily.calculateNextOccurrence(), "Daily recurrence should add exactly one day");
    }

    /**
     * Tests that the next occurrence for a WEEKLY event is exactly 7 days after the start date.
     */
    @Test
    public void testCalculateNextOccurrence_Weekly() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 21, 10, 0);
        RecurringEvent weekly = new RecurringEvent("Weekly", start, "Local", "Desc", "Cat", 10, RecurringEvent.RecurrenceType.WEEKLY);
        
        assertEquals(start.plusWeeks(1), weekly.calculateNextOccurrence(), "Weekly recurrence should add exactly one week");
    }

    /**
     * Tests that the next occurrence for a MONTHLY event is exactly one month after the start date.
     */
    @Test
    public void testCalculateNextOccurrence_Monthly() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 21, 10, 0);
        RecurringEvent monthlyEvent = new RecurringEvent("Mensal", start, "Local", "Desc", "Cat", 10, RecurringEvent.RecurrenceType.MONTHLY);
        
        assertEquals(start.plusMonths(1), monthlyEvent.calculateNextOccurrence(), "Monthly recurrence should add exactly one month");
    }
}