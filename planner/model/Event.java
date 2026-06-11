package planner.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: Base domain model class representing an event in the calendar.
 *
 * OOP Concept Applied - PREPARATION FOR INHERITANCE:
 * The attributes use the 'protected' access modifier. This encapsulates the data
 * from external classes while allowing subclasses (such as RecurringEvent) to have
 * direct access to the inherited attributes.
 *
 * OOP Concept Applied - COMPOSITION:
 * The Event class contains a list of Attendee objects, establishing a strong
 * structural relationship between the event and its participants.
 */
public class Event {
    
    protected String title;
    protected LocalDateTime dateTime;
    protected String location;
    protected String description;
    protected String category;
    protected int reminderMinutesBefore; 
    protected List<Attendee> attendees;

/**
 * Constructor for the base Event class.
 * The attendee list is initialized internally to prevent NullPointerException.
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

    // Access methods (getters and setters)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getReminderMinutesBefore() { return reminderMinutesBefore; }
    public void setReminderMinutesBefore(int reminderMinutesBefore) { this.reminderMinutesBefore = reminderMinutesBefore; }

    public List<Attendee> getAttendees() { return attendees; }

/**
 * Adds an attendee to the event's structural list.
 * @param attendee Attendee object to be added
 */
    public void addAttendee(Attendee attendee) {
        if (attendee != null) {
            this.attendees.add(attendee);
        }
    }

/**
 * Returns the formatted date and time for display (dd/MM/yyyy HH:mm).
 */
    public String getFormattedDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

/**
 * Converts the current state of the object into a delimiter-separated String.
 * Applies sanitization to text fields to prevent delimiter (;) conflicts
 * during the file I/O process.
 *
 * @return A formatted String ready for storage.
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
 * Returns a textual representation of the event for display in the UI.
 * @return A formatted String in the format "Title (Date and Time) - Category"
 */
    @Override
    public String toString() {
        return title + " (" + getFormattedDateTime() + ") - " + category;
    }
}