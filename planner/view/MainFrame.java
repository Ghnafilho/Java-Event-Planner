package planner.view;

import planner.model.Event;
import planner.controller.EventController;
import planner.exception.InvalidEventException;
import planner.model.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * MainFrame is the primary user interface for the Event Planner application.
 *
 * This frame provides:
 * - A calendar view on the left side for month navigation and date selection
 * - An event list on the right side showing all events for the selected date
 * - Event details display with attendee information
 * - Event management capabilities (create, edit, delete, add attendees)
 * - Search functionality to find events by keyword
 * - Export functionality to save events to a text file
 *
 * The main components are arranged in a split pane layout with the calendar
 * on the left and event management panel on the right.
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho 
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 1.0
 */
public class MainFrame extends JFrame {

    // Reference to the event controller for all event operations
    private final EventController controller;
    // Calendar panel component for date selection
    private final CalendarPanel calendarPanel;

    // Event list component showing events for the selected date
    private JList<Event> eventList;
    // Model backing the event list
    private DefaultListModel<Event> eventListModel;
    // Text area for displaying selected event details
    private JTextArea detailsArea;
    // Currently selected date
    private LocalDate selectedDate = LocalDate.now();

    /**
     * Constructs the main application window.
     *
     * Initializes all UI components, sets up the calendar panel and event list,
     * and displays events for today's date. The window is centered on the screen
     * and configured to exit the application when closed.
     *
     * @param controller The EventController instance for managing events
     */
    public MainFrame(EventController controller) {
        this.controller = controller;
        this.calendarPanel = new CalendarPanel(controller);

        setTitle("Event Planner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 620);
        setLocationRelativeTo(null); // Center window on screen

        initializeUI();
        updateEventList(selectedDate);
    }

    /**
     * Initializes the main user interface.
     *
     * Sets up the frame layout with:
     * - A top toolbar with action buttons
     * - A central split pane with calendar and event management panels
     */
    private void initializeUI() {
        setLayout(new BorderLayout());

        add(createTopToolbar(), BorderLayout.NORTH);
        add(createCentralPanel(), BorderLayout.CENTER);
    }

    /**
     * Creates the top toolbar containing action buttons and search functionality.
     *
     * The toolbar includes:
     * - "New Event" button to create a new event
     * - "Today" button to navigate back to current date
     * - "Export Day" button to save events to a file
     * - Search field and button to find events by keyword
     *
     * @return A JPanel containing the toolbar with all controls
     */
    private JPanel createTopToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(new Color(248, 250, 252));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        // Create action buttons
        JButton buttonNewEvent = new JButton("+ New Event");
        JButton buttonToday = new JButton("Today");
        JButton buttonExport = new JButton("Export Day");

        // Create search field and button
        JTextField searchField = new JTextField(18);
        searchField.setToolTipText("Search events...");
        JButton buttonSearch = new JButton("🔍");

        // Set up button action listeners
        buttonNewEvent.addActionListener(e -> openNewEventForm());
        buttonToday.addActionListener(e -> {
            selectedDate = LocalDate.now();
            calendarPanel.goToToday();
            updateEventList(selectedDate);
        });
        buttonExport.addActionListener(e -> exportDay());
        buttonSearch.addActionListener(e -> searchEvents(searchField.getText()));
        searchField.addActionListener(e -> searchEvents(searchField.getText()));

        // Add components to toolbar
        toolbar.add(buttonNewEvent);
        toolbar.add(buttonToday);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(new JLabel("Search:"));
        toolbar.add(searchField);
        toolbar.add(buttonSearch);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(buttonExport);

        return toolbar;
    }

    /**
     * Creates the central split pane dividing the calendar and event panel.
     *
     * The left side contains the calendar for date selection.
     * The right side contains the event list and details.
     * The divider can be moved to adjust the relative sizes of both panels.
     *
     * @return A JSplitPane with the calendar on the left and event panel on the right
     */
    private JSplitPane createCentralPanel() {
        // Set up calendar to notify this frame when a date is clicked
        calendarPanel.setOnDateClickCallback(date -> {
            selectedDate = date;
            updateEventList(date);
        });

        // Create right panel with event list and details
        JPanel rightPanel = createRightPanel();

        // Create split pane with calendar on left and event panel on right
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                calendarPanel, rightPanel);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.4);
        return splitPane;
    }

    /**
     * Creates the right panel containing the event list, details, and action buttons.
     *
     * This panel displays:
     * - A list of events for the selected date
     * - Detailed information about the selected event
     * - Action buttons for editing, deleting, and adding attendees
     *
     * @return A JPanel containing the event list and action controls
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create event list component
        eventListModel = new DefaultListModel<>();
        eventList = new JList<>(eventListModel);
        eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventList.addListSelectionListener(e -> {
            // Display details when an event is selected
            if (!e.getValueIsAdjusting()) {
                displayEventDetails(eventList.getSelectedValue());
            }
        });

        JScrollPane eventScrollPane = new JScrollPane(eventList);
        eventScrollPane.setPreferredSize(new Dimension(0, 200));

        // Create details display area
        detailsArea = new JTextArea(6, 35);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane detailsScrollPane = new JScrollPane(detailsArea);

        // Create action buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JButton buttonEdit = new JButton("✏ Edit");
        JButton buttonDelete = new JButton("🗑 Delete");
        JButton buttonAddAttendee = new JButton("👤 Attendee");
        buttonEdit.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonDelete.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonAddAttendee.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension buttonSize = new Dimension(160, 35);

        buttonEdit.setMaximumSize(buttonSize);
        buttonDelete.setMaximumSize(buttonSize);
        buttonAddAttendee.setMaximumSize(buttonSize);

        // Set up button action listeners
        buttonEdit.addActionListener(e -> editSelectedEvent());
        buttonDelete.addActionListener(e -> deleteSelectedEvent());
        buttonAddAttendee.addActionListener(e -> addAttendee());

        // Arrange buttons vertically with spacing
        buttonPanel.add(buttonEdit);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(buttonDelete);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(buttonAddAttendee);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // Arrange components in the main panel
        panel.add(new JLabel("Events for this day:"), BorderLayout.NORTH);
        panel.add(eventScrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(detailsScrollPane, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Updates the event list to show all events for the specified date.
     *
     * Clears the current list and populates it with events from the controller.
     * Clears the details area when refreshing the list.
     *
     * @param date The date to retrieve events for
     */
    private void updateEventList(LocalDate date) {
        eventListModel.clear();
        List<Event> events = controller.getEventsForDate(date);
        for (Event event : events) {
            eventListModel.addElement(event);
        }
        detailsArea.setText("");
    }

    /**
     * Displays detailed information about the selected event.
     *
     * Shows event title, date/time, location, category, description, reminder time,
     * recurrence type (if applicable), and list of attendees.
     *
     * @param event The Event object to display details for (null clears the display)
     */
    private void displayEventDetails(Event event) {
        // If no event selected, clear the display
        if (event == null) {
            detailsArea.setText("");
            return;
        }

        // Build formatted details string
        StringBuilder detailsBuilder = new StringBuilder();
        detailsBuilder.append("Title:       ").append(event.getTitle()).append("\n");
        detailsBuilder.append("Time:        ").append(event.getFormattedDateTime()).append("\n");
        detailsBuilder.append("Location:    ").append(event.getLocation()).append("\n");
        detailsBuilder.append("Category:    ").append(event.getCategory()).append("\n");
        detailsBuilder.append("Description: ").append(event.getDescription()).append("\n");
        detailsBuilder.append("Reminder:    ").append(event.getReminderMinutesBefore()).append(" minutes before\n");
        // Include recurrence info if this is a recurring event
        if (event instanceof RecurringEvent) {
            detailsBuilder.append("Recurrence:  ")
                    .append(((RecurringEvent) event).getRecurrenceType()).append("\n");
        }

        // Include attendees if any exist
        if (!event.getAttendees().isEmpty()) {
            detailsBuilder.append("\nAttendees:\n");
            event.getAttendees().forEach(a -> detailsBuilder.append("  • ").append(a).append("\n"));
        }

        detailsArea.setText(detailsBuilder.toString());
    }

    private void openNewEventForm() {
        EventFormDialog.showCreateDialog(this).ifPresent(this::createEventFromForm);
    }

    private void editSelectedEvent() {
        Event selectedEvent = eventList.getSelectedValue();
        if (selectedEvent == null) {
            JOptionPane.showMessageDialog(this, "Select an event to edit.");
            return;
        }
        EventFormDialog.showEditDialog(this, selectedEvent).ifPresent(data ->
                updateEventFromForm(selectedEvent, data));
    }

    private void createEventFromForm(EventFormDialog.EventFormData data) {
        try {
            if (data.isRecurring()) {
                controller.createRecurringEvent(
                        data.title(), data.dateTime(), data.location(), data.description(),
                        data.category(), data.reminderMinutes(), data.recurrenceType(), data.repetitions());
            } else {
                controller.createEvent(
                        data.title(), data.dateTime(), data.location(), data.description(),
                        data.category(), data.reminderMinutes());
            }
            refreshAfterChange();
        } catch (InvalidEventException ex) {
            showValidationError(ex);
        }
    }

    private void updateEventFromForm(Event existingEvent, EventFormDialog.EventFormData data) {
        try {
            if (existingEvent instanceof RecurringEvent recurringEvent) {
                String[] options = {"This occurrence only", "This and all future occurrences", "Cancel"};
                int choice = JOptionPane.showOptionDialog(this,
                        "This is a recurring event. What would you like to edit?",
                        "Edit recurring event",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);

                if (choice == 0) {
                    controller.updateEvent(recurringEvent, data.title(), data.dateTime(), data.location(),
                            data.description(), data.category(), data.reminderMinutes());
                } else if (choice == 1) {
                    controller.updateFutureOccurrences(recurringEvent, data.title(), data.dateTime(), data.location(),
                            data.description(), data.category(), data.reminderMinutes());
                }
            } else {
                controller.updateEvent(existingEvent, data.title(), data.dateTime(), data.location(),
                        data.description(), data.category(), data.reminderMinutes());
            }
            refreshAfterChange();
        } catch (InvalidEventException ex) {
            showValidationError(ex);
        }
    }

    private void refreshAfterChange() {
        calendarPanel.refresh();
        updateEventList(selectedDate);
    }

    private void showValidationError(InvalidEventException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Deletes the currently selected event.
     *
     * For recurring events, prompts the user to choose whether to delete only
     * this occurrence or this and all future occurrences.
     * For single events, requests confirmation before deletion.
     */
    private void deleteSelectedEvent() {
        Event selectedEvent = eventList.getSelectedValue();
        if (selectedEvent == null) {
            JOptionPane.showMessageDialog(this, "Select an event to delete.");
            return;
        }

        // Handle recurring events with additional options
        if (selectedEvent instanceof RecurringEvent recurringEvent) {
            String[] options = {"This occurrence only", "This and all future occurrences", "Cancel"};
            int choice = JOptionPane.showOptionDialog(this,
                    "This is a recurring event. What would you like to delete?",
                    "Delete recurring event",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            if (choice == 0) {
                // Delete only this occurrence
                controller.deleteEvent(recurringEvent);
            } else if (choice == 1) {
                // Delete this and all future occurrences
                controller.deleteFutureOccurrences(recurringEvent);
            }
            // choice == 2 or closed: do nothing
        } else {
            // For single events, request confirmation
            int confirmation = JOptionPane.showConfirmDialog(this,
                    "Delete \"" + selectedEvent.getTitle() + "\"?",
                    "Confirm deletion", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                controller.deleteEvent(selectedEvent);
            }
        }

        // Refresh UI
        calendarPanel.refresh();
        updateEventList(selectedDate);
    }

    /**
     * Opens a dialog to add an attendee to the selected event.
     *
     * Prompts the user for attendee name and email. For recurring events,
     * allows the user to choose whether to add the attendee to only this
     * occurrence or to this and all future occurrences.
     */
    private void addAttendee() {
        Event selectedEvent = eventList.getSelectedValue();
        if (selectedEvent == null) {
            JOptionPane.showMessageDialog(this, "Select an event first.");
            return;
        }

        // Create form fields for attendee information
        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);

        // Show dialog and get user response
        int result = JOptionPane.showConfirmDialog(this, formPanel,
                "Add Attendee", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String email = emailField.getText();

                if (selectedEvent instanceof RecurringEvent recurringEvent) {
                    // For recurring events, ask where to add the attendee
                    String[] options = {
                            "This event only",
                            "This and all future occurrences",
                            "Cancel"
                    };

                    int choice = JOptionPane.showOptionDialog(this,
                            "This is a recurring event. Where would you like to add the attendee?",
                            "Recurring event",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);

                    if (choice == 0) {
                        // Add attendee to this occurrence only
                        controller.addAttendee(recurringEvent, name, email);
                    } else if (choice == 1) {
                        // Add attendee to this and all future occurrences
                        controller.addAttendeeToFutureOccurrences(recurringEvent, name, email);
                    }
                    // choice == 2: user cancelled
                } else {
                    // Add attendee to single event
                    controller.addAttendee(selectedEvent, name, email);
                }

                // Refresh the display with updated information
                displayEventDetails(selectedEvent);

            } catch (InvalidEventException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Searches for events matching the given keyword.
     *
     * The search looks for matches in event title, location, description, and category.
     * Results are displayed in the event list. If the search field is empty,
     * the list returns to showing events for the selected date.
     *
     * @param keyword The search term to find in events
     */
    private void searchEvents(String keyword) {
        // If search is empty, show events for selected date
        if (keyword == null || keyword.trim().isEmpty()) {
            updateEventList(selectedDate);
            return;
        }

        // Perform search and display results
        List<Event> searchResults = controller.searchEvents(keyword.trim());
        eventListModel.clear();
        searchResults.forEach(eventListModel::addElement);
        detailsArea.setText(searchResults.size() + " result(s) found for: " + keyword);
    }

    /**
     * Exports all events for the currently selected date to a text file.
     *
     * Prompts the user to select a file location and name, then saves
     * the day's events in a formatted text file. Shows confirmation
     * or error message based on the result.
     */
    private void exportDay() {
        // Create file chooser dialog
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(
                "events_" + selectedDate + ".txt"));
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Export events to selected file
                controller.exportDayToFile(selectedDate,
                        fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "File exported successfully!");
            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
