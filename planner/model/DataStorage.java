package planner.model;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DataStorage is a component responsible for data persistence operations on disk (File I/O).
 *
 * <p><strong>Architecture Pattern - Dependency Injection:</strong> This class has been refactored
 * to support Dependency Injection, allowing unit tests to use temporary files without interfering
 * with real application data. The file path can be customized via constructor parameters.</p>
 *
 * <p><strong>Design Principle - Separation of Concerns:</strong> Isolates all file reading and
 * writing logic from domain logic, ensuring that model entities do not depend on specific file
 * system implementations. This allows for flexible data storage backends in the future.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Serializes and deserializes {@link Event} and {@link RecurringEvent} objects to/from CSV
 *       format</li>
 *   <li>Fault-tolerant loading: corrupted lines are logged and skipped without failing the entire
 *       load process</li>
 *   <li>Automatic migration support for legacy recurring events without series identifiers</li>
 *   <li>Thread-safe file operations using try-with-resources statements</li>
 *   <li>Comprehensive error handling and logging for debugging</li>
 * </ul>
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 2.0
 */
public class DataStorage {

    private final String filePath;

    /**
     * Default constructor for production use.
     *
     * <p>Initializes the DataStorage instance with the standard default file path
     * "events_data.txt" for storing event data.</p>
     */
    public DataStorage() {
        this("events_data.txt");
    }

    /**
     * Constructor for Dependency Injection.
     *
     * <p>Allows configuration of a custom file path, enabling test suites to provide alternative
     * paths (e.g., temporary files) without affecting production data. This is a best practice
     * for testable code design.</p>
     *
     * @param filePath The path to the file where events will be stored and retrieved from
     */
    public DataStorage(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Serializes the in-memory event list to the local file system.
     *
     * <p>Converts each {@link Event} object to CSV format using the {@link Event#toCSV()} method
     * and writes it to the file, one event per line. Uses try-with-resources to ensure safe and
     * automatic closing of I/O streams, even if an exception occurs during writing.</p>
     *
     * <p>If an I/O error occurs, it is logged to {@link System#err} and the method completes
     * gracefully.</p>
     *
     * @param events The {@link List} of {@link Event} objects to be persisted to disk
     */
    public void saveEvents(List<Event> events) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath))) {
            for (Event event : events) {
                writer.write(event.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("I/O failure while saving events: " + e.getMessage());
        }
    }

    /**
     * Reconstructs object-oriented event entities from the persisted text file.
     *
     * <p>Reads the file line by line and deserializes each line into an {@link Event} or
     * {@link RecurringEvent} object using the {@link #parseEventLine(String)} method. This
     * method is fault-tolerant: a single corrupted line will be logged as a warning and skipped,
     * preventing the entire loading process from failing and allowing partial data recovery.</p>
     *
     * <p>If the file does not exist, returns an empty list. If critical I/O errors occur, they
     * are logged to {@link System#err}.</p>
     *
     * @return A {@link List} of reconstructed {@link Event} objects from the file, or an empty
     *         list if the file does not exist or cannot be read
     */
    public List<Event> loadEvents() {
        List<Event> events = new ArrayList<>();
        File file = new File(this.filePath);

        if (!file.exists()) {
            return events;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Event event = parseEventLine(line);
                    if (event != null) {
                        events.add(event);
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Failure to perform parsing on the line. Partial restoration applied.");
                }
            }
        } catch (IOException e) {
            System.err.println("Critical error accessing local database: " + e.getMessage());
        }

        return events;
    }

    /**
     * Assigns shared series identifiers to recurring events loaded from legacy file formats.
     *
     * <p>Legacy recurring events stored before series ID support may lack an explicit UUID in
     * the CSV format. This method identifies such events by their title and recurrence type,
     * assigns a newly generated series identifier to group them together, and updates their
     * series ID.</p>
     *
     * <p>Events with the same title and recurrence type are considered part of the same series
     * and receive the same generated UUID. This ensures that recurring event instances loaded
     * from legacy data are properly grouped and recognized as belonging to the same series.</p>
     *
     * @param events The {@link List} of loaded {@link Event} objects to process for migration
     * @return {@code true} if at least one event received a newly generated series identifier,
     *         {@code false} if no migration was necessary
     */
    public static boolean assignLegacySeriesIds(List<Event> events) {
        Map<String, String> legacyIds = new HashMap<>();
        boolean migrated = false;

        for (Event event : events) {
            if (event instanceof RecurringEvent recurringEvent && recurringEvent.getSeriesId() == null) {
                String key = recurringEvent.getTitle() + "|" + recurringEvent.getRecurrenceType();
                String seriesId = legacyIds.computeIfAbsent(key, ignored -> UUID.randomUUID().toString());
                recurringEvent.setSeriesId(seriesId);
                migrated = true;
            }
        }
        return migrated;
    }

    /**
     * Parses a single CSV line into an {@link Event} or {@link RecurringEvent} object.
     *
     * <p><strong>CSV Format:</strong> The expected format is:
     * {@code Title;DateTime;Location;Description;Category;Reminder;Attendees;[REC:RecurrenceType:SeriesId]}
     * </p>
     *
     * <p><strong>Parsing Logic:</strong></p>
     * <ul>
     *   <li>Splits the line by semicolons (;) into individual fields</li>
     *   <li>Extracts basic event information: title, date/time, location, description, category,
     *       and reminder minutes</li>
     *   <li>Parses attendee list from comma-separated entries, each with format "Name|Email"</li>
     *   <li>Detects recurrence metadata in format "[REC:RecurrenceType:SeriesId]"</li>
     *   <li>Creates either a regular {@link Event} or {@link RecurringEvent} based on the presence
     *       of recurrence data</li>
     * </ul>
     *
     * <p><strong>Error Handling:</strong> Returns {@code null} if the line has fewer than 7 fields
     * or if parsing any field fails. Exceptions during parsing are propagated to the caller for
     * logging and recovery.</p>
     *
     * @param line The raw CSV string from the file containing event data
     * @return A reconstructed {@link Event} or {@link RecurringEvent} object, or {@code null} if
     *         the line is malformed or has insufficient fields
     * @throws NumberFormatException If the reminder field cannot be parsed as an integer
     * @throws java.time.format.DateTimeParseException If the date/time field is not in ISO format
     */
    private Event parseEventLine(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length < 7) return null;

        String title = parts[0];
        LocalDateTime dateTime = LocalDateTime.parse(parts[1], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String location = parts[2];
        String description = parts[3];
        String category = parts[4];
        int reminder = Integer.parseInt(parts[5]);

        RecurringEvent.RecurrenceType recurrenceType = RecurringEvent.RecurrenceType.NONE;
        String seriesId = null;
        boolean isRecurring = false;

        if (parts.length >= 8 && parts[7].startsWith("[REC:") && parts[7].endsWith("]")) {
            isRecurring = true;
            String inner = parts[7].substring(5, parts[7].length() - 1);
            int separatorIndex = inner.indexOf(':');

            if (separatorIndex == -1) {
                recurrenceType = RecurringEvent.RecurrenceType.valueOf(inner);
            } else {
                recurrenceType = RecurringEvent.RecurrenceType.valueOf(inner.substring(0, separatorIndex));
                seriesId = inner.substring(separatorIndex + 1);
                if (seriesId.isEmpty()) seriesId = null;
            }
        }

        Event event = isRecurring ? 
            new RecurringEvent(title, dateTime, location, description, category, reminder, recurrenceType, seriesId) :
            new Event(title, dateTime, location, description, category, reminder);

        String attendeesStr = parts[6];
        if (!attendeesStr.trim().isEmpty()) {
            String[] attendeeList = attendeesStr.split(",");
            for (String attendeeEntry : attendeeList) {
                String[] attendeeData = attendeeEntry.split("\\|");
                if (attendeeData.length == 2) {
                    event.addAttendee(new Attendee(attendeeData[0], attendeeData[1]));
                }
            }
        }
        return event;
    }
}
