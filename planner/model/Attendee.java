package planner.model;

/**
 * Attendee is an entity that represents an event participant.
 *
 * <p><strong>Design Pattern - Encapsulation:</strong> The 'name' and 'email' attributes are
 * declared as 'private', protecting the internal state of the object from unauthorized
 * modifications. Access to and modification of these data occur exclusively through public
 * accessor and mutator methods (getters and setters).</p>
 *
 * <p><strong>Data Persistence:</strong> The class provides a {@link #toCSV()} method for
 * serializing attendee information to a text file format. This method includes data sanitization
 * by replacing reserved delimiters (pipe '|' and semicolon ';') with spaces to prevent file
 * parsing errors.</p>
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 2.0
 */
public class Attendee {
    
    private String name;
    private String email;

    /**
     * Constructs an Attendee with the specified name and email address.
     *
     * <p>Initializes a new attendee participant with the provided contact information. Both
     * parameters are required to establish the attendee's basic identity and communication
     * details.</p>
     *
     * @param name  The attendee's name or display name
     * @param email The attendee's email address for contact purposes
     */
    public Attendee(String name, String email) {
        this.name = name;
        this.email = email;
    }

    /**
     * Returns the attendee's name.
     *
     * @return The {@link String} representing the attendee's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the attendee's name.
     *
     * @param name The new name for the attendee
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the attendee's email address.
     *
     * @return The {@link String} representing the attendee's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the attendee's email address.
     *
     * @param email The new email address for the attendee
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Converts the attendee's information to a CSV-formatted string for file persistence.
     *
     * <p><strong>Data Sanitization:</strong> This method implements input sanitization by
     * replacing reserved delimiters (pipe '|' and semicolon ';') with spaces in both the name
     * and email fields. This prevents file parsing errors if the user enters these special
     * characters in the name or email fields.</p>
     *
     * <p>The output format is: "Name|Email" where reserved characters have been replaced.</p>
     *
     * @return A {@link String} formatted as "Name|Email" with sanitized content
     */
    public String toCSV() {
        String safeName = name != null ? name.replace("|", " ").replace(";", " ") : "";
        String safeEmail = email != null ? email.replace("|", " ").replace(";", " ") : "";
        return safeName + "|" + safeEmail;
    }

    /**
     * Returns a string representation of the attendee for display purposes.
     *
     * <p><strong>Design Pattern - Polymorphism:</strong> This method overrides the
     * {@link Object#toString()} method inherited from the superclass, defining a custom textual
     * representation tailored for use in graphical user interface components and logging.</p>
     *
     * <p>The output format is: "Name (email)"</p>
     *
     * @return A {@link String} representation in the format "Name (email)"
     */
    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}
