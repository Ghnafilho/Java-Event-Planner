package planner.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Event is the base domain model class representing an event in the calendar system.
 *
 * <p><strong>Design Pattern - Inheritance Preparation:</strong> The attributes use the
 * 'protected' access modifier, which encapsulates the data from external classes while
 * allowing subclasses (such as {@link RecurringEvent}) to have direct access to inherited
 * attributes. This enables proper object-oriented design with inheritance hierarchies.</p>
 *
 * <p><strong>Design Pattern - Composition:</strong> The Event class contains a list of
 * {@link Attendee} objects, establishing a strong structural relationship (one-to-many) between
 * an event and its participants. This allows multiple attendees to be associated with each
 * event.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Core event information: title, date/time, location, description, category</li>
 *   <li>Reminder system with configurable minutes before event</li>
 *   <li>Attendee management with composition pattern</li>
 *   <li>CSV serialization for data persistence with input sanitization</li>
 *   <li>Formatted date/time display for user interface</li>
 * </ul>
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 2.0
 */
public class Event {
    
    /** The title or name of the event */
    protected String title;
    /** The date and time when the event occurs */
    protected LocalDateTime dateTime;
    /** The location or venue of the event */
    protected String location;
    /** A detailed description or notes about the event */
    protected String description;
    /** The category or classification of the event (e.g., "Meeting", "Birthday") */
    protected String category;
    /** The number of minutes before the event to trigger a reminder notification */
    protected int reminderMinutesBefore;
    /** The list of attendees participating in the event */
    protected List<Attendee> attendees;

    /**
     * Constructs an Event with the specified details.
     *
     * <p>Initializes a new event with all core information. The attendee list is initialized
     * internally as an empty {@link ArrayList} to prevent {@link NullPointerException} when
     * accessing the attendees collection.</p>
     *
     * @param title The title or name of the event (mandatory)
     * @param dateTime The {@link LocalDateTime} when the event occurs
     * @param location The physical or virtual location of the event
     * @param description A detailed description, notes, or additional information about the event
     * @param category The category or classification of the event
     * @param reminderMinutesBefore The number of minutes before the event to trigger a reminder
     */
    public Event(String title, LocalDateTime dateTime, String location, String description, String category, int reminderMinutesBefore) {
        this.title = title;
        this.dateTime = dateTime;
        this.location = location;
        this.description = description;
        this.category = category;
        this.reminderMinutesBefore = reminderMinutesBefore;
        this.attendees = new ArrayList<>();
    }

    /**
     * Returns the event's title.
     *
     * @return The {@link String} representing the event title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the event's title.
     *
     * @param title The new title for the event
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the event's date and time.
     *
     * @return The {@link LocalDateTime} representing when the event occurs
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Sets the event's date and time.
     *
     * @param dateTime The new {@link LocalDateTime} for the event
     */
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Returns the event's location.
     *
     * @return The {@link String} representing the event location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the event's location.
     *
     * @param location The new location for the event
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the event's description.
     *
     * @return The {@link String} containing the event description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the event's description.
     *
     * @param description The new description for the event
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the event's category.
     *
     * @return The {@link String} representing the event category (e.g., "Meeting", "Birthday")
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the event's category.
     *
     * @param category The new category for the event
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the reminder time in minutes before the event.
     *
     * @return The number of minutes before the event to display a reminder notification
     */
    public int getReminderMinutesBefore() {
        return reminderMinutesBefore;
    }

    /**
     * Sets the reminder time in minutes before the event.
     *
     * @param reminderMinutesBefore The number of minutes before the event to trigger a reminder
     */
    public void setReminderMinutesBefore(int reminderMinutesBefore) {
        this.reminderMinutesBefore = reminderMinutesBefore;
    }

    /**
     * Returns the list of attendees for this event.
     *
     * @return A {@link List} of {@link Attendee} objects associated with this event
     */
    public List<Attendee> getAttendees() {
        return attendees;
    }

    /**
     * Adds an attendee to the event's attendee list.
     *
     * <p>Adds the provided attendee to the internal composition list if the attendee object
     * is not null. This method is part of the composition pattern implementation.</p>
     *
     * @param attendee The {@link Attendee} object to be added to the event. If {@code null},
     *                 the operation is silently ignored
     */
    public void addAttendee(Attendee attendee) {
        if (attendee != null) {
            this.attendees.add(attendee);
        }
    }

    /**
     * Returns the event's date and time in a user-friendly formatted string.
     *
     * <p>The format is: "MM/dd/yyyy HH:mm" (e.g., "06/21/2026 14:30")</p>
     *
     * @return A formatted {@link String} representation of the date and time
     */
    public String getFormattedDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * Converts the event to a CSV (comma-separated values) format string for file persistence.
     *
     * <p><strong>Data Sanitization:</strong> This method applies comprehensive input sanitization
     * to all text fields to prevent delimiter conflicts during file I/O operations. The primary
     * delimiter used in the CSV format is the semicolon (;), and the secondary delimiter for
     * attendees is the pipe (|). Newline characters in descriptions are converted to spaces to
     * maintain single-line format.</p>
     *
     * <p><strong>CSV Format:</strong> The output format is:
     * {@code Title;DateTime;Location;Description;Category;Reminder;Attendees}
     * </p>
     *
     * <p><strong>Attendee Serialization:</strong> Multiple attendees are concatenated with commas,
     * where each attendee is serialized using {@link Attendee#toCSV()}.</p>
     *
     * @return A formatted {@link String} in CSV format ready for storage to disk
     */
    public String toCSV() {
        String safeTitle = title != null ? title.replace(";", ",").replace("|", "") : "";
        String safeLocation = location != null ? location.replace(";", ",").replace("|", "") : "";
        String safeDesc = description != null ? description.replace(";", ",").replace("|", "").replace("\n", " ") : "";
        String safeCat = category != null ? category.replace(";", ",").replace("|", "") : "";

        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append(safeTitle).append(";")
                  .append(dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append(";")
                  .append(safeLocation).append(";")
                  .append(safeDesc).append(";")
                  .append(safeCat).append(";")
                  .append(reminderMinutesBefore).append(";");
        
        // Concatenates the text representations of the composition list objects
        for (int i = 0; i < attendees.size(); i++) {
            csvBuilder.append(attendees.get(i).toCSV());
            if (i < attendees.size() - 1) {
                csvBuilder.append(",");
            }
        }
        
        return csvBuilder.toString();
    }

    /**
     * Returns a string representation of the event for display in the user interface.
     *
     * <p><strong>Design Pattern - Polymorphism:</strong> This method overrides the
     * {@link Object#toString()} method, providing a custom textual representation tailored
     * for UI components and logging.</p>
     *
     * <p>The format is: "Title (MM/dd/yyyy HH:mm) - Category"</p>
     *
     * @return A {@link String} representation in the format "Title (Date and Time) - Category"
     */
    @Override
    public String toString() {
        return title + " (" + getFormattedDateTime() + ") - " + category;
    }
}
