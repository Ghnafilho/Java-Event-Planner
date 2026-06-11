package planner.model;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: Component responsible for data persistence on disk (File I/O).
 *
 * Applied Architecture Principle - SEPARATION OF CONCERNS:
 * Isolates file reading and writing logic from domain logic, ensuring that
 * model entities do not depend on specific file system implementations.
 */
public class DataStorage {
    
    private static final String FILE_PATH = "events_data.txt";

/**
 * Serializes the in-memory data structure to a local file.
 * Uses try-with-resources to ensure the safe and automatic
 * closing of I/O streams.
 */
    public void saveEvents(List<Event> events) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Event event : events) {
                // Runtime polymorphism, i.e., the appropriate toCSV() method
                // will be invoked depending on the type of the instance (Event or RecurringEvent)
                writer.write(event.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("I/O failure while saving events: " + e.getMessage());
        }
    }

/**
 * Reconstructs object-oriented entities from the text file.
 *
 * Exception Handling:
 * Uses an individual try-catch block for each line read (inside the while loop).
 * This ensures fault tolerance. A single corrupted line will be ignored,
 * preventing the entire loading process from failing.
 */
    public List<Event> loadEvents() {
        List<Event> events = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return events; // Safe initialization if the system runs for the first time
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                try {
                    // The limit -1 prevents missing data at the end of the line from affecting the parsing
                    String[] parts = line.split(";", -1);
                    
                    if (parts.length < 7) continue; 
                    
                    String title = parts[0];
                    LocalDateTime dt = LocalDateTime.parse(parts[1], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    String location = parts[2];
                    String description = parts[3];
                    String category = parts[4];
                    int reminder = Integer.parseInt(parts[5]);
                    
                    boolean isRecurring = false;
                    RecurringEvent.RecurrenceType recType = RecurringEvent.RecurrenceType.NONE;
                    
                    // Identifies if the persisted line belongs to the RecurringEvent subclass
                    if (parts.length >= 8 && parts[7].startsWith("[REC:")) {
                        isRecurring = true;
                        String typeStr = parts[7].substring(5, parts[7].length() - 1);
                        recType = RecurringEvent.RecurrenceType.valueOf(typeStr);
                    }
                    
                    // Instantiates the correct entity
                    Event event;
                    if (isRecurring) {
                        event = new RecurringEvent(title, dt, location, description, category, reminder, recType);
                    } else {
                        event = new Event(title, dt, location, description, category, reminder);
                    }
                    
                    // Reconstruction of the structural composition (Attendees)
                    String attendeesStr = parts[6];
                    if (!attendeesStr.trim().isEmpty()) {
                        String[] attList = attendeesStr.split(",");
                        for (String att : attList) {
                            String[] attData = att.split("\\|");
                            if (attData.length == 2) {
                                event.addAttendee(new Attendee(attData[0], attData[1]));
                            }
                        }
                    }
                    
                    events.add(event);
                    
                } catch (Exception e) {
                    System.err.println("Warning: Failure to perform parsing on the line. Partial restoration applied.");
                }
            }
        } catch (IOException e) {
            System.err.println("Critical error accessing local database: " + e.getMessage());
        }

        return events;
    }
}