package planner.model;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * * Descrição: Componente responsável pela persistência de dados em disco (File I/O).
 * * Arquitetura Aplicada - SEPARAÇÃO DE CONCEITOS:
 * Isola a lógica de leitura e escrita de arquivos das lógicas de domínio, garantindo
 * que as entidades do modelo não depnedam de implementações especificas do sistema de arquivos.
 */
public class DataStorage {
    
    private static final String FILE_PATH = "events_data.txt";

    /**
     * Serializa a estrutura de dados em memória para um arquivo local.
     * Utiliza try-with-resources para garantir o fechamento seguro das streams I/O
     */
    public void saveEvents(List<Event> events) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Event event : events) {
                // Polimorfismo em tempo de execução, ou seja, o método toCSV() adequado
                // será evocado dependendo do tipo da instância (Event ou RecurringEvent)
                writer.write(event.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Falha de I/O ao salvar eventos: " + e.getMessage());
        }
    }

    /**
     * Reconstrói as entidades orientadas a objetos a partir do arquivo texto.
     * * Tratamento de exceções: 
     * Engloba um bloco try-catch individual por linha lida (dentro do loop while).
     * Isso assegura tolerância a falhas. Uma única linha corrompida será ignorada,
     * impedindo a quebra total do processo de carregamento.
     */
    public List<Event> loadEvents() {
        List<Event> events = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return events; // Inicialização segura se o sistema rodar pela primeira vez
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                try {
                    // O limite -1 impede que dados omitidos ao final da linha afetem o parsing
                    String[] parts = line.split(";", -1);
                    
                    if (parts.length < 7) continue; 
                    
                    String title = parts[0];
                    LocalDateTime dt = LocalDateTime.parse(parts[1], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    String location = parts[2];
                    String description = parts[3];
                    String category = parts[4];
                    int reminder = Integer.parseInt(parts[5]);
                    
                    boolean isRecurring = false;
                    RecurringEvent.RecurrenceType recType = RecurringEvent.RecurrenceType.NONE;
                    
                    // Identifica se a linha persistida pertence a subclasse RecurringEvent
                    if (parts.length >= 8 && parts[7].startsWith("[REC:")) {
                        isRecurring = true;
                        String typeStr = parts[7].substring(5, parts[7].length() - 1);
                        recType = RecurringEvent.RecurrenceType.valueOf(typeStr);
                    }
                    
                    // Instancia a entidade correta
                    Event event;
                    if (isRecurring) {
                        event = new RecurringEvent(title, dt, location, description, category, reminder, recType);
                    } else {
                        event = new Event(title, dt, location, description, category, reminder);
                    }
                    
                    // Reconstrução da composição estrutural (Attendees)
                    String attendeesStr = parts[6];
                    if (!attendeesStr.trim().isEmpty()) {
                        String[] attList = attendeesStr.split(",");
                        for (String att : attList) {
                            String[] attData = att.split("\\|");
                            if (attData.length == 2) {
                                event.addAttendee(new Attendee(attData[0], attData[1]));
                            }
                        }
                    }
                    
                    events.add(event);
                    
                } catch (Exception e) {
                    System.err.println("Aviso: Falha ao realizar parsing na linha. Restauração parcial aplicada.");
                }
            }
        } catch (IOException e) {
            System.err.println("Erro crítico ao acessar o banco de dados local: " + e.getMessage());
        }

        return events;
    }
}