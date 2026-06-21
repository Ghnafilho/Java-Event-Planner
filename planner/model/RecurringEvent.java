package planner.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RecurringEvent is a specialized domain model class that represents an event with recurring
 * scheduling rules.
 *
 * <p><strong>Design Pattern - Inheritance:</strong> Uses the 'extends' keyword to inherit the
 * structure and behavior of the {@link Event} superclass. This allows the base event class to be
 * extended with new exclusive features ({@link #recurrenceType}, {@link #seriesId}), promoting
 * code reuse and establishing a clear IS-A relationship.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Inherits all base event properties from {@link Event}</li>
 *   <li>Adds recurrence type classification (Daily, Weekly, Monthly, None)</li>
 *   <li>Maintains a series identifier to group recurring instances together</li>
 *   <li>Calculates next occurrence based on recurrence type</li>
 *   <li>Supports legacy data migration with optional series identifiers</li>
 *   <li>Polymorphic serialization with recurrence metadata</li>
 * </ul>
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 2.0
 */
public class RecurringEvent extends Event {

    /**
     * Enumeration representing the possible recurrence patterns for a recurring event.
     *
     * <p><strong>Type Safety:</strong> The use of an Enum restricts the possible recurrence
     * values to a predefined set, ensuring the integrity of chronological scheduling data and
     * preventing invalid recurrence types from being assigned.</p>
     */
    public enum RecurrenceType {
        /** Event repeats every day */
        DAILY,
        /** Event repeats every week on the same day */
        WEEKLY,
        /** Event repeats every month on the same date */
        MONTHLY,
        /** Event does not recur (single occurrence) */
        NONE
    }

    /** The pattern describing how often this recurring event repeats */
    private RecurrenceType recurrenceType;
    /** A unique identifier shared by all occurrences of this recurring event series */
    private String seriesId;

    /**
     * Constructs a RecurringEvent with automatic series identifier generation.
     *
     * <p>This constructor generates a new unique series identifier using {@link UUID#randomUUID()}
     * for this recurring event. All occurrences of this recurring event will share the same
     * series identifier to maintain the relationship between instances.</p>
     *
     * @param title The title or name of the event (inherited from {@link Event})
     * @param dateTime The {@link LocalDateTime} when the first occurrence of the event happens
     * @param location The physical or virtual location of the event
     * @param description A detailed description or notes about the event
     * @param category The category or classification of the event
     * @param reminderMinutesBefore The number of minutes before each occurrence to trigger a
     *                               reminder
     * @param recurrenceType The {@link RecurrenceType} pattern for this recurring event
     */
    public RecurringEvent(String title, LocalDateTime dateTime, String location, String description,
                          String category, int reminderMinutesBefore, RecurrenceType recurrenceType) {
        this(title, dateTime, location, description, category, reminderMinutesBefore,
                recurrenceType, UUID.randomUUID().toString());
    }

    /**
     * Constructs a RecurringEvent with an explicit series identifier.
     *
     * <p>This constructor allows the specification of a custom series identifier, which is
     * particularly useful for loading recurring events from persisted data or for migrating
     * legacy recurring events that may not have an explicit series identifier.</p>
     *
     * <p><strong>Legacy Support:</strong> A {@code null} seriesId is permitted to support legacy
     * data loaded from older file formats. Such events will be automatically assigned a new series
     * identifier during the data migration process.</p>
     *
     * @param title The title or name of the event (inherited from {@link Event})
     * @param dateTime The {@link LocalDateTime} when the first occurrence of the event happens
     * @param location The physical or virtual location of the event
     * @param description A detailed description or notes about the event
     * @param category The category or classification of the event
     * @param reminderMinutesBefore The number of minutes before each occurrence to trigger a
     *                               reminder
     * @param recurrenceType The {@link RecurrenceType} pattern for this recurring event
     * @param seriesId A unique {@link String} identifier shared by all occurrences of this series,
     *                 or {@code null} for legacy events without an explicit series identifier
     */
    public RecurringEvent(String title, LocalDateTime dateTime, String location, String description,
                          String category, int reminderMinutesBefore, RecurrenceType recurrenceType,
                          String seriesId) {
        super(title, dateTime, location, description, category, reminderMinutesBefore);
        this.recurrenceType = recurrenceType;
        this.seriesId = seriesId;
    }

    /**
     * Returns the recurrence type pattern for this recurring event.
     *
     * @return The {@link RecurrenceType} indicating how often this event repeats
     */
    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    /**
     * Sets the recurrence type pattern for this recurring event.
     *
     * @param recurrenceType The new {@link RecurrenceType} for this recurring event
     */
    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    /**
     * Returns the series identifier for this recurring event.
     *
     * <p>The series identifier is a unique {@link String} that groups all occurrences of this
     * recurring event together, allowing the system to identify which individual event instances
     * belong to the same recurring series.</p>
     *
     * @return The series identifier as a {@link String}, or {@code null} if the event is from
     *         legacy data without an explicit identifier
     */
    public String getSeriesId() {
        return seriesId;
    }

    /**
     * Sets the series identifier for this recurring event.
     *
     * @param seriesId The unique {@link String} identifier to group this event with other
     *                 occurrences of the same recurring series
     */
    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    /**
     * Calculates the date and time of the next occurrence of this recurring event.
     *
     * <p>Based on the {@link RecurrenceType} associated with this object, calculates and returns
     * the date/time of the next scheduled occurrence by adding the appropriate time interval to
     * the current event's date/time:</p>
     * <ul>
     *   <li>{@link RecurrenceType#DAILY}: Adds 1 day</li>
     *   <li>{@link RecurrenceType#WEEKLY}: Adds 1 week</li>
     *   <li>{@link RecurrenceType#MONTHLY}: Adds 1 month</li>
     *   <li>{@link RecurrenceType#NONE}: Returns the same date/time (no recurrence)</li>
     * </ul>
     *
     * @return A {@link LocalDateTime} representing when the next occurrence should take place
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
     * Converts the recurring event to a CSV (comma-separated values) format string for file
     * persistence.
     *
     * <p><strong>Design Pattern - Polymorphism:</strong> This method overrides the
     * {@link Event#toCSV()} method, demonstrating polymorphic behavior. The implementation calls
     * {@code super.toCSV()} to reuse the parent class serialization logic, then appends a
     * recurrence metadata suffix to identify this as a recurring event and preserve its series
     * information.</p>
     *
     * <p><strong>CSV Format Extension:</strong> The parent CSV format is extended with a suffix:
     * {@code ;[REC:RecurrenceType:SeriesId]}
     * </p>
     *
     * @return A formatted {@link String} in CSV format with recurrence metadata, ready for
     *         storage to disk
     */
    @Override
    public String toCSV() {
        return super.toCSV() + ";[REC:" + recurrenceType.name() + ":" + seriesId + "]";
    }
}
