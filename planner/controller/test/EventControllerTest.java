package planner.controller.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.LocalDateTime;
import planner.exception.InvalidEventException;
import planner.model.Event;
import planner.model.RecurringEvent;
import planner.model.DataStorage;
import planner.controller.EventController;

/**
 * Unit tests for the EventController class.
 * Uses dependency injection to ensure file operations are performed 
 * in a temporary, isolated environment.
 */
public class EventControllerTest {

    /**
     * Temporary directory for isolated file operations during testing.
     */
    @TempDir
    Path tempDir;

    /**
     * Verifies that attempting to create an event with an empty title throws an InvalidEventException.
     */
    @Test
    public void testCreateEvent_InvalidTitle_ShouldThrowException() {
        // Arrange: Use a temporary storage
        DataStorage tempStorage = new DataStorage(tempDir.resolve("invalid_title.txt").toString());
        EventController controller = new EventController(tempStorage);

        // Act & Assert
        assertThrows(InvalidEventException.class, () -> {
            controller.createEvent("", LocalDateTime.now(), "Local", "Desc", "Cat", 10);
        }, "Should throw InvalidEventException when title is empty");
    }

    /**
     * Verifies that creating an event with valid data correctly adds it to the internal list.
     */
    @Test
    public void testCreateEvent_ValidData_ShouldAddToList() throws InvalidEventException {
        // Arrange
        DataStorage tempStorage = new DataStorage(tempDir.resolve("valid_event.txt").toString());
        EventController controller = new EventController(tempStorage);
        String title = "Teste Unitário";
        
        // Act
        Event e = controller.createEvent(title, LocalDateTime.now(), "Local", "Desc", "Categoria", 10);
        
        // Assert
        assertNotNull(e, "Event object should not be null");
        assertEquals(title, e.getTitle(), "Event title should match the provided title");
        assertTrue(controller.getAllEvents().size() > 0, "Event list should contain the newly created event");
    }

    /**
     * Verifies that a recurring event is correctly instantiated, identified as a RecurringEvent, 
     * and assigned a unique series identifier.
     */
    @Test
    public void testCreateRecurringEvent_Success() throws InvalidEventException {
        // Arrange
        DataStorage tempStorage = new DataStorage(tempDir.resolve("recurring_event.txt").toString());
        EventController controller = new EventController(tempStorage);
        
        // Act
        Event e = controller.createRecurringEvent("Evento Semanal", LocalDateTime.now(), 
                                                 "Local", "Desc", "Cat", 10, 
                                                 RecurringEvent.RecurrenceType.WEEKLY, 5);

        // Assert
        assertTrue(e instanceof RecurringEvent, "Created object should be an instance of RecurringEvent");
        RecurringEvent re = (RecurringEvent) e;
        assertEquals(RecurringEvent.RecurrenceType.WEEKLY, re.getRecurrenceType(), "Recurrence type should match");
        assertNotNull(re.getSeriesId(), "Series ID should be automatically generated");
    }
}