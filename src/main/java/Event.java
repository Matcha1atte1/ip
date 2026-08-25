/**
 * A task that runs from one point in time to another,
 * e.g. {@code project meeting (from: Mon 2pm to: 4pm)}.
 * Both times are kept as plain text, since dates are not parsed at this stage.
 */
public class Event extends Task {
    /** When the event starts, exactly as the user typed it. */
    protected String from;

    /** When the event ends, exactly as the user typed it. */
    protected String to;

    /**
     * Creates an event that is not done yet.
     *
     * @param description the text describing the task
     * @param from        when the event starts
     * @param to          when the event ends
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * {@inheritDoc}
     * Wraps the inherited form with the {@code [E]} marker and both times,
     * giving e.g. {@code [E][ ] project meeting (from: Mon 2pm to: 4pm)}.
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }

    /**
     * {@inheritDoc}
     * Prefixes the {@code E} type letter and appends both times,
     * giving e.g. {@code E | 0 | project meeting | Mon 2pm | 4pm}.
     */
    @Override
    public String toFileFormat() {
        return "E | " + super.toFileFormat() + " | " + from + " | " + to;
    }
}
