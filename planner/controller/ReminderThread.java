package planner.controller;

import planner.model.Event;
import javax.swing.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ReminderThread is a background daemon thread that continuously monitors scheduled events
 * and displays reminder notifications at the appropriate time before each event.
 *
 * <p><strong>Threading Model:</strong> This thread runs independently as a daemon thread
 * (automatically stops when the application exits) and periodically checks if any events need
 * reminders. When a reminder is due, it displays a popup dialog to alert the user.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Runs as a daemon thread (automatically terminates when the main application exits)</li>
 *   <li>Checks for due reminders every 30 seconds ({@link #CHECK_INTERVAL_MS})</li>
 *   <li>Thread-safe reminder notifications using {@link SwingUtilities#invokeLater(Runnable)}</li>
 *   <li>Prevents duplicate notifications for the same event using a tracking set</li>
 *   <li>Graceful shutdown support via {@link #stop_running()} method</li>
 *   <li>Handles thread interruptions and sleep exceptions appropriately</li>
 * </ul>
 *
 * <p><strong>Reminder Timing:</strong> A reminder is displayed when the current time falls within
 * the reminder window, defined as from (event time - reminder minutes) to (event time + 1 minute)
 * to account for timing variations and race conditions.</p>
 *
 * <p><strong>Thread Safety:</strong> Uses {@link SwingUtilities#invokeLater(Runnable)} to ensure
 * all UI operations occur on the Swing Event Dispatch Thread (EDT), which is required for thread-safe
 * Swing component operations.</p>
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 2.0
 * @see EventController
 * @see planner.model.Event
 */
public class ReminderThread extends Thread {

    /**
     * The interval in milliseconds between consecutive reminder checks.
     * Set to 30 seconds (30,000 ms) to provide regular checking without excessive CPU usage.
     */
    private static final int CHECK_INTERVAL_MS = 30_000;

    /** Reference to the {@link EventController} for accessing all scheduled events */
    private final EventController controller;

    /**
     * Tracks which events have already been notified to prevent duplicate reminders.
     * Uses a unique key combining event title and date/time to identify events.
     */
    private final Set<String> alreadyNotified = new HashSet<>();

    /**
     * Flag to control the thread's execution loop.
     * Marked as {@code volatile} to ensure all threads see the most current value
     * when checking this flag.
     */
    private volatile boolean isRunning = true;

    /**
     * Constructs a ReminderThread with a reference to the EventController.
     *
     * <p>Initializes the thread with the provided controller instance and automatically
     * configures the thread as a daemon thread so it will automatically terminate when
     * the main application exits.</p>
     *
     * @param controller The {@link EventController} instance used to retrieve and monitor
     *                   scheduled events
     */
    public ReminderThread(EventController controller) {
        this.controller = controller;
        setDaemon(true);      // Thread automatically closes when program exits
        setName("ReminderThread");
    }

    /**
     * Main execution method of the reminder thread.
     *
     * <p>This method runs in a continuous loop, checking for due reminders at regular intervals
     * (every {@link #CHECK_INTERVAL_MS} milliseconds). The loop terminates when the
     * {@link #isRunning} flag is set to {@code false} by calling the {@link #stop_running()}
     * method.</p>
     *
     * <p><strong>Execution Flow:</strong></p>
     * <ol>
     *   <li>Call {@link #checkReminders()} to check for due reminders</li>
     *   <li>Sleep for 30 seconds</li>
     *   <li>Repeat until {@link #isRunning} becomes {@code false}</li>
     * </ol>
     *
     * <p>The thread gracefully handles {@link InterruptedException} and will exit the loop if
     * interrupted, allowing for clean shutdown.</p>
     */
    @Override
    public void run() {
        while (isRunning) {
            // Check which events need reminder notifications
            checkReminders();
            try {
                // Sleep for 30 seconds before checking again
                Thread.sleep(CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                // If interrupted, restore interrupt status and exit the loop
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Checks all scheduled events and triggers reminders for those that are due.
     *
     * <p><strong>Execution Process:</strong></p>
     * <ol>
     *   <li>Gets the current date and time via {@link LocalDateTime#now()}</li>
     *   <li>Iterates through all events in the controller via
     *       {@link EventController#getAllEvents()}</li>
     *   <li>For each event, calculates when its reminder should trigger by subtracting
     *       {@link Event#getReminderMinutesBefore()} from the event time</li>
     *   <li>Checks if the current time falls within the reminder window</li>
     *   <li>If the reminder is due and hasn't been shown yet, displays a popup notification
     *       via {@link #displayReminderPopup(Event)}</li>
     * </ol>
     *
     * <p><strong>Reminder Window:</strong> The reminder window is defined as from
     * (event time - reminder minutes) to (event time + 1 minute). The 1-minute tolerance
     * accounts for timing variations and race conditions.</p>
     *
     * <p><strong>Duplicate Prevention:</strong> Uses the {@link #alreadyNotified} set to track
     * which events have already triggered their reminders, ensuring each event produces exactly
     * one notification.</p>
     */
    private void checkReminders() {
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();

        // Iterate through all scheduled events
        for (Event event : controller.getAllEvents()) {
            // Calculate the exact moment when the reminder should appear
            // (reminder appears X minutes before the event starts)
            LocalDateTime reminderTime = event.getDateTime()
                    .minusMinutes(event.getReminderMinutesBefore());

            // Create a unique key for this event to track if it's already been notified
            // Combines event title and date/time to ensure uniqueness
            String eventKey = event.getTitle() + "@" + event.getDateTime();

            // Check if it's time to show the reminder:
            // - Current time must be at or after the reminder time
            // - Current time must be before the event starts (plus 1 minute tolerance)
            boolean shouldNotify = !now.isBefore(reminderTime)
                    && now.isBefore(event.getDateTime().plusMinutes(1));

            // If reminder is due and we haven't already notified for this event
            if (shouldNotify && !alreadyNotified.contains(eventKey)) {
                // Mark this event as notified to prevent duplicate reminders
                alreadyNotified.add(eventKey);
                // Store reference to event for use in lambda expression
                final Event eventToNotify = event;
                // Use SwingUtilities.invokeLater to safely display UI popup on the Swing/AWT thread
                // (Required because Swing components must be accessed from the Event Dispatch Thread)
                SwingUtilities.invokeLater(() -> displayReminderPopup(eventToNotify));
            }
        }
    }

    /**
     * Displays a popup notification window for an event reminder.
     *
     * <p>Creates and displays an {@link JOptionPane#INFORMATION_MESSAGE} dialog showing the
     * event's details in a formatted HTML message. The popup includes the event title, scheduled
     * date/time, and location. The user must acknowledge the reminder by clicking the OK button
     * to close the notification.</p>
     *
     * <p><strong>Message Format:</strong> The reminder popup displays:</p>
     * <ul>
     *   <li>A clock icon (⏰) with "Reminder!" heading</li>
     *   <li>Event title</li>
     *   <li>Formatted date and time ({@link Event#getFormattedDateTime()})</li>
     *   <li>Location</li>
     * </ul>
     *
     * @param event The {@link Event} object that the reminder is for. Must not be {@code null}
     */
    private void displayReminderPopup(Event event) {
        // Create a formatted HTML message with event details
        String message = "<html>"
                + "<b>⏰ Reminder!</b><br><br>"
                + "<b>Event:</b> " + event.getTitle() + "<br>"
                + "<b>Time:</b> " + event.getFormattedDateTime() + "<br>"
                + "<b>Location:</b> " + event.getLocation() + "<br>"
                + "</html>";

        // Display the reminder popup dialog
        // Parameters: parent component (null = center on screen), message, title, message type
        JOptionPane.showMessageDialog(null, message,
                "Reminder", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Stops the reminder thread gracefully.
     *
     * <p>Sets the {@link #isRunning} flag to {@code false}, which causes the main loop in the
     * {@link #run()} method to terminate. Also calls {@link Thread#interrupt()} to wake the
     * thread from any {@link Thread#sleep(long)} call if currently sleeping, allowing for
     * immediate shutdown.</p>
     *
     * <p>After calling this method, the thread will exit cleanly and the application can
     * shut down without hanging threads.</p>
     */
    public void stop_running() {
        // Signal the thread to stop running
        isRunning = false;
        // Interrupt any current sleep() call to exit immediately
        interrupt();
    }
}
