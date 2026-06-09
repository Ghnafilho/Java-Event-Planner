package planner.exception;

/**
 * InvalidEventException is a custom checked exception thrown when an event
 * operation violates validation rules or business logic constraints.
 *
 * This exception is used to indicate that an event creation or modification
 * failed due to invalid input data or system state. Common scenarios include:
 * - Empty or null event title
 * - Invalid or null date/time
 * - Empty or invalid category
 * - Invalid email format for attendees
 * - Invalid recurrence type
 *
 * Since this extends Exception (not RuntimeException), it is a checked exception
 * and must be caught or declared in method signatures using the 'throws' clause.
 *
 * Example usage:
 * <pre>
 *     try {
 *         Event event = controller.createEvent(title, dateTime, location, description, category, reminder);
 *     } catch (InvalidEventException e) {
 *         System.out.println("Failed to create event: " + e.getMessage());
 *     }
 * </pre>
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho 
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 1.0
 * @see planner.controller.EventController
 */
public class InvalidEventException extends Exception {

    /**
     * Constructs an InvalidEventException with a descriptive error message.
     *
     * The message should clearly describe what validation rule was violated
     * or what the specific problem was. This message will be displayed to the
     * user or logged for debugging purposes.
     *
     * @param message A descriptive error message explaining why the event is invalid
     *                (e.g., "Event title cannot be empty." or "Email address is invalid.")
     */
    public InvalidEventException(String message) {
        super(message);
    }
}
