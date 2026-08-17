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
}
