package planner.model;

import java.time.LocalDateTime;

/**
 * * Descrição: Representa um evento com regras de repetição cronológica
 * * Conceito OOP Aplicado - HERANÇA:
 * Utiliza a palavra-chave 'extends' para herdar a estrutura e o comportamento 
 * da superclasse 'Event'. Isso permite a extensão da classe base com novas 
 * características exclusivas (recurrenceType), promovendo reuso de código.
 */
public class RecurringEvent extends Event {

    /**
     * Type safety (segurança de tipos):
     * A utilização de Enum restringe os valores possíveis de repetição,
     * garantindo a integridade dos dados cronológicos
     */
    public enum RecurrenceType {
        DAILY, WEEKLY, MONTHLY, NONE
    }

    private RecurrenceType recurrenceType;

    /**
     * Construtor da subclasse.
     * A instrução 'super' é utilizada para invocar o construtor da superclasse 'Event',
     * garantindo que os atributos herdados sejam inicializados corretamente.
     */
    public RecurringEvent(String title, LocalDateTime dateTime, String location, String description, String category, int reminderMinutesBefore, RecurrenceType recurrenceType) {
        super(title, dateTime, location, description, category, reminderMinutesBefore);
        this.recurrenceType = recurrenceType;
    }

    public RecurrenceType getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(RecurrenceType recurrenceType) { this.recurrenceType = recurrenceType; }

    /**
     * Calcula a data e hora da proxima ocorrência do evento
     * baseando-se no tipo de recorrência associado ao objeto.
     */
    public LocalDateTime calculateNextOccurrence() {
        switch (recurrenceType) {
            case DAILY: return this.dateTime.plusDays(1);
            case WEEKLY: return this.dateTime.plusWeeks(1);
            case MONTHLY: return this.dateTime.plusMonths(1);
            default: return this.dateTime;
        }
    }

    /**
     * Conceito OOP Aplicado - POLIMORFISMO:
     * Sobrescreve o método de serialização. A chamada 'super.toCSV()' reaproveita 
     * a lógica implementada na classe pai, adicionando o sufixo necessário para 
     * identificacao da subclasse durante o carregamento dos dados
     */
    @Override
    public String toCSV() {
        return super.toCSV() + ";[REC:" + recurrenceType.name() + "]";
    }
}