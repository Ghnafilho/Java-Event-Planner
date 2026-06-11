package planner.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Description: Represents an event with recurring scheduling rules.
 *
 * OOP Concept Applied - INHERITANCE:
 * Uses the 'extends' keyword to inherit the structure and behavior
 * of the 'Event' superclass. This allows the base class to be extended
 * with new exclusive features (recurrenceType, seriesId), promoting code reuse.
 */
public class RecurringEvent extends Event {

    /**
     * Type Safety:
     * The use of an Enum restricts the possible recurrence values,
     * ensuring the integrity of chronological scheduling data.
     */
    public enum RecurrenceType {
        DAILY, WEEKLY, MONTHLY, NONE
    }

    private RecurrenceType recurrenceType;
    private String seriesId;

    /**
     * Subclass constructor that generates a new series identifier.
     */
    public RecurringEvent(String title, LocalDateTime dateTime, String location, String description,
                          String category, int reminderMinutesBefore, RecurrenceType recurrenceType) {
        this(title, dateTime, location, description, category, reminderMinutesBefore,
                recurrenceType, UUID.randomUUID().toString());
    }

    /**
     * Subclass constructor with an explicit series identifier shared by all occurrences.
     * A null seriesId is allowed for legacy data loaded from older file formats.
     */
    public RecurringEvent(String title, LocalDateTime dateTime, String location, String description,
                          String category, int reminderMinutesBefore, RecurrenceType recurrenceType,
                          String seriesId) {
        super(title, dateTime, location, description, category, reminderMinutesBefore);
        this.recurrenceType = recurrenceType;
        this.seriesId = seriesId;
    }

    public RecurrenceType getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(RecurrenceType recurrenceType) { this.recurrenceType = recurrenceType; }

    public String getSeriesId() { return seriesId; }
    public void setSeriesId(String seriesId) { this.seriesId = seriesId; }

    /**
     * Calculates the date and time of the next occurrence of the event
     * based on the recurrence type associated with the object.
     */
    public LocalDateTime calculateNextOccurrence() {
        switch (recurrenceType) {
            case DAILY: return this.dateTime.plusDays(1);
            case WEEKLY: return this.dateTime.plusWeeks(1);
            case MONTHLY: return this.dateTime.plusMonths(1);
            default: return this.dateTime;
        }
    }

    /**
     * OOP Concept Applied - POLYMORPHISM:
     * Overrides the serialization method. The call to 'super.toCSV()' reuses
     * the logic implemented in the parent class, adding the necessary suffix
     * to identify the subclass and its series during data loading.
     */
    @Override
    public String toCSV() {
        return super.toCSV() + ";[REC:" + recurrenceType.name() + ":" + seriesId + "]";
    }
}
