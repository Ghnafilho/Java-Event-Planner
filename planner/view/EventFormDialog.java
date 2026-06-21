package planner.view;

import planner.model.Event;
import planner.model.RecurringEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;


/**
 * EventFormDialog is a reusable dialog component responsible for collecting event information
 * from the user through a graphical form.
 *
 * <p><strong>Design Principles:</strong></p>
 * <ul>
 *   <li><strong>Separation of Concerns:</strong> Centralizes all event form creation, user input
 *       collection, and date parsing logic in a single component. This prevents code duplication
 *       and keeps the MainFrame class focused on application workflow and user interaction.</li>
 *   <li><strong>MVC Architecture:</strong> This class belongs to the View layer and is responsible
 *       only for presenting and collecting data. Business rules and validation remain delegated to
 *       the Controller and Model layers.</li>
 * </ul>
 *
 * <p><strong>Validation Behaviour:</strong></p>
 * <ul>
 *   <li>The dialog remains open until the user provides valid input or cancels.</li>
 *   <li>If validation fails, an error message is displayed and the form is shown again with all
 *       previously entered values preserved.</li>
 *   <li>The title field is mandatory and cannot be empty.</li>
 *   <li>The date/time field must follow the format "MM/dd/yyyy HH:mm".</li>
 * </ul>
 *
 * <p><strong>Recurrence Behaviour:</strong></p>
 * <ul>
 *   <li>When creating a new event, recurrence options are available.</li>
 *   <li>The repetitions field is dynamically shown only when a recurrence type other than
 *       "No recurrence" is selected.</li>
 * </ul>
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 2.0
 */
public final class EventFormDialog {

    private static final String DATE_PLACEHOLDER = "MM/dd/yyyy HH:mm";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_PLACEHOLDER);

    private static final String[] CATEGORIES =
            {"Meeting", "Birthday", "Appointment", "Reminder", "Other"};


    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private EventFormDialog() {}

    /**
     * Immutable data transfer object (DTO) used to transport form values collected from the
     * user interface.
     *
     * <p><strong>Design Pattern - Encapsulation:</strong> The Java Record feature automatically
     * generates accessors and guarantees immutability after object creation, preventing accidental
     * modification of user input data.</p>
     *
     * @param title The event title (mandatory, non-empty)
     * @param dateTime The event date and time in the format "MM/dd/yyyy HH:mm"
     * @param location The event location (optional)
     * @param description The event description (optional)
     * @param category The event category (e.g., "Meeting", "Birthday", "Appointment", "Reminder",
     *                 "Other")
     * @param reminderMinutes The number of minutes before the event to display a reminder
     * @param recurrenceType The type of recurrence for this event
     * @param repetitions The number of times the event should repeat (only applicable when
     *                    recurrence is enabled)
     */
    public record EventFormData(
            String title,
            LocalDateTime dateTime,
            String location,
            String description,
            String category,
            int reminderMinutes,
            RecurringEvent.RecurrenceType recurrenceType,
            int repetitions
    ) {
        /**
         * Checks whether this event has a recurrence type other than NONE.
         *
         * @return {@code true} if the event is recurring, {@code false} otherwise
         */
        public boolean isRecurring() {
            return recurrenceType != null
                    && recurrenceType != RecurringEvent.RecurrenceType.NONE;
        }
    }


    /**
     * Displays a dialog for creating a new event.
     *
     * <p>This is a convenience method that calls {@link #showDialog(Component, Event, boolean)}
     * with {@code existingEvent} set to {@code null} and {@code includeRecurrence} set to
     * {@code true}.</p>
     *
     * @param parent The parent {@link Component} where the dialog will be displayed
     * @return An {@link Optional} containing the collected {@link EventFormData} if the user
     *         successfully completes the form, or an empty {@link Optional} if the dialog is
     *         cancelled
     */
    public static Optional<EventFormData> showCreateDialog(Component parent) {
        return showDialog(parent, null, true);
    }


    /**
     * Displays a dialog for editing an existing event.
     *
     * <p>This is a convenience method that calls {@link #showDialog(Component, Event, boolean)}
     * with {@code existingEvent} set to the provided event and {@code includeRecurrence} set to
     * {@code false}. The form fields are populated with the existing event's data.</p>
     *
     * @param parent The parent {@link Component} where the dialog will be displayed
     * @param existingEvent The {@link Event} object to be edited
     * @return An {@link Optional} containing the collected {@link EventFormData} if the user
     *         successfully completes the form, or an empty {@link Optional} if the dialog is
     *         cancelled
     */
    public static Optional<EventFormData> showEditDialog(Component parent, Event existingEvent) {
        return showDialog(parent, existingEvent, false);
    }


    /**
     * Displays a dialog for creating or editing an event with form validation and error handling.
     *
     * <p>This method creates and manages a modal dialog containing input fields for event
     * information. The dialog remains visible until the user either submits valid data or
     * cancels the operation.</p>
     *
     * <p><strong>Form Fields:</strong></p>
     * <ul>
     *   <li><strong>Title:</strong> Required text field; cannot be empty</li>
     *   <li><strong>Date/Time:</strong> Required field in format "MM/dd/yyyy HH:mm"</li>
     *   <li><strong>Location:</strong> Optional text field</li>
     *   <li><strong>Category:</strong> Dropdown menu with predefined categories</li>
     *   <li><strong>Reminder:</strong> Spinner for minutes before event notification</li>
     *   <li><strong>Recurrence:</strong> Dropdown menu (only shown when creating new events)</li>
     *   <li><strong>Repetitions:</strong> Spinner for recurrence count (only shown when recurrence
     *       type is selected)</li>
     *   <li><strong>Description:</strong> Optional text area for additional details</li>
     * </ul>
     *
     * <p><strong>Validation Logic:</strong></p>
     * <ul>
     *   <li>Title must not be empty or whitespace-only</li>
     *   <li>Date/Time must follow the format "MM/dd/yyyy HH:mm"</li>
     *   <li>If validation fails, an error dialog is shown and the form reappears with user input
     *       preserved</li>
     * </ul>
     *
     * <p><strong>Recurrence Handling:</strong></p>
     * <ul>
     *   <li>When {@code includeRecurrence} is {@code true}, recurrence controls are displayed</li>
     *   <li>The "Repetitions" field visibility is toggled based on the selected recurrence type</li>
     *   <li>When creating new events, full recurrence support is available</li>
     *   <li>When editing existing events, recurrence controls are hidden</li>
     * </ul>
     *
     * @param parent The parent {@link Component} where the dialog will be displayed. Used to
     *               position the dialog relative to the parent window
     * @param existingEvent The {@link Event} object to be edited, or {@code null} if creating a
     *                      new event. When provided, all form fields are populated with the
     *                      event's existing data
     * @param includeRecurrence Whether recurrence control fields should be included in the dialog.
     *                          Should be {@code true} for new events and {@code false} for
     *                          editing existing events
     * @return An {@link Optional} containing the collected {@link EventFormData} when the user
     *         successfully submits the form, or an empty {@link Optional} if the user cancels
     *         the dialog
     * @throws DateTimeParseException If the entered date/time string does not match the required
     *                               format (caught and re-displayed as a user-friendly error)
     */
    private static Optional<EventFormData> showDialog(
        Component parent,
        Event existingEvent,
        boolean includeRecurrence) {

        // Create form fields for event information
        JTextField titleField = new JTextField(20);
        JTextField dateTimeField = createDateTimeField();
        JTextField locationField = new JTextField(20);
        JTextArea descriptionArea = new JTextArea(3, 20);

        JComboBox<String> categoryComboBox =
                new JComboBox<>(CATEGORIES);

        JSpinner reminderSpinner =
                new JSpinner(new SpinnerNumberModel(30, 0, 10080, 10));

        JSpinner repetitionsSpinner =
                new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));

        JComboBox<String> recurrenceComboBox =
                new JComboBox<>(new String[]{
                        "No recurrence",
                        "Daily",
                        "Weekly",
                        "Monthly"
                });

        // Populate fields when editing an existing event
        if (existingEvent != null) {
            titleField.setText(existingEvent.getTitle());
            dateTimeField.setText(
                    existingEvent.getDateTime().format(DATE_FORMATTER));
            locationField.setText(existingEvent.getLocation());
            descriptionArea.setText(existingEvent.getDescription());
            categoryComboBox.setSelectedItem(existingEvent.getCategory());
            reminderSpinner.setValue(
                    existingEvent.getReminderMinutesBefore());
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 6, 6));

        formPanel.add(new JLabel("Title:*"));
        formPanel.add(titleField);

        formPanel.add(new JLabel("Date/Time:*"));
        formPanel.add(dateTimeField);

        formPanel.add(new JLabel("Location:"));
        formPanel.add(locationField);

        formPanel.add(new JLabel("Category:*"));
        formPanel.add(categoryComboBox);

        formPanel.add(new JLabel("Reminder (min):"));
        formPanel.add(reminderSpinner);

        if (includeRecurrence) {

            JLabel recurrenceLabel = new JLabel("Recurrence:");
            formPanel.add(recurrenceLabel);
            formPanel.add(recurrenceComboBox);

            JLabel repetitionsLabel =
                    new JLabel("Repetitions (1-365):");

            repetitionsLabel.setVisible(false);
            repetitionsSpinner.setVisible(false);

            formPanel.add(repetitionsLabel);
            formPanel.add(repetitionsSpinner);

            recurrenceComboBox.addActionListener(e -> {
                String selected =
                        (String) recurrenceComboBox.getSelectedItem();

                boolean isRecurring =
                        selected != null &&
                        !selected.equals("No recurrence");

                repetitionsLabel.setVisible(isRecurring);
                repetitionsSpinner.setVisible(isRecurring);

                formPanel.revalidate();
                formPanel.repaint();

                Window window =
                        SwingUtilities.getWindowAncestor(formPanel);

                if (window != null) {
                    window.pack();
                }
            });
        }

        formPanel.add(new JLabel("Description:"));
        formPanel.add(new JScrollPane(descriptionArea));

        String dialogTitle =
                existingEvent == null
                        ? "New Event"
                        : "Edit Event";

        while (true) {

            int result = JOptionPane.showConfirmDialog(
                    parent,
                    formPanel,
                    dialogTitle,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return Optional.empty();
            }

            try {

                String title = titleField.getText().trim();

                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            parent,
                            "Title is required.",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    continue;
                }

                LocalDateTime dateTime =
                        LocalDateTime.parse(
                                dateTimeField.getText().trim(),
                                DATE_FORMATTER);

                String recurrenceString =
                        includeRecurrence
                                ? (String) recurrenceComboBox.getSelectedItem()
                                : "No recurrence";

                return Optional.of(
                        new EventFormData(
                                title,
                                dateTime,
                                locationField.getText().trim(),
                                descriptionArea.getText().trim(),
                                (String) categoryComboBox.getSelectedItem(),
                                (int) reminderSpinner.getValue(),
                                parseRecurrenceType(recurrenceString),
                                includeRecurrence
                                        ? (int) repetitionsSpinner.getValue()
                                        : 1
                        )
                );

            } catch (DateTimeParseException ex) {

                JOptionPane.showMessageDialog(
                        parent,
                        "Invalid date format.\nUse: " + DATE_PLACEHOLDER,
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        }


    /**
     * Creates a {@link JTextField} configured for date/time input with placeholder text and
     * automatic focus management.
     *
     * <p>The field displays a placeholder text when empty and unfocused. When the user focuses
     * on the field, the placeholder is cleared to allow input. If the field loses focus without
     * input, the placeholder is restored.</p>
     *
     * @return A {@link JTextField} configured with placeholder behaviour and focus listeners for
     *         date/time input
     */
    private static JTextField createDateTimeField() {
        JTextField field = new JTextField(DATE_PLACEHOLDER, 20);
        field.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(DATE_PLACEHOLDER)) {
                    field.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(DATE_PLACEHOLDER);
                }
            }
        });
        return field;
    }


    /**
     * Parses a recurrence type string into its corresponding {@link RecurringEvent.RecurrenceType}
     * enum value.
     *
     * <p>Maps user-friendly recurrence labels to their corresponding enum values:
     * <ul>
     *   <li>"Daily" → {@link RecurringEvent.RecurrenceType#DAILY}</li>
     *   <li>"Weekly" → {@link RecurringEvent.RecurrenceType#WEEKLY}</li>
     *   <li>"Monthly" → {@link RecurringEvent.RecurrenceType#MONTHLY}</li>
     *   <li>Any other value → {@link RecurringEvent.RecurrenceType#NONE}</li>
     * </ul>
     * </p>
     *
     * @param recurrenceString The string representation of the recurrence type (e.g., "Daily",
     *                         "Weekly", "Monthly", "No recurrence")
     * @return The corresponding {@link RecurringEvent.RecurrenceType} enum value. Returns
     *         {@link RecurringEvent.RecurrenceType#NONE} if the string does not match any
     *         predefined recurrence type
     */
    private static RecurringEvent.RecurrenceType parseRecurrenceType(String recurrenceString) {
        return switch (recurrenceString) {
            case "Daily" -> RecurringEvent.RecurrenceType.DAILY;
            case "Weekly" -> RecurringEvent.RecurrenceType.WEEKLY;
            case "Monthly" -> RecurringEvent.RecurrenceType.MONTHLY;
            default -> RecurringEvent.RecurrenceType.NONE;
        };
    }
}
