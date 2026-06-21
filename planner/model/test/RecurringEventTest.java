package planner.model.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import planner.model.RecurringEvent;

/**
 * RecurringEventTest provides unit tests for the {@link RecurringEvent} model class.
 *
 * <p><strong>Purpose:</strong> These tests validate the chronological calculation logic that
 * determines the date and time of the next occurrence for recurring events based on different
 * recurrence patterns (Daily, Weekly, Monthly).</p>
 *
 * <p><strong>Testing Strategy:</strong> Each test creates a {@link RecurringEvent} with a
 * specific recurrence type and verifies that {@link RecurringEvent#calculateNextOccurrence()}
 * returns the mathematically correct date/time for the next occurrence.</p>
 *
 * <p><strong>Test Coverage:</strong></p>
 * <ul>
 *   <li>Daily recurrence: Next occurrence exactly 1 day after the start date/time</li>
 *   <li>Weekly recurrence: Next occurrence exactly 7 days (1 week) after the start date/time</li>
 *   <li>Monthly recurrence: Next occurrence exactly 1 month after the start date/time</li>
 *   <li>Verification that {@link LocalDateTime} calculations handle month boundaries and year
 *       transitions correctly</li>
 * </ul>
 *
 * <p><strong>Test Data Pattern:</strong> All tests use the same baseline date (June 21, 2026
 * at 10:00 AM) to enable consistent and reproducible test results.</p>
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 2.0
 * @see RecurringEvent
 * @see RecurringEvent.RecurrenceType
 */
public class RecurringEventTest {

    /**
     * Tests that the next occurrence for a DAILY recurrence event is exactly 24 hours
     * (1 day) after the start date and time.
     *
     * <p><strong>Test Scenario:</strong> Creates a daily recurring event with start time
     * of June 21, 2026 at 10:00 AM and verifies that the calculated next occurrence is
     * June 22, 2026 at 10:00 AM (exactly 1 day later).</p>
     *
     * <p><strong>Assertion:</strong> Compares the result of
     * {@link RecurringEvent#calculateNextOccurrence()} with the expected value calculated
     * using {@link LocalDateTime#plusDays(long)}.</p>
     */
    @Test
    public void testCalculateNextOccurrence_Daily() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 21, 10, 0);
        RecurringEvent daily = new RecurringEvent("Daily", start, "Local", "Desc", "Cat", 10, RecurringEvent.RecurrenceType.DAILY);
        
        assertEquals(start.plusDays(1), daily.calculateNextOccurrence(), "Daily recurrence should add exactly one day");
    }

    /**
     * Tests that the next occurrence for a WEEKLY recurrence event is exactly 7 days
     * (1 week) after the start date and time.
     *
     * <p><strong>Test Scenario:</strong> Creates a weekly recurring event with start time
     * of June 21, 2026 at 10:00 AM and verifies that the calculated next occurrence is
     * June 28, 2026 at 10:00 AM (exactly 1 week later).</p>
     *
     * <p><strong>Assertion:</strong> Compares the result of
     * {@link RecurringEvent#calculateNextOccurrence()} with the expected value calculated
     * using {@link LocalDateTime#plusWeeks(long)}.</p>
     */
    @Test
    public void testCalculateNextOccurrence_Weekly() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 21, 10, 0);
        RecurringEvent weekly = new RecurringEvent("Weekly", start, "Local", "Desc", "Cat", 10, RecurringEvent.RecurrenceType.WEEKLY);
        
        assertEquals(start.plusWeeks(1), weekly.calculateNextOccurrence(), "Weekly recurrence should add exactly one week");
    }

    /**
     * Tests that the next occurrence for a MONTHLY recurrence event is exactly 1 month
     * after the start date and time.
     *
     * <p><strong>Test Scenario:</strong> Creates a monthly recurring event with start time
     * of June 21, 2026 at 10:00 AM and verifies that the calculated next occurrence is
     * July 21, 2026 at 10:00 AM (exactly 1 month later).</p>
     *
     * <p><strong>Edge Cases Handled:</strong> The {@link LocalDateTime#plusMonths(long)}
     * method correctly handles:
     * <ul>
     *   <li>Month transitions (e.g., June 30 + 1 month = July 30)</li>
     *   <li>Year transitions (e.g., December 21, 2026 + 1 month = January 21, 2027)</li>
     *   <li>Days that don't exist in the target month (e.g., January 31 + 1 month = February 28/29)</li>
     * </ul>
     * </p>
     *
     * <p><strong>Assertion:</strong> Compares the result of
     * {@link RecurringEvent#calculateNextOccurrence()} with the expected value calculated
     * using {@link LocalDateTime#plusMonths(long)}.</p>
     */
    @Test
    public void testCalculateNextOccurrence_Monthly() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 21, 10, 0);
        RecurringEvent monthlyEvent = new RecurringEvent("Mensal", start, "Local", "Desc", "Cat", 10, RecurringEvent.RecurrenceType.MONTHLY);
        
        assertEquals(start.plusMonths(1), monthlyEvent.calculateNextOccurrence(), "Monthly recurrence should add exactly one month");
    }
}
