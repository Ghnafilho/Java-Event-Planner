package planner.controller;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import planner.exception.InvalidEventException;
import planner.model.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * EventController manages all event-related operations including creation, updating, deletion,
 * and retrieval of events. This controller acts as the business logic layer between the View and
 * the data storage layer.
 *
 * <p><strong>Architecture Pattern - MVC Controller:</strong> This class implements the Controller
 * component of the Model-View-Controller pattern, mediating between the {@link planner.view.MainFrame}
 * (View) and the {@link planner.model} classes (Model), while delegating persistence to
 * {@link DataStorage}.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Regular single events and recurring events management</li>
 *   <li>Event validation and persistence</li>
 *   <li>Attendee management and assignment</li>
 *   <li>Event search and filtering capabilities</li>
 *   <li>Event export functionality to file formats</li>
 *   <li>Data integrity through validation and error handling</li>
 * </ul>
 *
 * <p><strong>Event Management:</strong> The controller maintains an in-memory list of events
 * for fast access during runtime and uses {@link DataStorage} to persist changes to disk.
 * All modifications go through the controller to ensure data consistency.</p>
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 2.0
 * @see DataStorage
 * @see planner.model.Event
 * @see planner.model.RecurringEvent
 * @see planner.exception.InvalidEventException
 */
public class EventController {
    
    /** In-memory list that holds events while the application is running */
    private final List<Event> events;

    /** Responsible for saving and loading events from file storage */
    private final DataStorage storage;

    /**
     * Constructs an EventController with default storage configuration.
     *
     * <p>Initializes the controller by creating a default {@link DataStorage} instance and
     * loading all events from the standard "events_data.txt" file. This is the primary
     * constructor used for production code.</p>
     */
    public EventController() {
        this(new DataStorage());
    }

    /**
     * Constructs an EventController with a custom storage instance.
     *
     * <p>Initializes the controller with the provided {@link DataStorage} instance and loads
     * all events from storage into memory. This constructor supports Dependency Injection,
     * allowing for flexible storage backends and testable code.</p>
     *
     * <p><strong>Legacy Migration:</strong> If legacy recurring events without series identifiers
     * are detected, new identifiers are automatically assigned and the updated events are saved
     * back to storage.</p>
     *
     * @param storage The {@link DataStorage} instance to use for persistence operations
     */
    public EventController(DataStorage storage) {
        this.storage = storage;
        List<Event> loadedEvents = this.storage.loadEvents();
        this.events = new ArrayList<>(loadedEvents);

        if (DataStorage.assignLegacySeriesIds(loadedEvents)) {
            this.storage.saveEvents(events);
        }
    }

    /**
     * Creates a new single (non-recurring) event and persists it to storage.
     *
     * <p><strong>Execution Steps:</strong></p>
     * <ol>
     *   <li>Validates all required input fields (title, dateTime, category)</li>
     *   <li>Creates a new {@link Event} object with the provided information</li>
     *   <li>Adds the event to the in-memory event list</li>
     *   <li>Persists the updated event list to file storage</li>
     *   <li>Returns the created event for further use by the caller</li>
     * </ol>
     *
     * @param title           The title or name of the event. Must be non-empty and non-null
     * @param dateTime        The date and time when the event occurs. Must be non-null
     * @param location        The physical location or venue of the event. May be {@code null} or
     *                        empty
     * @param description     Additional details, notes, or agenda items about the event. May be
     *                        {@code null} or empty
     * @param category        The category or type of the event (e.g., "Meeting", "Birthday").
     *                        Must be non-empty and non-null
     * @param reminderMinutes Number of minutes before the event to trigger a reminder
     *                        notification
     * @return The created {@link Event} object containing all provided information
     * @throws InvalidEventException If any required field is {@code null}, empty, or contains
     *                               only whitespace
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
     * Creates a new recurring event with specified recurrence pattern and persists all
     * occurrences to storage.
     *
     * <p><strong>Execution Logic:</strong> This method generates multiple instances of the same
     * event based on the recurrence type and repeat count. For example, if you specify
     * {@link RecurringEvent.RecurrenceType#DAILY} with repeat=5, it will create 5 event
     * instances, each 1 day apart, all sharing the same series identifier.</p>
     *
     * <p><strong>Execution Steps:</strong></p>
     * <ol>
     *   <li>Validates all required input fields</li>
     *   <li>Validates that recurrence type is not {@link RecurringEvent.RecurrenceType#NONE}</li>
     *   <li>Generates a unique series identifier using {@link UUID#randomUUID()}</li>
     *   <li>Creates the initial {@link RecurringEvent} instance</li>
     *   <li>Generates (repeat - 1) future occurrences based on the recurrence pattern</li>
     *   <li>Persists all event instances to storage as a single atomic operation</li>
     * </ol>
     *
     * @param title           The title or name of the recurring event. Must be non-empty and
     *                        non-null
     * @param dateTime        The date and time of the first occurrence. Must be non-null
     * @param location        The physical location or venue. May be {@code null} or empty
     * @param description     Additional event details. May be {@code null} or empty
     * @param category        The event category. Must be non-empty and non-null
     * @param reminderMinutes Number of minutes before each occurrence to trigger a reminder
     * @param recurrenceType  The pattern for recurrence:
     *                        <ul>
     *                          <li>{@link RecurringEvent.RecurrenceType#DAILY}: Repeats every day</li>
     *                          <li>{@link RecurringEvent.RecurrenceType#WEEKLY}: Repeats every week</li>
     *                          <li>{@link RecurringEvent.RecurrenceType#MONTHLY}: Repeats every month</li>
     *                          <li>{@link RecurringEvent.RecurrenceType#NONE}: Not allowed, throws
     *                              exception</li>
     *                        </ul>
     * @param repeat          The number of times this event should repeat (total occurrences).
     *                        For example, repeat=3 creates 3 events spaced according to the
     *                        recurrence type
     * @return The initial {@link RecurringEvent} object that was created (the first occurrence)
     * @throws InvalidEventException If any field is invalid, empty, recurrence type is
     *                               {@link RecurringEvent.RecurrenceType#NONE}, or repeat is not
     *                               valid
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

        String seriesId = UUID.randomUUID().toString();

        RecurringEvent event = new RecurringEvent(title, dateTime, location,
                description, category, reminderMinutes, recurrenceType, seriesId);
        events.add(event);

        int occurrencesToGenerate = switch (recurrenceType) {
            case DAILY -> repeat - 1;
            case WEEKLY -> repeat - 1;
            case MONTHLY -> repeat - 1;
            default -> 0;
        };

        LocalDateTime nextDateTime = dateTime;
        for (int i = 0; i < occurrencesToGenerate; i++) {
            nextDateTime = switch (recurrenceType) {
                case DAILY -> nextDateTime.plusDays(1);
                case WEEKLY -> nextDateTime.plusWeeks(1);
                case MONTHLY -> nextDateTime.plusMonths(1);
                default -> nextDateTime;
            };
            events.add(new RecurringEvent(title, nextDateTime, location,
                    description, category, reminderMinutes, recurrenceType, seriesId));
        }

        // Persist all events to storage
        storage.saveEvents(events);
        return event;
    }

    /**
     * Updates an existing single event with new information.
     *
     * <p>This method modifies all fields of the given event object in-place (since it's already
     * part of the in-memory event list) and immediately persists the changes to storage.</p>
     *
     * @param event           The {@link Event} object to update. Must already exist in the events
     *                        list
     * @param title           The new title for the event. Must be non-empty and non-null
     * @param dateTime        The new date and time. Must be non-null
     * @param location        The new location. May be {@code null} or empty
     * @param description     The new description. May be {@code null} or empty
     * @param category        The new category. Must be non-empty and non-null
     * @param reminderMinutes The new reminder time in minutes before the event
     * @throws InvalidEventException If any required field is {@code null}, empty, or contains
     *                               only whitespace
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
     * <p><strong>Behavior:</strong> This method finds all occurrences of the recurring event with
     * the same series ID that happen at or after (using date/time comparison) the given event
     * instance and applies the updates to all of them. This enables bulk editing of a recurring
     * event series while preserving past occurrences.</p>
     *
     * <p><strong>Time Shift Calculation:</strong> To maintain the recurring pattern when
     * modifying dates, the method calculates the time difference between the original event's
     * date/time and the new date/time, then applies this same shift to all future occurrences.
     * For example, if the specified occurrence is moved forward by 1 hour, all subsequent
     * occurrences are also shifted forward by 1 hour.</p>
     *
     * @param event           The {@link RecurringEvent} instance to start updating from. This
     *                        event and all later occurrences in the same series will be updated
     * @param title           The new title for all future occurrences. Must be non-empty and
     *                        non-null
     * @param dateTime        The new date and time for this specific occurrence. The time shift
     *                        will be applied to all future occurrences. Must be non-null
     * @param location        The new location for all future occurrences. May be {@code null} or
     *                        empty
     * @param description     The new description for all future occurrences. May be {@code null}
     *                        or empty
     * @param category        The new category for all future occurrences. Must be non-empty and
     *                        non-null
     * @param reminderMinutes The new reminder time for all future occurrences
     * @throws InvalidEventException If any required field is {@code null}, empty, or contains
     *                               only whitespace
     */
    public void updateFutureOccurrences(RecurringEvent event, String title,
                                        LocalDateTime dateTime, String location, String description,
                                        String category, int reminderMinutes) throws InvalidEventException {

        // Validate all required fields
        validateEventFields(title, dateTime, category);

        long timeDifferenceMinutes = java.time.Duration
                .between(event.getDateTime(), dateTime)
                .toMinutes();

        events.stream()
                .filter(e -> isSameSeriesOccurrence(event, e))
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
     * <p>Removes the event from the in-memory list and persists the updated list to storage.
     * If the event is a recurring event, only that specific occurrence is deleted. To delete
     * all future occurrences of a recurring event, use
     * {@link #deleteFutureOccurrences(RecurringEvent)}.</p>
     *
     * @param event The {@link Event} object to delete. Must exist in the events list
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
     * <p>This method removes the specified occurrence and all subsequent occurrences of the same
     * recurring event (identified by matching series ID), while preserving past occurrences that
     * happen before the specified event.</p>
     *
     * <p><strong>Example:</strong> If a daily event is scheduled for 10 consecutive days and
     * you call this method on day 5, occurrences 5-10 are deleted while occurrences 1-4 are
     * preserved.</p>
     *
     * @param event The {@link RecurringEvent} instance to start deleting from. This event and
     *              all later occurrences in the same series will be removed
     */
    public void deleteFutureOccurrences(RecurringEvent event) {
        events.removeIf(e -> isSameSeriesOccurrence(event, e));
        // Persist the change to storage
        storage.saveEvents(events);
    }

    /**
     * Retrieves all events scheduled for a specific date.
     *
     * <p>Filters all events by the given date (ignoring time of day) and returns them sorted
     * chronologically by time of day. This is commonly used by the calendar view to display
     * all events for a selected date.</p>
     *
     * @param date The {@link LocalDate} to retrieve events for
     * @return A {@link List} of {@link Event} objects scheduled for the given date, sorted by
     *         time of day in ascending order. Returns an empty list if no events are scheduled
     *         for that date
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
     * <p>Returns a list of unique {@link LocalDate} objects representing each day in the
     * specified month that has one or more scheduled events. This is particularly useful for
     * calendar UI components that need to highlight or mark days with events.</p>
     *
     * @param year  The year (e.g., 2024, 2025)
     * @param month The month as an integer (1-12, where 1 is January and 12 is December)
     * @return A {@link List} of distinct {@link LocalDate} objects for each day in the specified
     *         month that has at least one event. Returns an empty list if no events exist in
     *         that month
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
     * Searches for events matching a keyword in multiple fields.
     *
     * <p><strong>Search Scope:</strong> The search is case-insensitive and looks for substring
     * matches in the following event fields:
     * <ul>
     *   <li>Event title</li>
     *   <li>Event location</li>
     *   <li>Event description</li>
     *   <li>Event category</li>
     * </ul>
     *
     * <p>An event matches if the keyword appears in any of these fields, allowing flexible
     * searches across multiple aspects of events.</p>
     *
     * @param keyword The search term to look for. The search is case-insensitive and finds
     *                substring matches
     * @return A {@link List} of {@link Event} objects that match the search criteria. Returns
     *         an empty list if no events match the keyword or if the keyword is
     *         {@code null}/empty
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
     * <p>Validates the attendee information (name and email) and adds a new {@link Attendee}
     * object to the event's attendee list. The changes are immediately persisted to storage.</p>
     *
     * @param event The {@link Event} object to add the attendee to
     * @param name  The full name of the attendee. Must be non-empty after trimming whitespace
     * @param email The email address of the attendee. Must contain the "@" symbol for basic
     *              validation
     * @throws InvalidEventException If the name is empty or the email is invalid (missing "@"
     *                               symbol)
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
     * <p>Finds all occurrences of the recurring event with the same series ID that happen at or
     * after the specified event instance and adds the same attendee to each of them. This enables
     * bulk attendee assignment for a recurring event series.</p>
     *
     * @param event The {@link RecurringEvent} instance to start adding attendees from. This
     *              event and all later occurrences in the same series will have the attendee
     *              added
     * @param name  The full name of the attendee. Must be non-empty after trimming whitespace
     * @param email The email address of the attendee. Must contain the "@" symbol for basic
     *              validation
     * @throws InvalidEventException If the name is empty or the email is invalid (missing "@"
     *                               symbol)
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

        String trimmedName = name.trim();
        String trimmedEmail = email.trim();

        events.stream()
                .filter(e -> isSameSeriesOccurrence(event, e))
                .forEach(e -> e.addAttendee(new Attendee(trimmedName, trimmedEmail)));

        storage.saveEvents(events);
    }

    /**
     * Exports all events for a specific day to a formatted text file.
     *
     * <p>Creates a new file at the specified path containing a formatted list of all events
     * scheduled for the given date. Each event's title, formatted date/time, location, and
     * category are written to the file. If no events exist for the day, the file will indicate
     * that no events are scheduled.</p>
     *
     * <p><strong>File Format:</strong> The exported file includes:
     * <ul>
     *   <li>Header with the date</li>
     *   <li>Formatted event entries with title, time, location, and category</li>
     *   <li>Separator lines for readability</li>
     *   <li>Message if no events are scheduled</li>
     * </ul>
     *
     * @param date     The {@link LocalDate} to export events for
     * @param filePath The file path where the export should be saved. Examples:
     *                 <ul>
     *                   <li>Unix/Linux: "/tmp/events_2024-01-15.txt"</li>
     *                   <li>Windows: "C:\\exports\\events.txt"</li>
     *                   <li>Relative: "./exports/events_today.txt"</li>
     *                 </ul>
     * @throws java.io.IOException If there is an error writing to the file, such as:
     *                             <ul>
     *                               <li>Invalid file path</li>
     *                               <li>Insufficient permissions to write to the directory</li>
     *                               <li>Disk space issues</li>
     *                             </ul>
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
     * Checks if a candidate event belongs to the same recurring series and occurs at or after
     * the reference event.
     *
     * <p><strong>Matching Criteria:</strong> An event matches if:
     * <ul>
     *   <li>It is an instance of {@link RecurringEvent}</li>
     *   <li>It has the same series ID as the reference event</li>
     *   <li>Its date/time is equal to or after the reference event's date/time</li>
     * </ul>
     * </p>
     *
     * <p>This helper method is used internally by update and delete operations to identify
     * which occurrences should be modified or removed when managing recurring events.</p>
     *
     * @param reference The reference {@link RecurringEvent} to compare against
     * @param candidate The {@link Event} to check for series membership and date comparison
     * @return {@code true} if the candidate is part of the same series and occurs at or after
     *         the reference event, {@code false} otherwise
     */
    private boolean isSameSeriesOccurrence(RecurringEvent reference, Event candidate) {
        if (!(candidate instanceof RecurringEvent recurringCandidate)) {
            return false;
        }

        return reference.getSeriesId() != null
                && reference.getSeriesId().equals(recurringCandidate.getSeriesId())
                && !candidate.getDateTime().isBefore(reference.getDateTime());
    }

    /**
     * Validates that all required event fields contain valid, non-empty values.
     *
     * <p>This internal validation method is used by event creation and update methods to ensure
     * data integrity before persisting changes to storage. It checks that required fields are
     * not {@code null}, not empty strings, and not whitespace-only strings.</p>
     *
     * @param title    The event title to validate
     * @param dateTime The event date and time to validate
     * @param category The event category to validate
     * @throws InvalidEventException If any required field is {@code null}, empty, or contains
     *                               only whitespace characters
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
     * <p>Returns a new {@link ArrayList} containing all {@link Event} objects in the controller.
     * By returning a copy rather than a direct reference to the internal list, this method
     * prevents external code from directly modifying the controller's internal state. All
     * modifications should go through the controller's dedicated methods like
     * {@link #updateEvent(Event, String, LocalDateTime, String, String, String, int)},
     * {@link #deleteEvent(Event)}, and {@link #addAttendee(Event, String, String)}.</p>
     *
     * @return A new {@link ArrayList} containing copies of all {@link Event} objects in the
     *         system. Returns an empty list if no events exist. Modifications to the returned
     *         list do not affect the controller's internal state
     */
    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }
}
