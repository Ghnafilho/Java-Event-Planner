package planner.controller;

import planner.model.Event;
import javax.swing.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ReminderThread is a background daemon thread that continuously monitors scheduled events
 * and displays reminder notifications at the appropriate time before each event.
 *
 * This thread runs independently and periodically checks if any events need reminders.
 * When a reminder is due, it displays a popup dialog to alert the user. The thread
 * uses a set to track notified events to ensure each event triggers exactly one reminder.
 *
 * Key features:
 * - Runs as a daemon thread (automatically stops when the application exits)
 * - Checks for reminders every 30 seconds
 * - Thread-safe reminder notifications using SwingUtilities.invokeLater()
 * - Prevents duplicate notifications for the same event
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho 
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 1.0
 */
public class ReminderThread extends Thread {

    // Check interval: 30 seconds (30,000 milliseconds)
    // Controls how frequently the thread checks for due reminders
    private static final int CHECK_INTERVAL_MS = 30_000;

    // Reference to the event controller for accessing all scheduled events
    private final EventController controller;

    // Tracks which events have already been notified to prevent duplicate reminders
    // Uses event title + date/time as a unique key
    private final Set<String> alreadyNotified = new HashSet<>();

    // Flag to control the thread's execution loop
    // Using volatile keyword ensures all threads see the most current value
    private volatile boolean isRunning = true;

    /**
     * Constructs a ReminderThread with a reference to the EventController.
     *
     * The thread is automatically configured as a daemon thread so it will
     * automatically terminate when the main application exits.
     *
     * @param controller The EventController instance used to retrieve events
     */
    public ReminderThread(EventController controller) {
        this.controller = controller;
        setDaemon(true);      // Thread automatically closes when program exits
        setName("ReminderThread");
    }

    /**
     * Main execution method of the thread.
     *
     * This method runs continuously in a loop, checking for due reminders at regular
     * intervals (every 30 seconds). The loop terminates when the isRunning flag is set
     * to false by calling the stop() method.
     *
     * The thread gracefully handles interruptions and will exit the loop if interrupted.
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
     * Checks all events and triggers reminders for those that are due.
     *
     * This method:
     * 1. Gets the current date and time
     * 2. Iterates through all events in the controller
     * 3. Calculates when each event's reminder should trigger
     * 4. Checks if the current time is within the reminder window
     * 5. Displays a popup notification if reminder is due and hasn't been shown yet
     *
     * The reminder window is defined as: from (event time - reminder minutes) to
     * (event time + 1 minute) to account for timing variations.
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
     * The popup shows the event's title, scheduled date/time, and location
     * in a formatted HTML message. The user must acknowledge the reminder
     * by clicking the OK button to close the notification.
     *
     * @param event The Event object that the reminder is for
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
     * This method sets the isRunning flag to false, which causes the main loop
     * in the run() method to terminate. It also interrupts the thread to wake it
     * from any sleep() call if currently sleeping.
     *
     * After calling this method, the thread will exit cleanly and allow the
     * application to shut down.
     */
    public void stop_running() {
        // Signal the thread to stop running
        isRunning = false;
        // Interrupt any current sleep() call to exit immediately
        interrupt();
    }
}
