package planner;

import planner.controller.EventController;
import planner.controller.ReminderThread;
import planner.view.MainFrame;
import javax.swing.SwingUtilities;

/**
 * Main is the entry point for the Event Planner application.
 *
 * <p><strong>Initialization Process:</strong></p>
 * <ul>
 *   <li>Initializes the {@link EventController} to manage all event operations and data</li>
 *   <li>Starts the {@link ReminderThread} daemon thread for monitoring event reminders</li>
 *   <li>Creates and displays the primary application window ({@link MainFrame}) on the
 *       Event Dispatch Thread (EDT)</li>
 * </ul>
 *
 * <p><strong>Threading Model:</strong> Uses {@link SwingUtilities#invokeLater(Runnable)} to ensure
 * that all Swing UI components are created and displayed on the correct thread (EDT - Event
 * Dispatch Thread). This is a best practice in Swing applications to prevent thread safety issues.</p>
 *
 * <p><strong>Reminder System:</strong> The {@link ReminderThread} runs independently in the
 * background to monitor events and trigger reminders at the appropriate times.</p>
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 2.0
 */
public class Main {

    /**
     * Main method - the entry point of the Event Planner application.
     *
     * <p>This method initializes the application by:</p>
     * <ol>
     *   <li>Creating an instance of {@link EventController} to manage all event operations</li>
     *   <li>Starting a background {@link ReminderThread} thread that monitors and triggers event
     *       reminders</li>
     *   <li>Creating and displaying the main application window ({@link MainFrame}) on the Event
     *       Dispatch Thread (EDT)</li>
     * </ol>
     *
     * <p><strong>Thread Safety:</strong> All Swing UI operations are delegated to the Event
     * Dispatch Thread (EDT) using {@link SwingUtilities#invokeLater(Runnable)} to ensure thread
     * safety and proper UI rendering.</p>
     *
     * @param args Command-line arguments (currently unused)
     */
    public static void main(String[] args) {
        // SwingUtilities.invokeLater ensures that the UI is created on the correct Swing thread
        // (EDT - Event Dispatch Thread). This is a best practice for thread-safe UI creation.
        SwingUtilities.invokeLater(() -> {
            // Create the event controller for managing all event operations
            EventController controller = new EventController();

            // Start the reminder thread in parallel to monitor and trigger event reminders
            new ReminderThread(controller).start();

            // Create and display the main application window
            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }
}
