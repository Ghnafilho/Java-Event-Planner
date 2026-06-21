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
 * Description:
 * Reusable dialog component responsible for collecting event information
 * from the user through a graphical form.
 *
 * Applied Design Principle - SEPARATION OF CONCERNS:
 * Centralizes all event form creation, user input collection, and date parsing
 * logic in a single component. This prevents code duplication and keeps the
 * MainFrame class focused on application workflow and user interaction.
 *
 * Applied Architecture - MVC:
 * This class belongs to the View layer and is responsible only for presenting
 * and collecting data. Business rules and validation remain delegated to the
 * Controller and Model layers.
 */

public final class EventFormDialog {

    private static final String DATE_PLACEHOLDER = "MM/dd/yyyy HH:mm";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_PLACEHOLDER);

    private static final String[] CATEGORIES =
            {"Meeting", "Birthday", "Appointment", "Reminder", "Other"};


    private EventFormDialog() {}

    /**
     * Immutable data transfer object (DTO) used to transport
     * form values collected from the user interface.
     *
     * Applied OOP Concept - ENCAPSULATION:
     * The Java Record feature automatically generates accessors and
     * guarantees immutability after object creation, preventing accidental
     * modification of user input data.
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
        public boolean isRecurring() {
            return recurrenceType != null
                    && recurrenceType != RecurringEvent.RecurrenceType.NONE;
        }
    }


    /**
     * Displays a dialog for creating a new event.
     *
     * @param parent The parent component where the dialog will be displayed
     * @return An Optional containing the collected form data if successful,
     *         or empty if the dialog is cancelled.
     */
    public static Optional<EventFormData> showCreateDialog(Component parent) {
        return showDialog(parent, null, true);
    }


    /**
     * Displays a dialog for editing an existing event.
     *
     * @param parent The parent component where the dialog will be displayed
     * @param existingEvent The Event object to be edited
     * @return An Optional containing the collected form data if successful,
     *         or empty if the dialog is cancelled.
     */
    public static Optional<EventFormData> showEditDialog(Component parent, Event existingEvent) {
        return showDialog(parent, existingEvent, false);
    }


    /**
    * Displays a dialog for creating or editing an event.
    *
    * Validation Behaviour:
    * * The dialog remains open until the user provides valid input or cancels.
    * * If validation fails, an error message is displayed and the form is shown
    * again with all previously entered values preserved.
    * * The title field is mandatory and cannot be empty.
    * * The date/time field must follow the format "MM/dd/yyyy HH:mm".
    *
    * Recurrence Behaviour:
    * * When creating a new event, recurrence options are available.
    * * The repetitions field is dynamically shown only when a recurrence type
    * other than "No recurrence" is selected.
    *
    * @param parent The parent component where the dialog will be displayed
    * @param existingEvent The Event object to be edited (null for new events)
    * @param includeRecurrence Whether recurrence controls should be displayed
    * @return An Optional containing the collected form data when validation
        succeeds, or an empty Optional if the user cancels the operation.


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
     * Creates a JTextField with a placeholder and focus listener
     * for date/time input.
     *
     * @return A JTextField configured for date/time input
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
     * Parses a recurrence type string into a RecurringEvent.RecurrenceType enum value.
     *
     * @param recurrenceString The string representation of the recurrence type
     * @return The corresponding RecurringEvent.RecurrenceType enum value
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

