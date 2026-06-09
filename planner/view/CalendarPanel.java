package planner.view;

import planner.controller.EventController;
import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Consumer;
import java.util.List;

/**
 * CalendarPanel is a custom JPanel that displays an interactive calendar view for event management.
 *
 * Features:
 * - Monthly calendar grid showing all days of the selected month
 * - Navigation buttons to move between months
 * - Visual indicators for today's date (blue highlight)
 * - Visual indicators for selected date (light blue highlight)
 * - Red dots on dates that have scheduled events
 * - Click handler to notify parent components when a date is selected
 * - Automatic refresh when events are added or the month changes
 *
 * The panel uses a callback pattern (Consumer<LocalDate>) to notify parent components
 * (typically MainFrame) when the user clicks on a date.
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho 
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 1.0
 */
public class CalendarPanel extends JPanel {

    // Reference to the event controller for accessing event data
    private final EventController controller;

    // The currently displayed month and year
    private YearMonth currentMonth;

    // The currently selected date (highlighted in light blue)
    private LocalDate selectedDate;

    // Callback function that gets invoked when the user clicks on a date
    // Used to notify the parent component (MainFrame) of date selection
    private Consumer<LocalDate> onDateClickCallback;

    /**
     * Constructs a CalendarPanel with a reference to the EventController.
     *
     * Initializes the calendar to display the current month and sets the
     * selected date to today. The panel layout is configured and the calendar
     * grid is built.
     *
     * @param controller The EventController instance for accessing events
     */
    public CalendarPanel(EventController controller) {
        this.controller = controller;
        this.currentMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();
        setLayout(new BorderLayout());
        buildPanel();
    }

    /**
     * Sets the callback function that gets invoked when a date is clicked.
     *
     * This callback is typically set by the MainFrame to receive notifications
     * whenever the user clicks on a date in the calendar. The callback receives
     * the selected LocalDate as a parameter.
     *
     * @param callback A Consumer that accepts a LocalDate when a date is clicked
     */
    public void setOnDateClickCallback(Consumer<LocalDate> callback) {
        this.onDateClickCallback = callback;
    }

    /**
     * Refreshes the entire calendar panel.
     *
     * This method removes all components, rebuilds the calendar grid, and
     * repaints the panel. Call this method after:
     * - Changing the month
     * - Adding or removing events
     * - Selecting a different date
     */
    public void refresh() {
        removeAll();
        buildPanel();
        revalidate();
        repaint();
    }

    /**
     * Navigates the calendar back to the current month and date.
     *
     * Sets the displayed month to today and the selected date to today,
     * then refreshes the panel to reflect these changes.
     */
    public void goToToday() {
        currentMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        refresh();
    }

    /**
     * Builds the complete calendar panel structure.
     *
     * This method is responsible for creating and arranging all components
     * that make up the calendar view:
     * - Navigation bar (previous/next month buttons and title)
     * - Calendar grid with days of the week header and date cells
     */
    private void buildPanel() {
        add(createNavigationBar(), BorderLayout.NORTH);
        add(createCalendarGrid(), BorderLayout.CENTER);
    }

    /**
     * Creates the navigation bar at the top of the calendar.
     *
     * The navigation bar contains:
     * - Left arrow button to go to the previous month
     * - Month and year title in the center
     * - Right arrow button to go to the next month
     *
     * The bar has a blue background and white text for visual appeal.
     *
     * @return A JPanel containing the navigation controls
     */
    private JPanel createNavigationBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(new Color(37, 99, 235));

        // Create previous month button
        JButton previousButton = new JButton("◀");
        // Create next month button
        JButton nextButton = new JButton("▶");

        // Add click handlers for month navigation
        previousButton.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refresh();
        });
        nextButton.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refresh();
        });

        // Create the title label showing current month and year
        // Uses Portuguese locale for month name display
        String monthName = currentMonth.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.of("pt", "BR"));
        JLabel title = new JLabel(
                monthName.substring(0, 1).toUpperCase() + monthName.substring(1)
                        + " " + currentMonth.getYear(),
                SwingConstants.CENTER
        );
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        // Add components to navigation bar
        nav.add(previousButton, BorderLayout.WEST);
        nav.add(title, BorderLayout.CENTER);
        nav.add(nextButton, BorderLayout.EAST);
        return nav;
    }

    /**
     * Creates the calendar grid displaying all dates of the current month.
     *
     * The grid includes:
     * - Header row with day-of-week abbreviations (Sun, Mon, Tue, etc.)
     * - Day cells for each date in the month, properly aligned with the day of week
     * - Empty cells to align the first day of the month to the correct column
     *
     * @return A JPanel containing the complete calendar grid
     */
    private JPanel createCalendarGrid() {
        JPanel container = new JPanel(new BorderLayout());

        // Create header row showing day abbreviations
        JPanel headerRow = new JPanel(new GridLayout(1, 7));
        String[] dayAbbreviations = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String dayAbbr : dayAbbreviations) {
            JLabel label = new JLabel(dayAbbr, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            headerRow.add(label);
        }

        // Create the main grid for date cells
        JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));

        // Get all dates in this month that have events
        List<LocalDate> datesWithEvents = controller.getDatesWithEventsInMonth(
                currentMonth.getYear(), currentMonth.getMonthValue());

        // Calculate which column the first day of the month falls into
        // getDayOfWeek().getValue() returns: MON=1, TUE=2, ..., SUN=7
        // We need to convert to: SUN=0, MON=1, ..., SAT=6
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int columnOfFirstDay = firstDayOfMonth.getDayOfWeek().getValue() % 7;

        // Fill empty cells before the first day of the month
        for (int i = 0; i < columnOfFirstDay; i++) {
            grid.add(new JPanel()); // Empty cell for alignment
        }

        // Create cells for each day of the month
        for (int dayOfMonth = 1; dayOfMonth <= currentMonth.lengthOfMonth(); dayOfMonth++) {
            LocalDate date = currentMonth.atDay(dayOfMonth);
            grid.add(createDateCell(date, datesWithEvents));
        }

        // Add header and grid to container
        container.add(headerRow, BorderLayout.NORTH);
        container.add(grid, BorderLayout.CENTER);
        return container;
    }

    /**
     * Creates a single date cell for display in the calendar grid.
     *
     * Each cell is a clickable panel that:
     * - Displays the day of month number
     * - Shows a blue background if it's today
     * - Shows a light blue background if it's the selected date
     * - Displays a red dot if the date has events
     * - Invokes the date click callback when clicked
     *
     * @param date            The date to create a cell for
     * @param datesWithEvents List of dates that have scheduled events
     *
     * @return A JPanel representing a single calendar date cell
     */
    private JPanel createDateCell(LocalDate date, List<LocalDate> datesWithEvents) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        // Determine cell state
        boolean isToday = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);
        boolean hasEvents = datesWithEvents.contains(date);

        // Create label for the day number
        JLabel dayNumber = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
        dayNumber.setFont(new Font("Segoe UI", isToday ? Font.BOLD : Font.PLAIN, 13));

        // Apply background color based on cell state
        if (isToday) {
            // Today's date: blue background with white text
            cell.setBackground(new Color(37, 99, 235));
            dayNumber.setForeground(Color.WHITE);
        } else if (isSelected) {
            // Selected date: light blue background
            cell.setBackground(new Color(191, 219, 254));
        } else {
            // Other dates: white background
            cell.setBackground(Color.WHITE);
        }

        // Add day number to cell
        cell.add(dayNumber, BorderLayout.CENTER);

        // Add visual indicator (red dot) if this date has events
        if (hasEvents) {
            JPanel eventIndicator = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Draw a small red circle to indicate events exist on this date
                    g.setColor(new Color(220, 38, 38));
                    g.fillOval(getWidth() / 2 - 3, 1, 6, 6);
                }
            };
            eventIndicator.setOpaque(false);
            eventIndicator.setPreferredSize(new Dimension(10, 10));
            cell.add(eventIndicator, BorderLayout.SOUTH);
        }

        // Make cell clickable (cursor changes to hand and responds to clicks)
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cell.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Update selected date
                selectedDate = date;
                // Refresh panel to show new selection
                refresh();
                // Notify parent component of date selection via callback
                if (onDateClickCallback != null) {
                    onDateClickCallback.accept(date);
                }
            }
        });

        return cell;
    }
}
