package planner.controller;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import planner.exception.InvalidEventException;
import planner.model.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
public class EventController{
        // A lista que fica na memória enquanto o programa roda
        private final List<Event> events;


        // Responsável por salvar/carregar do arquivo
        private final DataStorage storage;


        // Construtor: ao criar o controller, já carrega os dados do arquivo
        public EventController() {
            this.storage = new DataStorage();
            this.events  = new ArrayList<>(storage.loadEvents());
        }
        public Event createEvent(String title, LocalDateTime dateTime, String location,
                            String description, String category, int reminderMinutes)
            throws InvalidEventException {


        // 1. Valida os campos
        validateEventFields(title, dateTime, category);


        // 2. Cria o objeto
        Event event = new Event(title, dateTime, location, description, category, reminderMinutes);


        // 3. Adiciona na lista em memória
        events.add(event);


        // 4. Salva no arquivo
        storage.saveEvents(events);


        // 5. Retorna o evento criado (a View pode usar)
        return event;
        }
        public RecurringEvent createRecurringEvent(String title, LocalDateTime dateTime,
                String location, String description, String category,
                int reminderMinutes, RecurringEvent.RecurrenceType recurrenceType, int repeat)
                throws InvalidEventException {

            validateEventFields(title, dateTime, category);

            if (recurrenceType == null || recurrenceType == RecurringEvent.RecurrenceType.NONE) {
                throw new InvalidEventException("Tipo de recorrência inválido.");
            }

            RecurringEvent event = new RecurringEvent(title, dateTime, location,
                    description, category, reminderMinutes, recurrenceType);
            events.add(event);

            // Gera as repetições futuras
            int repeticoes = switch (recurrenceType) {
                case DAILY   -> repeat -1;   
                case WEEKLY  -> repeat -1;   
                case MONTHLY -> repeat -1;   
                default      -> 0;
            };

            LocalDateTime proxData = dateTime;
            for (int i = 0; i < repeticoes; i++) {
                proxData = switch (recurrenceType) {
                    case DAILY   -> proxData.plusDays(1);
                    case WEEKLY  -> proxData.plusWeeks(1);
                    case MONTHLY -> proxData.plusMonths(1);
                    default      -> proxData;
                };
                events.add(new RecurringEvent(title, proxData, location,
                        description, category, reminderMinutes, recurrenceType));
            }

            storage.saveEvents(events);
            return event;
        }
    public void updateEvent(Event event, String title, LocalDateTime dateTime,
            String location, String description, String category, int reminderMinutes)
            throws InvalidEventException {


        validateEventFields(title, dateTime, category);


        // Atualiza os campos do objeto que já está na lista
        event.setTitle(title);
        event.setDateTime(dateTime);
        event.setLocation(location);
        event.setDescription(description);
        event.setCategory(category);
        event.setReminderMinutesBefore(reminderMinutes);


        storage.saveEvents(events); // salva as mudanças
    }
    
    public void updateFutureOccurrences(RecurringEvent event, String title,
            LocalDateTime dateTime, String location, String description,
            String category, int reminderMinutes) throws InvalidEventException {

        validateEventFields(title, dateTime, category);

        // Diferença de tempo entre a data nova e a antiga (para manter o offset nas repetições)
        long diferencaMinutos = java.time.Duration.between(event.getDateTime(), dateTime).toMinutes();

        events.stream()
            .filter(e -> e instanceof RecurringEvent
                    && e.getTitle().equals(event.getTitle())
                    && !e.getDateTime().isBefore(event.getDateTime()))
            .forEach(e -> {
                e.setTitle(title);
                e.setDateTime(e.getDateTime().plusMinutes(diferencaMinutos));
                e.setLocation(location);
                e.setDescription(description);
                e.setCategory(category);
                e.setReminderMinutesBefore(reminderMinutes);
            });

        storage.saveEvents(events);
    }
    public void deleteEvent(Event event) {
        events.remove(event);
        storage.saveEvents(events);
    }
    public void deleteFutureOccurrences(RecurringEvent event) {
        events.removeIf(e ->
            e instanceof RecurringEvent
            && e.getTitle().equals(event.getTitle())
            && !e.getDateTime().isBefore(event.getDateTime())
        );
        storage.saveEvents(events);
    }
    public List<Event> getEventsForDate(LocalDate date) {
        return events.stream()
                .filter(e -> e.getDateTime().toLocalDate().equals(date))
                .sorted((a, b) -> a.getDateTime().compareTo(b.getDateTime()))
                .collect(Collectors.toList());
    }
    public List<LocalDate> getDatesWithEventsInMonth(int year, int month) {
        return events.stream()
                .map(e -> e.getDateTime().toLocalDate())
                .filter(d -> d.getYear() == year && d.getMonthValue() == month)
                .distinct()
                .collect(Collectors.toList());
    }
    public List<Event> searchEvents(String keyword) {
        String lower = keyword.toLowerCase();
        return events.stream()
                .filter(e ->
                    e.getTitle().toLowerCase().contains(lower)
                    || (e.getLocation()    != null && e.getLocation().toLowerCase().contains(lower))
                    || (e.getDescription() != null && e.getDescription().toLowerCase().contains(lower))
                    || (e.getCategory()    != null && e.getCategory().toLowerCase().contains(lower))
                )
                .collect(Collectors.toList());
    }
    public void addAttendee(Event event, String name, String email)
        throws InvalidEventException {


        if (name == null || name.trim().isEmpty()) {
            throw new InvalidEventException("Nome do participante não pode ser vazio.");
        }
        if (email == null || !email.contains("@")) {
            throw new InvalidEventException("E-mail inválido.");
        }
        event.addAttendee(new Attendee(name.trim(), email.trim()));
        storage.saveEvents(events);
    }
    public void exportDayToFile(LocalDate date, String filePath) throws java.io.IOException {
        List<Event> dayEvents = getEventsForDate(date);


        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.FileWriter(filePath))) {


            pw.println("Eventos do dia " + date);
            pw.println("=".repeat(40));


            if (dayEvents.isEmpty()) {
                pw.println("Nenhum evento neste dia.");
            } else {
                for (Event e : dayEvents) {
                    pw.println("Título:    " + e.getTitle());
                    pw.println("Horário:   " + e.getFormattedDateTime());
                    pw.println("Local:     " + e.getLocation());
                    pw.println("Categoria: " + e.getCategory());
                    pw.println("-".repeat(40));
                }
            }
        }
    }
    private void validateEventFields(String title, LocalDateTime dateTime, String category)
            throws InvalidEventException {


        if (title == null || title.trim().isEmpty()) {
            throw new InvalidEventException("O título do evento não pode ser vazio.");
        }
        if (dateTime == null) {
            throw new InvalidEventException("Data e hora são obrigatórias.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new InvalidEventException("A categoria não pode ser vazia.");
        }
    }
    public List<Event> getAllEvents(){
        return new ArrayList<>(events);
    }








}

