package planner.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * * Descrição: Classe base do modelo de domínio que representa um evento no calendário
 * * Conceito OOP Aplicado - PREPARAÇÃO PARA HERANÇA:
 * Os atributos utilizam o modificador de acesso 'protected'. Isso encapsula os dados 
 * de classes externas, mas permite que subclasses (como RecurringEvent) tenham acesso 
 * direto aos atributos herdados.
 * * Conceito OOP Aplicado - COMPOSIÇÃO:
 * A classe Event tem uma lista de objetos Attendee, estabelecendo um relacionamento 
 * estrutural forte entre o evento e seus participantes.
 */
public class Event {
    
    protected String title;
    protected LocalDateTime dateTime;
    protected String location;
    protected String description;
    protected String category;
    protected int reminderMinutesBefore; 
    protected List<Attendee> attendees;

    /**
     * Construtor da classe base Event.
     * A lista de participantes é inicializada internamente para evitar NullPointerException.
     */
    public Event(String title, LocalDateTime dateTime, String location, String description, String category, int reminderMinutesBefore) {
        this.title = title;
        this.dateTime = dateTime;
        this.location = location;
        this.description = description;
        this.category = category;
        this.reminderMinutesBefore = reminderMinutesBefore;
        this.attendees = new ArrayList<>();
    }

    // Métodos de acesso (getters e setters)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getReminderMinutesBefore() { return reminderMinutesBefore; }
    public void setReminderMinutesBefore(int reminderMinutesBefore) { this.reminderMinutesBefore = reminderMinutesBefore; }

    public List<Attendee> getAttendees() { return attendees; }

    /**
     * Adiciona um participante à lista estrutural do evento.
     * @param attendee Objeto Attendee a ser adicionado
     */
    public void addAttendee(Attendee attendee) {
        if (attendee != null) {
            this.attendees.add(attendee);
        }
    }

    /**
     * Retorna a data e hora formatadas para exibição no padrão brasileiro
     */
    public String getFormattedDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * Converte o estado atual do objeto em uma String separada por delimitadores.
     * Aplica sanitização nos campos de texto para evitar quebras de delimitador (;)
     * durante o processo de File I/O.
     * * @return String formatada para armazenamento.
     */
    public String toCSV() {
        String safeTitle = title != null ? title.replace(";", ",").replace("|", "") : "";
        String safeLocation = location != null ? location.replace(";", ",").replace("|", "") : "";
        String safeDesc = description != null ? description.replace(";", ",").replace("|", "").replace("\n", " ") : "";
        String safeCat = category != null ? category.replace(";", ",").replace("|", "") : "";

        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append(safeTitle).append(";")
                  .append(dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append(";")
                  .append(safeLocation).append(";")
                  .append(safeDesc).append(";")
                  .append(safeCat).append(";")
                  .append(reminderMinutesBefore).append(";");
        
        // Concatena as representações em texto dos objetos da lista de Composição
        for (int i = 0; i < attendees.size(); i++) {
            csvBuilder.append(attendees.get(i).toCSV());
            if (i < attendees.size() - 1) {
                csvBuilder.append(",");
            }
        }
        
        return csvBuilder.toString();
    }

    @Override
    public String toString() {
        return title + " (" + getFormattedDateTime() + ") - " + category;
    }
}