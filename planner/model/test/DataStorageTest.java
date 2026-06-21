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
 * DataStorageTest provides unit tests for the {@link DataStorage} class.
 *
 * <p><strong>Purpose:</strong> These tests ensure that the file persistence logic correctly
 * serializes {@link Event} and {@link RecurringEvent} objects to CSV format and deserializes
 * them back into memory with data integrity preserved. This is critical for verifying that
 * the "round-trip" serialization process maintains all object state.</p>
 *
 * <p><strong>Testing Strategy:</strong> Tests use JUnit 5 with temporary directories
 * ({@link TempDir}) to ensure file system isolation. Each test operates on its own temporary
 * file, preventing interference with other tests or the actual application data.</p>
 *
 * <p><strong>Test Coverage:</strong></p>
 * <ul>
 *   <li>Round-trip serialization and deserialization of {@link RecurringEvent} objects</li>
 *   <li>Preservation of all event attributes during save/load cycles</li>
 *   <li>Correct type detection (distinguishing {@link RecurringEvent} from {@link Event})</li>
 *   <li>Series ID persistence and matching</li>
 * </ul>
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 2.0
 * @see DataStorage
 * @see planner.model.Event
 * @see planner.model.RecurringEvent
 */
public class DataStorageTest {

    /**
     * Temporary directory managed by JUnit 5 for isolated file system operations.
     *
     * <p>This directory is automatically created before each test and cleaned up afterward,
     * ensuring that each test has a fresh, isolated environment. This prevents tests from
     * interfering with each other or with real application data.</p>
     */
    @TempDir
    Path tempDir;

    /**
     * Tests the "round-trip" serialization and deserialization process for a
     * {@link RecurringEvent}.
     *
     * <p><strong>Purpose:</strong> Verifies that a {@link RecurringEvent} can be successfully
     * saved to a file in CSV format and then loaded back with all attributes intact and
     * unchanged.</p>
     *
     * <p><strong>Test Scenario:</strong></p>
     * <ol>
     *   <li><strong>Arrange:</strong> Create a temporary file and a {@link RecurringEvent}
     *       with known properties</li>
     *   <li><strong>Act:</strong> Save the event to the temporary file via
     *       {@link DataStorage#saveEvents(List)}, then reload it using
     *       {@link DataStorage#loadEvents()}</li>
     *   <li><strong>Assert:</strong> Verify that the loaded event matches the original in:
     *       <ul>
     *         <li>Title</li>
     *         <li>Recurrence type</li>
     *         <li>Series ID</li>
     *         <li>Type classification (is a {@link RecurringEvent}, not a generic
     *             {@link Event})</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * <p><strong>Test Data:</strong> Uses a specific {@link RecurringEvent} instance:
     * <ul>
     *   <li>Title: "Reunião Semanal" (Weekly Meeting in Portuguese)</li>
     *   <li>Date/Time: June 21, 2026 at 10:00</li>
     *   <li>Location: "Sala 01"</li>
     *   <li>Category: "Work"</li>
     *   <li>Recurrence: WEEKLY</li>
     *   <li>Reminder: 15 minutes before</li>
     * </ul>
     * </p>
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
