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
 * Unit tests for the {@link planner.controller.EventController} class.
 *
 * <p>These tests use dependency injection to provide a temporary {@link DataStorage}
 * instance that writes to an isolated temporary directory. Using JUnit's {@link TempDir}
 * ensures filesystem side-effects are contained and cleaned up between runs.</p>
 *
 * @author Pedro Rocha
 * @since 1.0
 */
public class EventControllerTest {

    /**
     * Temporary directory for isolated file operations during testing.
     *
     * <p>The {@link TempDir} field is initialized by JUnit before each test method.
     * Tests should construct any test-specific file paths off of this directory so
     * that no permanent files are created on the developer's filesystem.</p>
     */
    @TempDir
    Path tempDir;

    /**
     * Verifies that attempting to create an event with an empty title
     * causes the {@link planner.exception.InvalidEventException} to be thrown.
     *
     * <p>The test constructs an {@link EventController} backed by a temporary
     * {@link DataStorage} file and asserts that creating an event with an
     * empty title is rejected.</p>
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
     * Verifies that creating an event with valid input data returns a non-null {@link Event}
     * and that the created event is added to the controller's internal list.
     *
     * @throws InvalidEventException if the provided test data is considered invalid by the controller.
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
     * Verifies that a recurring event is instantiated as a {@link RecurringEvent},
     * that the recurrence type is set correctly, and that a series identifier is generated.
     *
     * <p>This test constructs a weekly recurring event with a defined count and checks
     * the resulting object's runtime type and recurrence metadata.</p>
     *
     * @throws InvalidEventException if the recurrence configuration is invalid.
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