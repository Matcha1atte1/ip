package harvey.task;

import harvey.HarveyException;

import java.util.ArrayList;
/**
 * The list of tasks Harvey is keeping, together with the operations that change it.
 * <p>
 * The point of wrapping the {@link ArrayList} rather than passing one around is where the
 * task numbers are handled. The user counts tasks from 1 and an {@code ArrayList} counts
 * from 0, so every caller used to subtract 1 by hand and check the range itself. Both of
 * those now happen here, once, which is the only place that can see the size of the list
 * anyway.
 */
public class TaskList {
    /** The tasks, in the order the user added them. Never null. */
    private final ArrayList<Task> tasks;

    /** Creates an empty list, for when there is nothing saved to start from. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a list holding tasks that were loaded from somewhere, e.g. the save file.
     *
     * @param tasks the tasks to start with
     */
    public TaskList(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to add
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Returns the task the user refers to by number.
     *
     * @param taskNumber the number shown by {@code list}, counting from 1
     * @return the task with that number
     * @throws HarveyException if there is no task with that number
     */
    public Task get(int taskNumber) throws HarveyException {
        return tasks.get(toIndex(taskNumber));
    }

    /**
     * Removes the task the user refers to by number and returns it.
     * The tasks after it shift down, so the remaining ones stay numbered 1, 2, 3, ...
     * with no gap.
     *
     * @param taskNumber the number shown by {@code list}, counting from 1
     * @return the task that was removed
     * @throws HarveyException if there is no task with that number
     */
    public Task delete(int taskNumber) throws HarveyException {
        return tasks.remove(toIndex(taskNumber));
    }

    /**
     * Returns how many tasks are stored.
     *
     * @return the number of tasks
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns whether there are no tasks at all.
     *
     * @return true if the list is empty
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Returns the tasks as a plain list, for code that needs to walk through all of them.
     * <p>
     * This hands out the actual list rather than a copy, which is the simple choice: a
     * caller could in principle modify it behind this class's back. Returning a copy, or
     * making {@code TaskList} implement {@code Iterable}, would close that hole; neither
     * is worth the extra machinery while the only callers are display and saving.
     *
     * @return the tasks in order
     */
    public ArrayList<Task> asList() {
        return tasks;
    }

    /**
     * Converts a task number as the user says it into a position in the list.
     *
     * @param taskNumber the number shown by {@code list}, counting from 1
     * @return the matching index, counting from 0
     * @throws HarveyException if the number names a task that does not exist
     */
    private int toIndex(int taskNumber) throws HarveyException {
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new HarveyException("There is no task " + taskNumber + ". You have "
                    + tasks.size() + " task(s), so pick a number from 1 to " + tasks.size() + ".");
        }
        return taskNumber - 1;
    }
}
