package planner.model;

/**
 * * Descrição: Entidade que representa um participante do evento
 * * Conceito OOP Aplicado - ENCAPSULAMENTO:
 * Os atributos 'name' e 'email' são declarados como 'private'. Isso protege o estado
 * interno do objeto contra modificações indevidas. O acesso e a alteração desses 
 * dados ocorrem exclusivamente através de métodos públicos (getters e setters)
 */
public class Attendee {
    
    private String name;
    private String email;

    /**
     * Construtor da classe Attendee
     * Exige a inicialização do estado básico do participante.
     * * @param name Nome do participante.
     * @param email Endereço de e-mail do participante.
     */
    public Attendee(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Métodos de acesso (getters e setters)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /**
     * Formata os dados do participante para persistência em arquivo de texto.
     * Implementa uma sanitização de dados, isto é, substitui delimitadores reservados 
     * (como o pipe '|' e o ponto e vírgula ';') por espaços. Isso previne falhas 
     * na leitura do arquivo caso o usuário insira esses caracteres no nome ou e-mail.
     * * @return String formatada no padrão "Nome|Email".
     */
    public String toCSV() {
        String safeName = name != null ? name.replace("|", " ").replace(";", " ") : "";
        String safeEmail = email != null ? email.replace("|", " ").replace(";", " ") : "";
        return safeName + "|" + safeEmail;
    }

    /**
     * Conceito OOP Aplicado - POLIMORFISMO (sobrescrita/override):
     * Sobrescreve o método toString() herdado da superclasse Object.
     * Define a representação textual do objeto em componentes de interface gráfica
     */
    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}