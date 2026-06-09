package planner.controller;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import planner.exception.InvalidEventException;
import planner.model.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * EventController manages all event-related operations including creation, updating,
 * deletion, and retrieval of events. This controller acts as the business logic layer
 * between the View and the data storage layer.
 *
 * It handles:
 * - Regular single events and recurring events
 * - Event validation and persistence
 * - Attendee management
 * - Event search and filtering
 * - Event export functionality
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho 
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 1.0
 */
public class EventController {
    
    // In-memory list that holds events while the program is running
    private final List<Event> events;

    // Responsible for saving/loading events from file storage
    private final DataStorage storage;

    /**
     * Constructor initializes the EventController by loading all events from storage.
     * The events are loaded into memory for faster access during the application runtime.
     */
    public EventController() {
        this.storage = new DataStorage();
        this.events = new ArrayList<>(storage.loadEvents());
    }

    /**
     * Creates a new single (non-recurring) event and saves it to storage.
     *
     * The method performs the following steps:
     * 1. Validates all input fields (title, dateTime, category)
     * 2. Creates a new Event object
     * 3. Adds the event to the in-memory list
     * 4. Persists the updated events list to file storage
     * 5. Returns the created event for further use by the caller
     *
     * @param title            The title/name of the event (non-empty string required)
     * @param dateTime         The date and time of the event (non-null required)
     * @param location         The physical location or venue of the event (nullable)
     * @param description      Additional details about the event (nullable)
     * @param category         The category/type of the event (non-empty string required)
     * @param reminderMinutes  Number of minutes before the event to trigger a reminder
     *
     * @return The created Event object
     * @throws InvalidEventException If any required field is invalid or empty
     */
    public Event createEvent(String title, LocalDateTime dateTime, String location,
                             String description, String category, int reminderMinutes)
            throws InvalidEventException {

        // Step 1: Validate all required fields
        validateEventFields(title, dateTime, category);

        // Step 2: Create the event object
        Event event = new Event(title, dateTime, location, description, category, reminderMinutes);

        // Step 3: Add to in-memory list
        events.add(event);

        // Step 4: Persist to storage
        storage.saveEvents(events);

        // Step 5: Return the created event
        return event;
    }

    /**
     * Creates a new recurring event with specified recurrence pattern and saves it to storage.
     *
     * This method generates multiple instances of the same event based on the recurrence type
     * and repeat count. For example, if you specify DAILY with repeat=5, it will create
     * 5 event instances, each 1 day apart.
     *
     * The method performs the following steps:
     * 1. Validates all input fields
     * 2. Validates the recurrence type is not NONE
     * 3. Creates the initial recurring event
     * 4. Generates future occurrences based on the recurrence pattern
     * 5. Persists all instances to storage
     *
     * @param title            The title/name of the recurring event (non-empty string required)
     * @param dateTime         The date and time of the first occurrence (non-null required)
     * @param location         The physical location or venue (nullable)
     * @param description      Additional event details (nullable)
     * @param category         The category/type of the event (non-empty string required)
     * @param reminderMinutes  Number of minutes before each occurrence to trigger a reminder
     * @param recurrenceType   The pattern for recurrence (DAILY, WEEKLY, MONTHLY, or NONE)
     * @param repeat           The number of times this event should repeat
     *
     * @return The initial RecurringEvent object that was created
     * @throws InvalidEventException If any field is invalid, empty, or recurrence type is NONE
     *
     * @see RecurringEvent.RecurrenceType
     */
    public RecurringEvent createRecurringEvent(String title, LocalDateTime dateTime,
                                               String location, String description, String category,
                                               int reminderMinutes, RecurringEvent.RecurrenceType recurrenceType, int repeat)
            throws InvalidEventException {

        // Validate all required fields
        validateEventFields(title, dateTime, category);

        // Ensure recurrence type is valid (not NONE)
        if (recurrenceType == null || recurrenceType == RecurringEvent.RecurrenceType.NONE) {
            throw new InvalidEventException("Recurrence type is invalid.");
        }

        // Create the initial recurring event
        RecurringEvent event = new RecurringEvent(title, dateTime, location,
                description, category, reminderMinutes, recurrenceType);
        events.add(event);

        // Calculate the number of future occurrences to generate (repeat count minus 1)
        int occurrencesToGenerate = switch (recurrenceType) {
            case DAILY -> repeat - 1;
            case WEEKLY -> repeat - 1;
            case MONTHLY -> repeat - 1;
            default -> 0;
        };

        // Generate all future occurrences
        LocalDateTime nextDateTime = dateTime;
        for (int i = 0; i < occurrencesToGenerate; i++) {
            // Calculate the next date based on recurrence type
            nextDateTime = switch (recurrenceType) {
                case DAILY -> nextDateTime.plusDays(1);
                case WEEKLY -> nextDateTime.plusWeeks(1);
                case MONTHLY -> nextDateTime.plusMonths(1);
                default -> nextDateTime;
            };
            // Add the new occurrence to events list
            events.add(new RecurringEvent(title, nextDateTime, location,
                    description, category, reminderMinutes, recurrenceType));
        }

        // Persist all events to storage
        storage.saveEvents(events);
        return event;
    }

    /**
     * Updates an existing single event with new information.
     *
     * This method updates all fields of the given event object and persists
     * the changes to storage. The event object is modified in-place since it's
     * already part of the events list.
     *
     * @param event           The Event object to update (must already exist in the list)
     * @param title           The new title (non-empty string required)
     * @param dateTime        The new date and time (non-null required)
     * @param location        The new location (nullable)
     * @param description     The new description (nullable)
     * @param category        The new category (non-empty string required)
     * @param reminderMinutes The new reminder time in minutes
     *
     * @throws InvalidEventException If any required field is invalid or empty
     */
    public void updateEvent(Event event, String title, LocalDateTime dateTime,
                            String location, String description, String category, int reminderMinutes)
            throws InvalidEventException {

        // Validate all required fields before applying changes
        validateEventFields(title, dateTime, category);

        // Update all fields of the event object
        event.setTitle(title);
        event.setDateTime(dateTime);
        event.setLocation(location);
        event.setDescription(description);
        event.setCategory(category);
        event.setReminderMinutesBefore(reminderMinutes);

        // Persist changes to storage
        storage.saveEvents(events);
    }

    /**
     * Updates all future occurrences of a recurring event starting from a specified instance.
     *
     * This method finds all occurrences of a recurring event that happen at or after
     * the given event's date and applies the updates to all of them. It maintains
     * the time difference between occurrences by calculating the time shift.
     *
     * For example, if you move one occurrence forward by 1 hour, all future occurrences
     * will also be shifted forward by 1 hour to maintain the recurring pattern.
     *
     * @param event           The recurring event instance to start updating from
     * @param title           The new title for all future occurrences
     * @param dateTime        The new date and time for this occurrence
     * @param location        The new location for all future occurrences
     * @param description     The new description for all future occurrences
     * @param category        The new category for all future occurrences
     * @param reminderMinutes The new reminder time for all future occurrences
     *
     * @throws InvalidEventException If any required field is invalid or empty
     */
    public void updateFutureOccurrences(RecurringEvent event, String title,
                                        LocalDateTime dateTime, String location, String description,
                                        String category, int reminderMinutes) throws InvalidEventException {

        // Validate all required fields
        validateEventFields(title, dateTime, category);

        // Store the original title to identify all matching recurring events
        String originalTitle = event.getTitle();

        // Calculate the time difference between old and new dateTime in minutes
        // This allows us to maintain consistent intervals between recurring occurrences
        long timeDifferenceMinutes = java.time.Duration
                .between(event.getDateTime(), dateTime)
                .toMinutes();

        // Update all future occurrences of this recurring event
        events.stream()
                .filter(e -> e instanceof RecurringEvent
                        && e.getTitle().equals(originalTitle)
                        && !e.getDateTime().isBefore(event.getDateTime()))
                .forEach(e -> {
                    e.setTitle(title);
                    e.setDateTime(e.getDateTime().plusMinutes(timeDifferenceMinutes));
                    e.setLocation(location);
                    e.setDescription(description);
                    e.setCategory(category);
                    e.setReminderMinutesBefore(reminderMinutes);
                });

        // Persist changes to storage
        storage.saveEvents(events);
    }

    /**
     * Deletes a single event from the system.
     *
     * Removes the event from the in-memory list and persists the updated
     * list to storage. If the event is a recurring event, only that specific
     * occurrence is deleted (to delete all future occurrences, use deleteFutureOccurrences).
     *
     * @param event The Event object to delete (must exist in the events list)
     */
    public void deleteEvent(Event event) {
        // Remove the event from the in-memory list
        events.remove(event);
        // Persist the change to storage
        storage.saveEvents(events);
    }

    /**
     * Deletes all future occurrences of a recurring event starting from a specified instance.
     *
     * This method removes the specified occurrence and all subsequent occurrences
     * of the same recurring event, while preserving past occurrences.
     *
     * For example, if a daily event is scheduled for 10 days and you call this method
     * on day 5, it will delete occurrences 5-10 but keep occurrences 1-4.
     *
     * @param event The recurring event instance to start deleting from
     */
    public void deleteFutureOccurrences(RecurringEvent event) {
        // Remove all occurrences that match the event title and occur at or after the given date
        events.removeIf(e ->
                e instanceof RecurringEvent
                        && e.getTitle().equals(event.getTitle())
                        && !e.getDateTime().isBefore(event.getDateTime())
        );
        // Persist the change to storage
        storage.saveEvents(events);
    }

    /**
     * Retrieves all events scheduled for a specific date.
     *
     * This method filters all events by the given date (ignoring time of day)
     * and returns them sorted chronologically by time.
     *
     * @param date The date to retrieve events for
     *
     * @return A list of Event objects scheduled for the given date, sorted by time.
     *         Returns an empty list if no events are found for that date.
     */
    public List<Event> getEventsForDate(LocalDate date) {
        return events.stream()
                // Filter events that occur on the specified date
                .filter(e -> e.getDateTime().toLocalDate().equals(date))
                // Sort events chronologically by start time
                .sorted((a, b) -> a.getDateTime().compareTo(b.getDateTime()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all distinct dates in a specific month that have at least one event.
     *
     * This method is useful for calendar views where you need to highlight which
     * days have scheduled events.
     *
     * @param year  The year (e.g., 2024)
     * @param month The month as an integer (1-12, where 1 is January)
     *
     * @return A list of distinct LocalDate objects for each day in the specified month
     *         that has at least one event. Returns an empty list if no events exist
     *         in that month.
     */
    public List<LocalDate> getDatesWithEventsInMonth(int year, int month) {
        return events.stream()
                // Extract the local date from each event
                .map(e -> e.getDateTime().toLocalDate())
                // Filter events that belong to the specified year and month
                .filter(d -> d.getYear() == year && d.getMonthValue() == month)
                // Remove duplicate dates (keep each date only once)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Searches for events matching a keyword in their title, location, description, or category.
     *
     * The search is case-insensitive and returns all events where the keyword is found
     * in any of the searchable fields.
     *
     * @param keyword The search term to look for (case-insensitive substring match)
     *
     * @return A list of Event objects that match the search criteria.
     *         Returns an empty list if no matches are found.
     */
    public List<Event> searchEvents(String keyword) {
        // Convert keyword to lowercase for case-insensitive comparison
        String lowerKeyword = keyword.toLowerCase();
        return events.stream()
                // Include event if keyword is found in any searchable field
                .filter(e ->
                        e.getTitle().toLowerCase().contains(lowerKeyword)
                                || (e.getLocation() != null && e.getLocation().toLowerCase().contains(lowerKeyword))
                                || (e.getDescription() != null && e.getDescription().toLowerCase().contains(lowerKeyword))
                                || (e.getCategory() != null && e.getCategory().toLowerCase().contains(lowerKeyword))
                )
                .collect(Collectors.toList());
    }

    /**
     * Adds a single attendee to an event.
     *
     * This method validates the attendee information and adds them to the event's
     * attendee list. The attendee information is then persisted to storage.
     *
     * @param event The Event object to add the attendee to
     * @param name  The full name of the attendee (non-empty string required)
     * @param email The email address of the attendee (must contain "@" symbol)
     *
     * @throws InvalidEventException If the name is empty or the email is invalid
     */
    public void addAttendee(Event event, String name, String email)
            throws InvalidEventException {

        // Validate attendee name is not empty
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidEventException("Attendee name cannot be empty.");
        }

        // Validate email contains the "@" symbol (basic email validation)
        if (email == null || !email.contains("@")) {
            throw new InvalidEventException("Email address is invalid.");
        }

        // Create attendee object and add to event
        event.addAttendee(new Attendee(name.trim(), email.trim()));
        // Persist changes to storage
        storage.saveEvents(events);
    }

    /**
     * Adds a single attendee to all future occurrences of a recurring event.
     *
     * This method finds all occurrences of the recurring event that happen at or after
     * the specified event instance and adds the attendee to each of them.
     *
     * @param event The recurring event instance to start adding attendees from
     * @param name  The full name of the attendee (non-empty string required)
     * @param email The email address of the attendee (must contain "@" symbol)
     *
     * @throws InvalidEventException If the name is empty or the email is invalid
     */
    public void addAttendeeToFutureOccurrences(
            RecurringEvent event,
            String name,
            String email)
            throws InvalidEventException {

        // Validate attendee name is not empty
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidEventException("Attendee name cannot be empty.");
        }

        // Validate email contains the "@" symbol (basic email validation)
        if (email == null || !email.contains("@")) {
            throw new InvalidEventException("Email address is invalid.");
        }

        // Create the attendee object once to add to multiple events
        Attendee newAttendee = new Attendee(name.trim(), email.trim());

        // Add attendee to all future occurrences of this recurring event
        events.stream()
                .filter(e -> e instanceof RecurringEvent
                        && e.getTitle().equals(event.getTitle())
                        && !e.getDateTime().isBefore(event.getDateTime()))
                .forEach(e -> e.addAttendee(newAttendee));

        // Persist changes to storage
        storage.saveEvents(events);
    }

    /**
     * Exports all events for a specific day to a text file.
     *
     * The exported file contains a formatted list of events with title, time, location,
     * and category information. If no events exist for the day, the file will indicate
     * that there are no events.
     *
     * @param date     The date to export events for
     * @param filePath The file path where the export should be saved
     *                 (e.g., "/tmp/events_2024-01-15.txt" or "C:\\exports\\events.txt")
     *
     * @throws java.io.IOException If there is an error writing to the file
     *         (e.g., invalid path, insufficient permissions)
     */
    public void exportDayToFile(LocalDate date, String filePath) throws java.io.IOException {
        // Retrieve all events for the specified date
        List<Event> dayEvents = getEventsForDate(date);

        // Open a file writer and print events
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.FileWriter(filePath))) {

            // Write header
            pw.println("Events for day " + date);
            pw.println("=".repeat(40));

            // Check if there are any events for this day
            if (dayEvents.isEmpty()) {
                pw.println("No events scheduled for this day.");
            } else {
                // Write each event's details
                for (Event e : dayEvents) {
                    pw.println("Title:     " + e.getTitle());
                    pw.println("Time:      " + e.getFormattedDateTime());
                    pw.println("Location:  " + e.getLocation());
                    pw.println("Category:  " + e.getCategory());
                    pw.println("-".repeat(40));
                }
            }
        }
    }

    /**
     * Validates that required event fields are not null and not empty.
     *
     * This is an internal validation method used by event creation and update methods
     * to ensure data integrity before persisting to storage.
     *
     * @param title    The event title to validate
     * @param dateTime The event date and time to validate
     * @param category The event category to validate
     *
     * @throws InvalidEventException If any required field is null, empty, or contains only whitespace
     */
    private void validateEventFields(String title, LocalDateTime dateTime, String category)
            throws InvalidEventException {

        // Validate title is not empty
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidEventException("Event title cannot be empty.");
        }
        // Validate dateTime is not null
        if (dateTime == null) {
            throw new InvalidEventException("Date and time are required.");
        }
        // Validate category is not empty
        if (category == null || category.trim().isEmpty()) {
            throw new InvalidEventException("Event category cannot be empty.");
        }
    }

    /**
     * Retrieves a copy of all events currently stored in the system.
     *
     * This method returns a copy of the internal events list to prevent external
     * code from directly modifying the controller's internal state. Always use the
     * update and delete methods to make changes to events.
     *
     * @return A new ArrayList containing all Event objects in the system.
     *         Returns an empty list if no events exist.
     */
    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }
}
