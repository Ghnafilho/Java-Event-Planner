package planner.exception;

/**
 * InvalidEventException is a custom checked exception thrown when an event operation violates
 * validation rules or business logic constraints.
 *
 * <p><strong>Purpose:</strong> This exception is used to indicate that an event creation or
 * modification failed due to invalid input data or system state. By using a custom exception,
 * the application can distinguish between validation errors specific to events and other types
 * of exceptions.</p>
 *
 * <p><strong>Common Scenarios:</strong></p>
 * <ul>
 *   <li>Empty or null event title</li>
 *   <li>Invalid or null date/time value</li>
 *   <li>Empty or invalid event category</li>
 *   <li>Invalid email format for attendees</li>
 *   <li>Invalid recurrence type or pattern</li>
 *   <li>Attempt to modify a deleted or non-existent event</li>
 *   <li>Conflicting or contradictory event parameters</li>
 * </ul>
 *
 * <p><strong>Exception Type:</strong> Since this class extends {@link Exception} (not
 * {@link RuntimeException}), it is a <strong>checked exception</strong>. This means it must be
 * caught or declared in method signatures using the 'throws' clause. Checked exceptions promote
 * explicit error handling and make the API contract clearer to callers.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * try {
 *     Event event = controller.createEvent(title, dateTime, location, description,
 *                                          category, reminder);
 * } catch (InvalidEventException e) {
 *     System.out.println("Failed to create event: " + e.getMessage());
 *     // Display error to user or take recovery action
 * }
 * </pre>
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 2.0
 * @see planner.controller.EventController
 * @see planner.model.Event
 * @see planner.model.RecurringEvent
 */
public class InvalidEventException extends Exception {

    /**
     * Constructs an InvalidEventException with a descriptive error message.
     *
     * <p>The message should clearly describe what validation rule was violated or what the
     * specific problem was. This message will be displayed to the user in error dialogs or
     * logged for debugging and troubleshooting purposes.</p>
     *
     * <p>The message is passed to the superclass {@link Exception} and can be retrieved later
     * using {@link #getMessage()}.</p>
     *
     * @param message A descriptive {@link String} error message explaining why the event is
     *                invalid or what validation constraint was violated. Examples:
     *                <ul>
     *                  <li>"Event title cannot be empty."</li>
     *                  <li>"Date and time cannot be null."</li>
     *                  <li>"Category must be one of: Meeting, Birthday, Appointment, Reminder,
     *                      Other."</li>
     *                  <li>"Invalid email address format: user@example."</li>
     *                  <li>"Reminder time cannot be negative."</li>
     *                </ul>
     */
    public InvalidEventException(String message) {
        super(message);
    }
}
