package harvey.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import harvey.HarveyException;

/**
 * Tests {@link TaskList}.
 * <p>
 * The interesting behaviour here is the numbering. The user counts tasks from 1 and the
 * underlying list counts from 0, so the boundaries either side of the valid range are
 * where an off-by-one error would show up. Those cases get the most attention below.
 */
public class TaskListTest {
    /** Builds a list of three todos named a, b and c, for tests that need existing tasks. */
    private TaskList threeTasks() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("a"));
        tasks.add(new Todo("b"));
        tasks.add(new Todo("c"));
        return tasks;
    }

    @Test
    public void newTaskList_noArguments_isEmpty() {
        TaskList tasks = new TaskList();
        assertTrue(tasks.isEmpty());
        assertEquals(0, tasks.size());
    }

    @Test
    public void add_oneTask_sizeGrowsAndListNoLongerEmpty() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));
        assertFalse(tasks.isEmpty());
        assertEquals(1, tasks.size());
    }

    @Test
    public void get_firstTaskNumber_returnsFirstTask() throws HarveyException {
        TaskList tasks = threeTasks();
        // Task 1 must be the first task added, not the second: this is the off-by-one
        // that the conversion from the user's numbering exists to prevent.
        assertEquals("[T][ ] a", tasks.get(1).toString());
    }

    @Test
    public void get_lastTaskNumber_returnsLastTask() throws HarveyException {
        TaskList tasks = threeTasks();
        assertEquals("[T][ ] c", tasks.get(3).toString());
    }

    @Test
    public void get_taskNumberJustPastEnd_exceptionThrown() {
        TaskList tasks = threeTasks();
        // 3 is valid and 4 is not, so this is the boundary that matters.
        HarveyException e = assertThrows(HarveyException.class, () -> tasks.get(4));
        assertEquals("There is no task 4. You have 3 task(s), so pick a number from 1 to 3.",
                e.getMessage());
    }

    @Test
    public void get_zero_exceptionThrown() {
        // 0 is a valid index into the underlying list but not a valid task number,
        // so forgetting the lower bound would let this through silently.
        TaskList tasks = threeTasks();
        assertThrows(HarveyException.class, () -> tasks.get(0));
    }

    @Test
    public void get_negativeTaskNumber_exceptionThrown() {
        TaskList tasks = threeTasks();
        assertThrows(HarveyException.class, () -> tasks.get(-1));
    }

    @Test
    public void get_anyTaskNumberOnEmptyList_exceptionThrown() {
        TaskList tasks = new TaskList();
        assertThrows(HarveyException.class, () -> tasks.get(1));
    }

    @Test
    public void get_validTaskNumber_returnsTheStoredObjectNotACopy() throws HarveyException {
        // Harvey marks a task by calling markAsDone() on what get() returned, so the
        // change is only visible in the list if get() hands back the stored object.
        TaskList tasks = new TaskList();
        Todo stored = new Todo("read book");
        tasks.add(stored);
        assertSame(stored, tasks.get(1));
    }

    @Test
    public void delete_middleTaskNumber_removesItAndClosesTheGap() throws HarveyException {
        TaskList tasks = threeTasks();
        Task removed = tasks.delete(2);

        assertEquals("[T][ ] b", removed.toString());
        assertEquals(2, tasks.size());
        // c must move up to task 2; a gap would break every later task number.
        assertEquals("[T][ ] a", tasks.get(1).toString());
        assertEquals("[T][ ] c", tasks.get(2).toString());
    }

    @Test
    public void delete_lastRemainingTask_listBecomesEmpty() throws HarveyException {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("only"));
        tasks.delete(1);
        assertTrue(tasks.isEmpty());
    }

    @Test
    public void delete_taskNumberJustPastEnd_exceptionThrownAndListUnchanged() {
        TaskList tasks = threeTasks();
        assertThrows(HarveyException.class, () -> tasks.delete(4));
        assertEquals(3, tasks.size());
    }

    @Test
    public void asList_afterAdds_containsTasksInInsertionOrder() {
        TaskList tasks = threeTasks();
        assertEquals(3, tasks.asList().size());
        assertEquals("[T][ ] a", tasks.asList().get(0).toString());
        assertEquals("[T][ ] c", tasks.asList().get(2).toString());
    }
}
