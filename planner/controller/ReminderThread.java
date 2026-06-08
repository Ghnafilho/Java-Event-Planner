package planner.controller;
import planner.model.Event;
import javax.swing.*;
import java.time.LocalDateTime;
import java.util.*;
    public class ReminderThread extends Thread {


        private static final int INTERVALO_MS = 30_000; // 30 segundos


        private final EventController controller;
        private final Set<String> jaNotificados = new HashSet<>();
        private volatile boolean rodando = true;


        public ReminderThread(EventController controller) {
            this.controller = controller;
            setDaemon(true);      // fecha junto com o programa
            setName("ReminderThread");
        }


        @Override
        public void run() {
            while (rodando) {
                verificarLembretes();
                try {
                    Thread.sleep(INTERVALO_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }


        private void verificarLembretes() {
            LocalDateTime agora = LocalDateTime.now();


            for (Event event : controller.getAllEvents()) {
                // Calcula o momento em que o lembrete deve aparecer
                LocalDateTime momentoLembrete = event.getDateTime()
                        .minusMinutes(event.getReminderMinutesBefore());


                // Chave única para evitar notificar o mesmo evento duas vezes
                String chave = event.getTitle() + "@" + event.getDateTime();


                // É hora de notificar?
                boolean deveNotificar = !agora.isBefore(momentoLembrete)
                        && agora.isBefore(event.getDateTime().plusMinutes(1));


                if (deveNotificar && !jaNotificados.contains(chave)) {
                    jaNotificados.add(chave);
                    final Event e = event;
                    // SwingUtilities.invokeLater: executa na thread do Swing (obrigatório para UI)
                    SwingUtilities.invokeLater(() -> mostrarPopup(e));
                }
            }
        }


        private void mostrarPopup(Event event) {
            String mensagem = "<html>"
                    + "<b>⏰ Lembrete!</b><br><br>"
                    + "<b>Evento:</b> " + event.getTitle() + "<br>"
                    + "<b>Horário:</b> " + event.getFormattedDateTime() + "<br>"
                    + "<b>Local:</b> " + event.getLocation() + "<br>"
                    + "</html>";


            JOptionPane.showMessageDialog(null, mensagem,
                    "Lembrete", JOptionPane.INFORMATION_MESSAGE);
        }


        public void parar() {
            rodando = false;
            interrupt();
        }
    }
