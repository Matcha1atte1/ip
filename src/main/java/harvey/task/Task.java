package harvey.task;
/**
 * A single task in Harvey's list.
 * Bundles a task's description together with whether it has been completed,
 * replacing the two parallel arrays used in the previous version.
 */
public class Task {
    /** What the user typed to describe this task, e.g. {@code read book}. */
    protected String description;

    /** Whether the user has marked this task as done. */
    protected boolean isDone;

    /**
     * Creates a task that is not done yet.
     *
     * @param description the text describing the task
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the single character shown inside the status box.
     *
     * @return {@code "X"} if the task is done, otherwise a space
     */
    public String getStatusIcon() {
        return (isDone ? "X" : " "); // mark done task with X
    }

    /** Records that this task has been completed. */
    public void markAsDone() {
        this.isDone = true;
    }

    /** Records that this task is not completed after all. */
    public void markAsNotDone() {
        this.isDone = false;
    }

    /**
     * Returns the task as it should be shown to the user, e.g. {@code [X] read book}.
     * Overriding {@code toString} means the task can be printed directly, so the
     * display format lives in one place.
     *
     * @return the status box followed by the description
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }

    /**
     * Returns the task as one line of the save file, e.g. {@code 1 | read book}.
     * <p>
     * This is deliberately separate from {@link #toString()}: the display form is meant for
     * a person to read, while this form is meant to be read back by the program, so the two
     * are free to change independently. Each subclass adds its own type letter in front and
     * any extra fields at the end.
     *
     * @return the done flag and description, separated by {@code |}
     */
    public String toFileFormat() {
        // 1 and 0 are used rather than the "X" and " " of the display form, because a
        // space is easy to lose when the line is split back up during loading.
        return (isDone ? "1" : "0") + " | " + description;
    }
}
