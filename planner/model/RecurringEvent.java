package planner.model;

import java.time.LocalDateTime;

/**
 * Description: Represents an event with recurring scheduling rules.
 *
 * OOP Concept Applied - INHERITANCE:
 * Uses the 'extends' keyword to inherit the structure and behavior
 * of the 'Event' superclass. This allows the base class to be extended
 * with new exclusive features (recurrenceType), promoting code reuse.
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

/**
 * Subclass constructor.
 * The 'super' statement is used to invoke the constructor of the 'Event'
 * superclass, ensuring that inherited attributes are properly initialized.
 */
    public RecurringEvent(String title, LocalDateTime dateTime, String location, String description, String category, int reminderMinutesBefore, RecurrenceType recurrenceType) {
        super(title, dateTime, location, description, category, reminderMinutesBefore);
        this.recurrenceType = recurrenceType;
    }

    public RecurrenceType getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(RecurrenceType recurrenceType) { this.recurrenceType = recurrenceType; }

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
 * to identify the subclass during data loading.
 */
    @Override
    public String toCSV() {
        return super.toCSV() + ";[REC:" + recurrenceType.name() + "]";
    }
}