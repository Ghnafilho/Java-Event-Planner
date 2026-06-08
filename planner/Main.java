package planner;


import planner.controller.EventController;
import planner.controller.ReminderThread;
import planner.view.MainFrame;
import javax.swing.SwingUtilities;


public class Main {
    public static void main(String[] args) {


        // SwingUtilities.invokeLater garante que a UI seja criada
        // na thread correta do Swing (EDT — Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            EventController controller = new EventController();


            // Inicia a thread de lembretes em paralelo
            new ReminderThread(controller).start();


            // Cria e exibe a janela principal
            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }
}
