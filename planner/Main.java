package planner;


import planner.controller.EventController;
import planner.controller.ReminderThread;
import planner.view.MainFrame;
import javax.swing.SwingUtilities;


public class Main {
    public static void main(String[] args) {


        // SwingUtilities.invokeLater ensures that the UI is created
        // on the correct Swing thread (EDT - Event Dispatch Thread).
        SwingUtilities.invokeLater(() -> {
            EventController controller = new EventController();


            // Starts the reminder thread in parallel
            new ReminderThread(controller).start();


            // Creates and displays the main window
            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }
}
