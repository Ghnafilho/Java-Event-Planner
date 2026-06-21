package planner.view;

import planner.controller.EventController;
import planner.model.Event;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Consumer;
import java.util.List;

/**
 * CalendarPanel is a custom JPanel that displays an interactive calendar view for event management.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Three view modes: Daily, Weekly and Monthly calendar grid</li>
 *   <li>Navigation buttons to move between days, weeks or months depending on the active view</li>
 *   <li>Visual indicators for today's date (blue highlight)</li>
 *   <li>Visual indicators for selected date (light blue highlight)</li>
 *   <li>Colored dots on dates that have scheduled events, grouped by category</li>
 *   <li>Click handler to notify parent components when a date is selected</li>
 *   <li>Automatic refresh when events are added or the displayed period changes</li>
 *   <li>Toggle buttons on the navigation bar to switch between view modes</li>
 * </ul>
 *
 * <p>The panel uses a callback pattern ({@link Consumer}&lt;{@link LocalDate}&gt;) to notify parent
 * components (typically MainFrame) when the user clicks on a date.</p>
 *
 * @author Gustavo Henrique Nogueira de Andrade Filho
 * @author Pedro Rocha Dantas
 * @author Leonardo Oliveira Eid
 * @version 2.0
 */
public class CalendarPanel extends JPanel {

    // Reference to the event controller for accessing event data
    private final EventController controller;

    // The currently displayed month and year (used in MONTHLY view)
    private YearMonth currentMonth;

    // The currently displayed week anchor date (used in WEEKLY view)
    private LocalDate currentWeekStart;

    // The currently displayed day (used in DAILY view)
    private LocalDate currentDay;

    // The currently selected date (highlighted in light blue)
    private LocalDate selectedDate;

    // Callback function invoked when the user clicks on a date
    // Used to notify the parent component (MainFrame) of date selection
    private Consumer<LocalDate> onDateClickCallback;

    /**
     * Enum representing the three available calendar view modes.
     * Switching between modes changes both navigation behaviour and grid layout.
     */
    public enum ViewMode {
        /** Shows a single day with its events listed by hour */
        DAILY,
        /** Shows all seven days of the selected week */
        WEEKLY,
        /** Shows the entire month as a traditional calendar grid */
        MONTHLY
    }

    // The active view mode; defaults to MONTHLY on construction
    private ViewMode viewMode;

    // Formatter used to display dates in the daily/weekly header labels
    private static final DateTimeFormatter LABEL_FORMAT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Constructs a CalendarPanel with a reference to the EventController.
     *
     * <p>Initialises the calendar to display the current month and sets the selected date to today.
     * The default view mode is {@link ViewMode#MONTHLY}. The panel layout is configured and the
     * calendar grid is built.</p>
     *
     * @param controller The {@link EventController} instance used for accessing events
     */
    public CalendarPanel(EventController controller) {
        this.controller        = controller;
        this.currentMonth      = YearMonth.now();
        this.currentDay        = LocalDate.now();
        this.currentWeekStart  = LocalDate.now().with(
                java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        this.selectedDate      = LocalDate.now();
        this.viewMode          = ViewMode.MONTHLY;
        setLayout(new BorderLayout());
        buildPanel();
    }

    /**
     * Returns the display colour for a given event category.
     *
     * <p>Maps predefined event categories to their corresponding {@link Color} values:
     * <ul>
     *   <li>Meeting: Red (RGB: 200, 50, 46)</li>
     *   <li>Birthday: Yellow (RGB: 234, 179, 8)</li>
     *   <li>Appointment: Green (RGB: 34, 197, 94)</li>
     *   <li>Reminder: Orange (RGB: 249, 115, 22)</li>
     *   <li>Other: Purple (RGB: 168, 85, 247)</li>
     * </ul>
     * </p>
     *
     * @param category The event category name (e.g. "Meeting", "Birthday")
     * @return The {@link Color} used to mark events of that category on the calendar
     */
    private Color getEventColor(String category) {
        switch (category) {
            case "Meeting":     return new Color(200, 50, 46);   // red
            case "Birthday":    return new Color(234, 179, 8);   // yellow
            case "Appointment": return new Color(34, 197, 94);   // green
            case "Reminder":    return new Color(249, 115, 22);  // orange
            default:            return new Color(168, 85, 247);  // purple (other)
        }
    }

    /**
     * Sets the callback function invoked when a date cell is clicked.
     *
     * <p>This callback is typically set by MainFrame to receive notifications whenever the user
     * selects a date. The callback receives the selected {@link LocalDate} as its parameter.</p>
     *
     * @param callback A {@link Consumer} that accepts a {@link LocalDate} when a date is clicked
     */
    public void setOnDateClickCallback(Consumer<LocalDate> callback) {
        this.onDateClickCallback = callback;
    }

    /**
     * Refreshes the entire calendar panel.
     *
     * <p>Removes all components, rebuilds the calendar grid for the active view mode, and repaints
     * the panel. Call this method after:
     * <ul>
     *   <li>Changing the displayed period or view mode</li>
     *   <li>Adding or removing events</li>
     *   <li>Selecting a different date</li>
     * </ul>
     */
    public void refresh() {
        removeAll();
        buildPanel();
        revalidate();
        repaint();
    }

    /**
     * Navigates the calendar back to the current day and resets the view to MONTHLY.
     *
     * <p>Sets the displayed month, week anchor and day to today and the selected date to today,
     * then refreshes the panel to reflect these changes.</p>
     */
    public void goToToday() {
        currentMonth     = YearMonth.now();
        currentDay       = LocalDate.now();
        currentWeekStart = LocalDate.now().with(
                java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        selectedDate     = LocalDate.now();
        refresh();
    }

    /**
     * Builds the complete calendar panel structure for the active view mode.
     *
     * <p>Delegates grid construction to the appropriate private helper method based on the current
     * {@link ViewMode}:
     * <ul>
     *   <li>{@link #createDailyGrid()} for {@link ViewMode#DAILY}</li>
     *   <li>{@link #createWeeklyGrid()} for {@link ViewMode#WEEKLY}</li>
     *   <li>{@link #createCalendarGrid()} for {@link ViewMode#MONTHLY}</li>
     * </ul>
     * </p>
     */
    private void buildPanel() {
        add(createNavigationBar(), BorderLayout.NORTH);

        switch (viewMode) {
            case DAILY   -> add(createDailyGrid(),   BorderLayout.CENTER);
            case WEEKLY  -> add(createWeeklyGrid(),  BorderLayout.CENTER);
            case MONTHLY -> add(createCalendarGrid(), BorderLayout.CENTER);
        }
    }

    // =========================================================================
    // NAVIGATION BAR
    // =========================================================================

    /**
     * Creates the navigation bar at the top of the calendar.
     *
     * <p>The navigation bar contains:
     * <ul>
     *   <li>Left arrow button to go to the previous period (day / week / month)</li>
     *   <li>Period title label in the centre</li>
     *   <li>Right arrow button to go to the next period</li>
     *   <li>Three toggle buttons on the right to switch between view modes</li>
     * </ul>
     * The bar has a blue background with white text for visual consistency.</p>
     *
     * @return A {@link JPanel} containing the navigation controls
     */
    private JPanel createNavigationBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(new Color(37, 99, 235));

        // --- Left / Right navigation buttons ---
        JButton previousButton = new JButton("◀");
        JButton nextButton     = new JButton("▶");

        previousButton.addActionListener(e -> navigatePrevious());
        nextButton.addActionListener(e -> navigateNext());

        // --- Centre title label ---
        JLabel title = new JLabel(buildTitleText(), SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        // --- View mode toggle buttons on the right ---
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        modePanel.setOpaque(false);

        JButton dayBtn    = createModeButton("Day",   ViewMode.DAILY);
        JButton weekBtn   = createModeButton("Week",  ViewMode.WEEKLY);
        JButton monthBtn  = createModeButton("Month", ViewMode.MONTHLY);

        // Highlight the button that matches the current view mode
        highlightActiveMode(dayBtn,   ViewMode.DAILY);
        highlightActiveMode(weekBtn,  ViewMode.WEEKLY);
        highlightActiveMode(monthBtn, ViewMode.MONTHLY);

        modePanel.add(dayBtn);
        modePanel.add(weekBtn);
        modePanel.add(monthBtn);

        nav.add(previousButton, BorderLayout.WEST);
        nav.add(title,          BorderLayout.CENTER);
        nav.add(nextButton,     BorderLayout.EAST);

        // Wrap previous + title + next in a centre panel, and mode buttons on the far right
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(nextButton,  BorderLayout.WEST);
        right.add(modePanel,   BorderLayout.EAST);

        nav.add(previousButton, BorderLayout.WEST);
        nav.add(title,          BorderLayout.CENTER);
        nav.add(right,          BorderLayout.EAST);

        return nav;
    }

    /**
     * Builds the title text shown in the navigation bar depending on the view mode.
     *
     * <p>Title format varies by view mode:
     * <ul>
     *   <li>{@link ViewMode#DAILY}: "15/06/2026"</li>
     *   <li>{@link ViewMode#WEEKLY}: "15/06/2026 – 21/06/2026"</li>
     *   <li>{@link ViewMode#MONTHLY}: "June 2026"</li>
     * </ul>
     * </p>
     *
     * @return The formatted title string
     */
    private String buildTitleText() {
        switch (viewMode) {
            case DAILY:
                return currentDay.format(LABEL_FORMAT);

            case WEEKLY:
                LocalDate weekEnd = currentWeekStart.plusDays(6);
                return currentWeekStart.format(LABEL_FORMAT)
                        + " – " + weekEnd.format(LABEL_FORMAT);

            case MONTHLY:
            default:
                String monthName = currentMonth.getMonth()
                        .getDisplayName(TextStyle.FULL, Locale.of("en", "US"));
                return monthName.substring(0, 1).toUpperCase()
                        + monthName.substring(1) + " " + currentMonth.getYear();
        }
    }

    /**
     * Creates a small toggle button that switches the calendar to the given view mode.
     *
     * <p>When clicked, this button updates the active {@link ViewMode}, adjusts the display period
     * accordingly, and refreshes the calendar panel. The button styling includes hover effects and
     * hand cursor indication.</p>
     *
     * @param label The button label (e.g. "Day", "Week", "Month")
     * @param mode  The {@link ViewMode} this button activates
     * @return A configured {@link JButton} with appropriate event listeners
     */
    private JButton createModeButton(String label, ViewMode mode) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            viewMode = mode;
            // When switching to weekly, anchor the week to the selected date
            if (mode == ViewMode.WEEKLY) {
                currentWeekStart = selectedDate.with(
                        java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            }
            // When switching to daily, show the selected date
            if (mode == ViewMode.DAILY) {
                currentDay = selectedDate;
            }
            if (mode == ViewMode.MONTHLY) {
                currentMonth = YearMonth.from(selectedDate);
            }
            refresh();
        });
        return btn;
    }

    /**
     * Visually highlights a mode button when its mode matches the active view mode.
     *
     * <p>Active buttons receive a darker background colour to indicate the current selection, with
     * white text for improved contrast.</p>
     *
     * @param btn  The {@link JButton} to potentially highlight
     * @param mode The {@link ViewMode} this button represents
     */
    private void highlightActiveMode(JButton btn, ViewMode mode) {
        if (viewMode == mode) {
            btn.setBackground(new Color(29, 78, 216)); // darker blue
            btn.setForeground(Color.WHITE);
        }
    }

    /**
     * Navigates to the previous period based on the active view mode.
     *
     * <p>The navigation behaviour depends on the current {@link ViewMode}:
     * <ul>
     *   <li>{@link ViewMode#DAILY}: moves back one day</li>
     *   <li>{@link ViewMode#WEEKLY}: moves back one week</li>
     *   <li>{@link ViewMode#MONTHLY}: moves back one month</li>
     * </ul>
     * Updates the selected date and triggers the date click callback, then refreshes the panel.</p>
     */
    private void navigatePrevious() {
      switch (viewMode) {
          case DAILY -> {
              currentDay   = currentDay.minusDays(1);
              selectedDate = currentDay;
              if (onDateClickCallback != null) {
                  onDateClickCallback.accept(selectedDate);
              }
          }
          case WEEKLY  -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            selectedDate = currentWeekStart;
            if (onDateClickCallback != null) {
                  onDateClickCallback.accept(selectedDate);
            } 
          }

          case MONTHLY -> {
            currentMonth = currentMonth.minusMonths(1);
            selectedDate = currentMonth.atDay(1);
            if (onDateClickCallback != null) {
                  onDateClickCallback.accept(selectedDate);
            }
          }
      }
      refresh();
  }

    /**
     * Navigates to the next period based on the active view mode.
     *
     * <p>The navigation behaviour depends on the current {@link ViewMode}:
     * <ul>
     *   <li>{@link ViewMode#DAILY}: moves forward one day</li>
     *   <li>{@link ViewMode#WEEKLY}: moves forward one week</li>
     *   <li>{@link ViewMode#MONTHLY}: moves forward one month</li>
     * </ul>
     * Updates the selected date and triggers the date click callback, then refreshes the panel.</p>
     */
    private void navigateNext() {
      switch (viewMode) {
          case DAILY -> {
              currentDay   = currentDay.plusDays(1);
              selectedDate = currentDay;
              if (onDateClickCallback != null) {
                  onDateClickCallback.accept(selectedDate);
              }
          }
          case WEEKLY  -> {
              currentWeekStart = currentWeekStart.plusWeeks(1);
              selectedDate = currentWeekStart; 
              if (onDateClickCallback != null) {
                  onDateClickCallback.accept(selectedDate);
            }
          }
          case MONTHLY -> {
            currentMonth = currentMonth.plusMonths(1);
            selectedDate = currentMonth.atDay(1);
            if (onDateClickCallback != null) {
                  onDateClickCallback.accept(selectedDate);
            }
          }
      }
      refresh();
  }

    // =========================================================================
    // DAILY VIEW
    // =========================================================================

    /**
     * Creates the daily view grid showing events for the {@code currentDay}.
     *
     * <p>The layout consists of 24 rows, one per hour of the day (00:00 – 23:00). Each row shows:
     * <ul>
     *   <li>A hour label on the left (e.g. "08:00")</li>
     *   <li>A coloured event label for each event that starts in that hour, or an empty separator
     *       line if no events exist in that hour</li>
     * </ul>
     * The panel includes a header showing the day name and date. Clicking the header selects that
     * date and notifies the parent via callback.</p>
     *
     * @return A {@link JPanel} containing the scrollable daily timeline view
     */
    private JPanel createDailyGrid() {
        JPanel container = new JPanel(new BorderLayout());

        // Header showing the day name and date
        JPanel header = new JPanel();
        header.setBackground(new Color(219, 234, 254)); // light blue
        String dayName = currentDay.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.of("en", "US"));
        JLabel headerLabel = new JLabel(
                dayName.substring(0, 1).toUpperCase() + dayName.substring(1)
                        + " – " + currentDay.format(LABEL_FORMAT),
                SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.add(headerLabel);
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedDate = currentDay;
                refresh();
                if (onDateClickCallback != null) {
                    onDateClickCallback.accept(currentDay);
                }
            }
        });
        container.add(header, BorderLayout.NORTH);

        // Retrieve all events for the current day
        List<Event> dayEvents = new ArrayList<>(controller.getEventsForDate(currentDay));

        // Build the hour-by-hour timeline panel
        JPanel timelinePanel = new JPanel(new GridLayout(24, 1, 0, 1));
        timelinePanel.setBackground(Color.WHITE);

        for (int hour = 0; hour < 24; hour++) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(hour % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

            // Hour label on the left
            JLabel hourLabel = new JLabel(
                    String.format("  %02d:00", hour), SwingConstants.LEFT);
            hourLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            hourLabel.setForeground(new Color(100, 116, 139));
            hourLabel.setPreferredSize(new Dimension(55, 28));
            row.add(hourLabel, BorderLayout.WEST);

            // Events panel: one coloured label per event in this hour slot
            JPanel eventsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
            eventsRow.setOpaque(false);
            final int h = hour;
            dayEvents.stream()
                    .filter(ev -> ev.getDateTime().getHour() == h)
                    .forEach(ev -> {
                        JLabel evLabel = new JLabel(
                                " " + ev.getDateTime().format(
                                        DateTimeFormatter.ofPattern("HH:mm"))
                                        + " " + ev.getTitle() + " ");
                        evLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                        evLabel.setForeground(Color.WHITE);
                        evLabel.setBackground(getEventColor(ev.getCategory()));
                        evLabel.setOpaque(true);
                        evLabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
                        eventsRow.add(evLabel);
                    });

            row.add(eventsRow, BorderLayout.CENTER);
            timelinePanel.add(row);
        }

        // Wrap the timeline in a scroll pane so it does not overflow
        JScrollPane scroll = new JScrollPane(timelinePanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        container.add(scroll, BorderLayout.CENTER);

        return container;
    }

    // =========================================================================
    // WEEKLY VIEW
    // =========================================================================

    /**
     * Creates the weekly view grid showing all seven days of the selected week.
     *
     * <p>The layout consists of:
     * <ul>
     *   <li>Seven columns, one per day of the week (Sunday to Saturday)</li>
     *   <li>A header for each column with the day abbreviation and date number</li>
     *   <li>An event area below each day header listing events for that day</li>
     * </ul>
     * Clicking a day header selects that date and notifies the parent via callback. Days matching
     * today are highlighted in blue; the selected date is highlighted in light blue.</p>
     *
     * @return A {@link JPanel} containing the weekly grid view
     */
    private JPanel createWeeklyGrid() {
        JPanel container = new JPanel(new BorderLayout());

        // Build one column per day of the week (7 columns)
        JPanel weekGrid = new JPanel(new GridLayout(1, 7, 2, 0));

        for (int i = 0; i < 7; i++) {
            LocalDate day = currentWeekStart.plusDays(i);
            weekGrid.add(createWeekDayColumn(day));
        }

        container.add(weekGrid, BorderLayout.CENTER);
        return container;
    }

    /**
     * Creates a single day column for the weekly view.
     *
     * <p>Each column contains:
     * <ul>
     *   <li>A header with the abbreviated day name and date number</li>
     *   <li>A scrollable list of event labels for that day</li>
     * </ul>
     * The header background reflects the day's state:
     * <ul>
     *   <li>Blue for today</li>
     *   <li>Light blue for the selected date</li>
     *   <li>Light grey for all other days</li>
     * </ul>
     * The header is clickable to select the represented day.</p>
     *
     * @param day The {@link LocalDate} to render as a column
     * @return A {@link JPanel} representing one day column
     */
    private JPanel createWeekDayColumn(LocalDate day) {
        JPanel column = new JPanel(new BorderLayout());
        column.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
                new Color(226, 232, 240)));

        boolean isToday    = day.equals(LocalDate.now());
        boolean isSelected = day.equals(selectedDate);

        // --- Day header ---
        JPanel header = new JPanel(new GridLayout(2, 1));
        if (isToday) {
            header.setBackground(new Color(37, 99, 235));   // blue
        } else if (isSelected) {
            header.setBackground(new Color(191, 219, 254)); // light blue
        } else {
            header.setBackground(new Color(241, 245, 249)); // light grey
        }

        // Abbreviated day name (e.g. "Sun", "Mon")
        String abbr = day.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.of("en", "US"));
        JLabel nameLabel = new JLabel(
                abbr.substring(0, 1).toUpperCase() + abbr.substring(1),
                SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nameLabel.setForeground(isToday ? Color.WHITE : new Color(71, 85, 105));

        // Day number
        JLabel dayNumLabel = new JLabel(
                String.valueOf(day.getDayOfMonth()), SwingConstants.CENTER);
        dayNumLabel.setFont(new Font("Segoe UI",
                isToday ? Font.BOLD : Font.PLAIN, 14));
        dayNumLabel.setForeground(isToday ? Color.WHITE : Color.BLACK);

        header.add(nameLabel);
        header.add(dayNumLabel);

        // Make the header clickable to select this day
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedDate = day;
                refresh();
                if (onDateClickCallback != null) {
                    onDateClickCallback.accept(day);
                }
            }
        });
        column.add(header, BorderLayout.NORTH);

        // --- Events list ---
        List<Event> dayEvents = new ArrayList<>(controller.getEventsForDate(day));
        JPanel eventsPanel = new JPanel();
        eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
        eventsPanel.setBackground(Color.WHITE);

        for (Event ev : dayEvents) {
            JLabel evLabel = new JLabel(
                    " " + ev.getDateTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                            + " " + ev.getTitle());
            evLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            evLabel.setForeground(Color.WHITE);
            evLabel.setBackground(getEventColor(ev.getCategory()));
            evLabel.setOpaque(true);
            evLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            evLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            eventsPanel.add(evLabel);
            eventsPanel.add(Box.createVerticalStrut(2));
        }

        // Wrap events list in a scroll pane
        JScrollPane scroll = new JScrollPane(eventsPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        column.add(scroll, BorderLayout.CENTER);

        return column;
    }

    // =========================================================================
    // MONTHLY VIEW
    // =========================================================================

    /**
     * Creates the monthly calendar grid displaying all dates of the current month.
     *
     * <p>The grid includes:
     * <ul>
     *   <li>A header row with day-of-week abbreviations (Sun, Mon, Tue, etc.)</li>
     *   <li>Day cells for each date in the month, properly aligned with the day of week</li>
     *   <li>Empty cells to align the first day of the month to the correct column</li>
     * </ul>
     * </p>
     *
     * @return A {@link JPanel} containing the complete monthly calendar grid
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

        // Calculate which column the first day of the month falls into.
        // getDayOfWeek().getValue() returns MON=1 … SUN=7; convert to SUN=0 … SAT=6.
        LocalDate firstDayOfMonth  = currentMonth.atDay(1);
        int columnOfFirstDay = firstDayOfMonth.getDayOfWeek().getValue() % 7;

        // Fill empty cells before the first day of the month for correct alignment
        for (int i = 0; i < columnOfFirstDay; i++) {
            grid.add(new JPanel()); // empty alignment cell
        }

        // Create a date cell for each day of the month
        for (int dayOfMonth = 1; dayOfMonth <= currentMonth.lengthOfMonth(); dayOfMonth++) {
            LocalDate date = currentMonth.atDay(dayOfMonth);
            grid.add(createDateCell(date, datesWithEvents));
        }

        container.add(headerRow, BorderLayout.NORTH);
        container.add(grid,      BorderLayout.CENTER);
        return container;
    }

    /**
     * Creates a single date cell for the monthly calendar grid.
     *
     * <p>Each cell is a clickable panel that:
     * <ul>
     *   <li>Displays the day-of-month number</li>
     *   <li>Shows a blue background if it represents today</li>
     *   <li>Shows a light blue background if it is the selected date</li>
     *   <li>Displays coloured dots (one per event category) if the date has events</li>
     *   <li>Invokes the date click callback when the user clicks on it</li>
     * </ul>
     * </p>
     *
     * @param date            The {@link LocalDate} to create a cell for
     * @param datesWithEvents A {@link List} of {@link LocalDate} objects that have at least one
     *                        scheduled event
     * @return A {@link JPanel} representing a single calendar date cell
     */
    private JPanel createDateCell(LocalDate date, List<LocalDate> datesWithEvents) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        boolean isToday    = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);
        boolean hasEvents  = datesWithEvents.contains(date);

        // Day number label
        JLabel dayNumber = new JLabel(
                String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
        dayNumber.setFont(new Font("Segoe UI",
                isToday ? Font.BOLD : Font.PLAIN, 13));

        // Background colour based on cell state
        if (isToday) {
            // Today: blue background with white text
            cell.setBackground(new Color(37, 99, 235));
            dayNumber.setForeground(Color.WHITE);
        } else if (isSelected) {
            // Selected date: light blue background
            cell.setBackground(new Color(191, 219, 254));
        } else {
            // Default: white background
            cell.setBackground(Color.WHITE);
        }

        cell.add(dayNumber, BorderLayout.CENTER);

        // Coloured dots indicator if this date has events
        if (hasEvents) {
            JPanel eventIndicator = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Draw one small coloured circle per event, spaced horizontally
                    List<Event> evs = controller.getEventsForDate(date);
                    int x = 2;
                    for (Event ev : evs) {
                        g.setColor(getEventColor(ev.getCategory()));
                        g.fillOval(x, getHeight() - 6, 5, 5);
                        x += 7; // horizontal gap between dots
                    }
                }
            };
            eventIndicator.setOpaque(false);
            eventIndicator.setPreferredSize(new Dimension(10, 10));
            cell.add(eventIndicator, BorderLayout.SOUTH);
        }

        // Make the cell clickable
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cell.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedDate = date;
                refresh();
                // Notify parent component of the new selection via callback
                if (onDateClickCallback != null) {
                    onDateClickCallback.accept(date);
                }
            }
        });

        return cell;
    }
}
