package harvey.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import harvey.task.TaskList;
import harvey.task.Todo;

/**
 * Tests the joining of reply lines.
 * <p>
 * {@link Ui#formatLines(String...)} is worth testing despite its size, because every
 * reply in the program is built through it and because varargs has edge cases a plain
 * parameter does not: no arguments at all, and a single argument that must not gain a
 * trailing separator.
 */
public class UiTest {
    /** The line separator the replies are joined with. */
    private static final String NEW_LINE = System.lineSeparator();

    private final Ui ui = new Ui();

    @Test
    public void formatLines_severalLines_joinedBySeparator() {
        assertEquals("one" + NEW_LINE + "two" + NEW_LINE + "three",
                ui.formatLines("one", "two", "three"));
    }

    @Test
    public void formatLines_singleLine_noTrailingSeparator() {
        assertEquals("only", ui.formatLines("only"));
    }

    @Test
    public void formatLines_noLines_returnsEmptyString() {
        assertEquals("", ui.formatLines());
    }

    @Test
    public void formatTaskList_twoTasks_headingThenNumberedTasks() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));
        tasks.add(new Todo("buy milk"));

        assertEquals("Here are the tasks in your list:" + NEW_LINE
                + "1.[T][ ] read book" + NEW_LINE
                + "2.[T][ ] buy milk", ui.formatTaskList(tasks));
    }
}
