package planner.model.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import planner.model.*;

/**
 * Unit tests for DataStorage class.
 * Ensures that file persistence logic correctly serializes and deserializes objects.
 */
public class DataStorageTest {

    /**
     * Temporary directory managed by JUnit 5 for file system isolation.
     */
    @TempDir
    Path tempDir;

    /**
     * Tests the "round-trip" serialization and deserialization process for a RecurringEvent.
     * This ensures that data saved to a file is identical to the data retrieved from it.
     */
    @Test
    public void testRoundTrip_RecurringEvent() {
        // 1. Arrange: Setup the test environment and create an object
        Path tempFilePath = tempDir.resolve("test_events.txt");
        DataStorage storage = new DataStorage(tempFilePath.toString());
        
        LocalDateTime now = LocalDateTime.of(2026, 6, 21, 10, 0);
        RecurringEvent original = new RecurringEvent("Reunião Semanal", now, "Sala 01", 
                                                    "Desc", "Work", 15, 
                                                    RecurringEvent.RecurrenceType.WEEKLY);

        // 2. Act: Save the object to the file and load it back
        List<Event> listaOriginal = new ArrayList<>();
        listaOriginal.add(original);
        storage.saveEvents(listaOriginal);
        
        List<Event> listaCarregada = storage.loadEvents();

        // 3. Assert: Verify the loaded object matches the original
        assertFalse(listaCarregada.isEmpty(), "Loaded list should not be empty");
        assertTrue(listaCarregada.get(0) instanceof RecurringEvent, "Loaded object should be a RecurringEvent");
        
        RecurringEvent carregado = (RecurringEvent) listaCarregada.get(0);
        
        assertEquals(original.getTitle(), carregado.getTitle(), "Titles should match");
        assertEquals(original.getRecurrenceType(), carregado.getRecurrenceType(), "Recurrence types should match");
        assertEquals(original.getSeriesId(), carregado.getSeriesId(), "Series IDs should match");
    }
}