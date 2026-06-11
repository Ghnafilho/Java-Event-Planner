package planner.model;

/**
 * Description: Entity that represents an event participant.
 *
 * OOP Concept Applied - ENCAPSULATION:
 * The 'name' and 'email' attributes are declared as 'private'. This protects
 * the internal state of the object from unauthorized modifications. Access to
 * and modification of these data occur exclusively through public methods
 * (getters and setters).
 */
public class Attendee {
    
    private String name;
    private String email;

/**
 * Constructor for the Attendee class.
 * Requires initialization of the participant's basic state.
 *
 * @param name  Participant's name.
 * @param email Participant's email address.
 */
    public Attendee(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Access methods (getters and setters)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

/**
 * Formats the participant's data for persistence in a text file.
 * Implements data sanitization by replacing reserved delimiters
 * (such as the pipe '|' and semicolon ';') with spaces. This prevents
 * file parsing errors if the user enters these characters in the name
 * or email fields.
 *
 * @return A String formatted as "Name|Email".
 */
    public String toCSV() {
        String safeName = name != null ? name.replace("|", " ").replace(";", " ") : "";
        String safeEmail = email != null ? email.replace("|", " ").replace(";", " ") : "";
        return safeName + "|" + safeEmail;
    }

/**
 * OOP Concept Applied - POLYMORPHISM (method overriding):
 * Overrides the toString() method inherited from the Object superclass.
 * Defines the textual representation of the object for use in graphical
 * user interface components.
 */
    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}