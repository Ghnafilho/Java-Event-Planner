# Java Event Planner

A desktop application for event and calendar management, developed in Java using Swing.
Academic project for the **Object-Oriented Programming** course.

---

## Features

* Monthly calendar view with highlighted days containing events
* Create, edit, and delete events
* Support for recurring events (daily, weekly, and monthly)
* When editing or deleting a recurring event, choose between modifying only that occurrence or all future occurrences
* Automatic reminder pop-ups while the application is running
* Add attendees (name and email) to each event
* Search events by keyword
* Export all events from a specific day to a `.txt` file
* Automatic local file persistence — data is loaded when the application starts
* Error handling with clear messages, without exposing stack traces to the user

---

## Project Structure

```text
.
├── run.bat                             # Windows compile/run helper script
├── planner/
│   ├── Main.java                       # Application entry point
│   ├── controller/
│   │   ├── EventController.java        # Business logic (CRUD, search, export)
│   │   └── ReminderThread.java         # Background reminder thread
│   ├── model/
│   │   ├── Event.java                  # Base event model
│   │   ├── RecurringEvent.java         # Recurring event (inherits from Event)
│   │   ├── Attendee.java               # Event attendee
│   │   └── DataStorage.java            # File reading and writing
│   ├── view/
│   │   ├── MainFrame.java              # Main application window
│   │   └── CalendarPanel.java          # Calendar visual grid
│   └── exception/
│       └── InvalidEventException.java  # Custom validation exception

```

---

## Object-Oriented Programming Concepts Applied

| Concept              | Where it is Applied                                    |
| -------------------- | ------------------------------------------------------ |
| **Inheritance**      | `RecurringEvent` extends `Event`                       |
| **Polymorphism**     | `toCSV()` overridden in `RecurringEvent`               |
| **Encapsulation**    | Private attributes with getters/setters in all classes |
| **Composition**      | `Event` contains a list of `Attendee` objects          |
| **Custom Exception** | `InvalidEventException` for input validation           |
| **Threading**        | `ReminderThread` runs as a daemon thread in parallel   |
| **Enum**             | `RecurrenceType` for recurrence types                  |

---

## How to Compile and Run

### Prerequisite

* Java 17 or later installed

### Windows

The project includes a helper script that automatically compiles and runs the application.

Run:

```cmd
run.bat
```

Or simply double-click the file in Windows Explorer.

The script automatically:

1. Removes previous compiled files
2. Compiles all source files into the `out` directory
3. Launches the application

### Linux/macOS

```bash
# Create output directory
mkdir out

# Compile
javac -d out $(find planner -name "*.java")

# Run
java -cp out planner.Main
```
---

## Data Persistence

Events are automatically saved to the `events_data.txt` file located in the project's root directory.

The file is loaded when the application starts. If it is missing or corrupted, the application will start normally with an empty event list.

---

## Data File Format

Each line represents an event using the following format:

```text
title;dateTime;location;description;category;reminderMinutes;attendees
```

Recurring events include an additional suffix:

```text
...;[REC:DAILY]
```
