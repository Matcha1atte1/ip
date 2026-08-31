package harvey.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import harvey.HarveyException;
import harvey.storage.Storage;
import harvey.task.TaskList;
import harvey.ui.Ui;

/**
 * Tests what the commands say back.
 * <p>
 * These tests became worth writing when {@code execute} started returning its reply
 * instead of printing it. Previously the only way to check a reply was to capture
 * {@code System.out}; now the reply is simply the return value, so what the user is told
 * can be asserted directly.
 * <p>
 * Every command saves, so each test writes into its own {@code @TempDir} rather than
 * touching the real {@code ./data/harvey.txt}.
 */
public class CommandTest {
    /** The line separator the commands use when building multi-line replies. */
    private static final String NEW_LINE = System.lineSeparator();

    /** An empty folder created fresh for each test and removed afterwards. */
    @TempDir
    private Path tempDir;

    private final Ui ui = new Ui();

    /** Returns a Storage writing into this test's temporary folder. */
    private Storage storage() {
        return new Storage(tempDir.toString(), "harvey.txt");
    }

    /** Returns a list holding one todo, added through the command being tested. */
    private TaskList listWithOneTodo() throws HarveyException {
        TaskList tasks = new TaskList();
        new AddCommand(CommandType.TODO, "read book").execute(tasks, ui, storage());
        return tasks;
    }

    @Test
    public void execute_addTodo_replyNamesTaskAndNewCount() throws HarveyException {
        TaskList tasks = new TaskList();
        String reply = new AddCommand(CommandType.TODO, "read book").execute(tasks, ui, storage());

        assertEquals("Got it. I've added this task:" + NEW_LINE
                + "  [T][ ] read book" + NEW_LINE
                + "Now you have 1 tasks in the list.", reply);
        assertEquals(1, tasks.size());
    }

    @Test
    public void execute_addTodoWithReservedCharacter_exceptionThrown() {
        TaskList tasks = new TaskList();
        AddCommand command = new AddCommand(CommandType.TODO, "read | book");

        HarveyException e = assertThrows(HarveyException.class, () ->
                command.execute(tasks, ui, storage()));
        assertTrue(e.getMessage().contains("separate fields"));
        assertTrue(tasks.isEmpty());
    }

    @Test
    public void execute_markFirstTask_replyShowsTaskTicked() throws HarveyException {
        TaskList tasks = listWithOneTodo();
        String reply = new MarkCommand("1").execute(tasks, ui, storage());

        assertEquals("Nice! I've marked this task as done:" + NEW_LINE
                + "  [T][X] read book", reply);
    }

    @Test
    public void execute_unmarkFirstTask_replyShowsTaskUnticked() throws HarveyException {
        TaskList tasks = listWithOneTodo();
        new MarkCommand("1").execute(tasks, ui, storage());
        String reply = new UnmarkCommand("1").execute(tasks, ui, storage());

        assertEquals("OK, I've marked this task as not done yet:" + NEW_LINE
                + "  [T][ ] read book", reply);
    }

    @Test
    public void execute_markTaskNumberJustPastEnd_exceptionThrown() throws HarveyException {
        TaskList tasks = listWithOneTodo();
        MarkCommand command = new MarkCommand("2");

        assertThrows(HarveyException.class, () -> command.execute(tasks, ui, storage()));
    }

    @Test
    public void execute_deleteOnlyTask_replyNamesTaskAndEmptiesList() throws HarveyException {
        TaskList tasks = listWithOneTodo();
        String reply = new DeleteCommand("1").execute(tasks, ui, storage());

        assertEquals("Noted. I've removed this task:" + NEW_LINE
                + "  [T][ ] read book" + NEW_LINE
                + "Now you have 0 tasks in the list.", reply);
        assertTrue(tasks.isEmpty());
    }

    @Test
    public void execute_listWithOneTask_replyIsNumberedFromOne() throws HarveyException {
        TaskList tasks = listWithOneTodo();
        String reply = new ListCommand().execute(tasks, ui, storage());

        assertEquals("Here are the tasks in your list:" + NEW_LINE
                + "1.[T][ ] read book", reply);
    }

    @Test
    public void execute_listOnEmptyList_exceptionThrown() {
        TaskList tasks = new TaskList();
        ListCommand command = new ListCommand();

        assertThrows(HarveyException.class, () -> command.execute(tasks, ui, storage()));
    }

    @Test
    public void execute_findMatchingKeyword_replyListsOnlyTheMatches() throws HarveyException {
        TaskList tasks = listWithOneTodo();
        new AddCommand(CommandType.TODO, "buy milk").execute(tasks, ui, storage());
        String reply = new FindCommand("milk").execute(tasks, ui, storage());

        // Numbered 1 among the matches, not 2 as it stands in the full list.
        assertEquals("Here are the matching tasks in your list:" + NEW_LINE
                + "1.[T][ ] buy milk", reply);
    }

    @Test
    public void execute_findKeywordInNoTask_exceptionThrown() throws HarveyException {
        TaskList tasks = listWithOneTodo();
        FindCommand command = new FindCommand("holiday");

        assertThrows(HarveyException.class, () -> command.execute(tasks, ui, storage()));
    }

    @Test
    public void execute_exit_repliesWithFarewellAndIsExitIsTrue() throws HarveyException {
        ExitCommand command = new ExitCommand();
        String reply = command.execute(new TaskList(), ui, storage());

        assertEquals("Bye. Hope to see you again soon!", reply);
        assertTrue(command.isExit());
    }
}
