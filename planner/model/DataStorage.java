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
 * Component responsible for data persistence on disk (File I/O).
 * Refactored to support Dependency Injection, allowing unit tests 
 * to use temporary files without interfering with real data.
 * * Applied Architecture Principle - SEPARATION OF CONCERNS:
 * Isolates file reading and writing logic from domain logic, ensuring that
 * model entities do not depend on specific file system implementations.
 */
public class DataStorage {

    private final String filePath;

    /**
     * Default constructor for production use.
     * Defaults to the standard "events_data.txt" file.
     */
    public DataStorage() {
        this("events_data.txt");
    }

    /**
     * Constructor for Dependency Injection.
     * Allows test suites to provide custom file paths (e.g., temporary files).
     * * @param filePath The path to the file where events will be stored.
     */
    public DataStorage(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Serializes the in-memory data structure to a local file.
     * Uses try-with-resources to ensure safe and automatic closing of I/O streams.
     * * @param events The list of events to be persisted.
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
     * Reconstructs object-oriented entities from the text file.
     * Fault-tolerant: a single corrupted line will be ignored, 
     * preventing the entire loading process from failing.
     * * @return A list of reconstructed Event objects.
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
     * Assigns a shared series identifier to recurring events loaded from legacy
     * file formats (without an explicit UUID in the CSV suffix).
     * * @param events The list of loaded events to process for migration.
     * @return true if any event received a newly generated series identifier.
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
     * Parses a single CSV line into an Event or RecurringEvent object.
     * * @param line The raw CSV string from the file.
     * @return A reconstructed Event object, or null if parsing fails.
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