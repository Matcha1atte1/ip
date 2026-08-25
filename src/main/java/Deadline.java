/**
 * A task that must be finished before a given point in time,
 * e.g. {@code return book (by: Sunday)}.
 * The due date is kept as plain text, since dates are not parsed at this stage.
 */
public class Deadline extends Task {
    /** When the task is due, exactly as the user typed it. */
    protected String by;

    /**
     * Creates a deadline that is not done yet.
     *
     * @param description the text describing the task
     * @param by          when the task is due
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * {@inheritDoc}
     * Wraps the inherited form with the {@code [D]} marker and the due date,
     * giving e.g. {@code [D][ ] return book (by: Sunday)}.
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }

    /**
     * {@inheritDoc}
     * Prefixes the {@code D} type letter and appends the due date,
     * giving e.g. {@code D | 0 | return book | Sunday}.
     */
    @Override
    public String toFileFormat() {
        return "D | " + super.toFileFormat() + " | " + by;
    }
}
