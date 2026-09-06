package harvey.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import harvey.HarveyException;
import harvey.task.Deadline;
import harvey.task.Event;
import harvey.task.Task;
import harvey.task.Todo;

/**
 * Tests {@link Storage} against real files in a temporary folder.
 * <p>
 * {@code @TempDir} gives each test its own empty directory, which JUnit deletes
 * afterwards. That keeps the tests independent of one another and stops them touching
 * the real {@code ./data/harvey.txt}, which belongs to whoever is running the app.
 * <p>
 * These are not unit tests in the strictest sense, since they touch the disk. They are
 * worth it: the whole point of Storage is what ends up in the file, and a fake file
 * system would be testing the fake.
 */
public class StorageTest {
    /** An empty folder created fresh for each test and removed afterwards. */
    @TempDir
    private Path tempDir;

    /** Returns a Storage writing into this test's temporary folder. */
    private Storage storage() {
        return new Storage(tempDir.toString(), "harvey.txt");
    }

    /** Writes the given lines straight to the save file, as a hand edit would. */
    private void writeFile(String... lines) throws IOException {
        Files.write(tempDir.resolve("harvey.txt"), List.of(lines));
    }

    @Test
    public void load_fileDoesNotExist_returnsEmptyListWithoutError() throws HarveyException {
        // The first run on any machine takes this path, so it must not be an error.
        assertTrue(storage().load().isEmpty());
    }

    @Test
    public void save_folderDoesNotExist_createsItAndWritesTheFile() throws Exception {
        // Nothing has created ./data yet on a fresh clone, and writing into a missing
        // folder fails, so save has to make it.
        Storage storage = new Storage(tempDir.resolve("nested").toString(), "harvey.txt");
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(new Todo("read book"));
        storage.save(tasks);

        assertEquals(List.of("T | 0 | read book"),
                Files.readAllLines(tempDir.resolve("nested").resolve("harvey.txt")));
    }

    @Test
    public void save_allThreeTaskTypes_writesOneLinePerTaskInOrder() throws Exception {
        ArrayList<Task> tasks = new ArrayList<>();
        Todo todo = new Todo("read book");
        todo.markAsDone();
        tasks.add(todo);
        tasks.add(new Deadline("return book", java.time.LocalDate.of(2019, 10, 15)));
        tasks.add(new Event("project meeting", java.time.LocalDateTime.of(2019, 10, 15, 14, 0),
                java.time.LocalDateTime.of(2019, 10, 15, 16, 0)));
        storage().save(tasks);

        assertEquals(List.of(
                "T | 1 | read book",
                "D | 0 | return book | 2019-10-15",
                "E | 0 | project meeting | 2019-10-15 1400 | 2019-10-15 1600"),
                Files.readAllLines(tempDir.resolve("harvey.txt")));
    }

    @Test
    public void saveThenLoad_allThreeTaskTypes_tasksComeBackUnchanged() throws Exception {
        // The property that matters most: whatever is saved must reload identically,
        // including the done flag and every date field.
        ArrayList<Task> original = new ArrayList<>();
        Todo todo = new Todo("read book");
        todo.markAsDone();
        original.add(todo);
        original.add(new Deadline("return book", java.time.LocalDate.of(2019, 10, 15)));
        original.add(new Event("project meeting", java.time.LocalDateTime.of(2019, 10, 15, 14, 0),
                java.time.LocalDateTime.of(2019, 10, 15, 16, 0)));

        Storage storage = storage();
        storage.save(original);
        ArrayList<Task> reloaded = storage.load();

        assertEquals(original.size(), reloaded.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i).toString(), reloaded.get(i).toString());
        }
        assertEquals(0, storage.getSkippedLines());
    }

    @Test
    public void save_emptyList_clearsTheFile() throws Exception {
        Storage storage = storage();
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(new Todo("read book"));
        storage.save(tasks);
        // Deleting the last task must leave an empty file, not the old contents.
        storage.save(new ArrayList<>());

        assertTrue(Files.readAllLines(tempDir.resolve("harvey.txt")).isEmpty());
        assertTrue(storage.load().isEmpty());
    }

    @Test
    public void load_blankLines_ignoredWithoutCountingAsDamage() throws Exception {
        // A trailing newline is normal, not a sign the file was edited badly.
        writeFile("T | 0 | read book", "", "   ");
        Storage storage = storage();

        assertEquals(1, storage.load().size());
        assertEquals(0, storage.getSkippedLines());
    }

    @Test
    public void load_lineWithTooFewFields_skippedAndCounted() throws Exception {
        writeFile("T | 0 | keep me", "D | 0 | missing its date");
        Storage storage = storage();

        assertEquals(1, storage.load().size());
        assertEquals(1, storage.getSkippedLines());
    }

    @Test
    public void load_unknownTypeLetter_skippedRatherThanTreatedAsTodo() throws Exception {
        // Guessing here would turn damaged data into a task that looks legitimate,
        // which is worse than losing the line.
        writeFile("X | 0 | bogus type");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(1, storage.getSkippedLines());
    }

    @Test
    public void load_emptyDescription_skipped() throws Exception {
        // The description is checked before any task is built, so no half-formed Todo
        // is created and thrown away.
        writeFile("T | 0 | ");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(1, storage.getSkippedLines());
    }

    @Test
    public void load_deadlineWithTooManyFields_skipped() throws Exception {
        // Each task type has its own expected field count, so a deadline carrying an
        // extra field is damaged even though it has more than the shared three.
        writeFile("D | 0 | return book | 2019-10-15 | extra");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(1, storage.getSkippedLines());
    }

    @Test
    public void load_doneFlagNeitherZeroNorOne_skipped() throws Exception {
        writeFile("T | yes | read book");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(1, storage.getSkippedLines());
    }

    @Test
    public void load_unparseableDate_skipped() throws Exception {
        // Storage and the command parser must agree on what a date looks like.
        writeFile("D | 0 | return book | 15-Oct-2019");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(1, storage.getSkippedLines());
    }

    @Test
    public void load_severalDamagedLines_goodLinesStillLoad() throws Exception {
        // One bad line must not cost the user the rest of the file.
        writeFile("T | 0 | first",
                "nonsense",
                "X | 0 | bad type",
                "E | 1 | meeting | 2019-10-15 1400 | 2019-10-15 1600");
        Storage storage = storage();
        ArrayList<Task> loaded = storage.load();

        assertEquals(2, loaded.size());
        assertEquals(2, storage.getSkippedLines());
        assertEquals("[T][ ] first", loaded.get(0).toString());
        assertEquals("[E][X] meeting (from: Oct 15 2019 2:00PM to: Oct 15 2019 4:00PM)",
                loaded.get(1).toString());
    }

    @Test
    public void getSkippedLines_secondLoadOfCleanFile_countResetToZero() throws Exception {
        writeFile("nonsense");
        Storage storage = storage();
        storage.load();
        assertEquals(1, storage.getSkippedLines());

        // The count describes the most recent load only; a stale count would make
        // Harvey complain about a file that is now fine.
        writeFile("T | 0 | read book");
        storage.load();
        assertEquals(0, storage.getSkippedLines());
    }

    @Test
    public void load_eventWithUnreadableTimes_lineIsSkipped() throws Exception {
        // Events used to be saved with their times as free text. Such a line can no longer
        // be understood, so it is treated as damaged rather than loaded as a broken event.
        writeFile("E | 0 | meeting | Mon 2pm | 4pm");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(1, storage.getSkippedLines());
    }

    @Test
    public void load_eventEndingBeforeItStarts_lineIsSkipped() throws Exception {
        // The file can be edited by hand, so an impossible event has to be caught on the
        // way in rather than reaching the clash check.
        writeFile("E | 0 | meeting | 2019-10-15 1600 | 2019-10-15 1400");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(1, storage.getSkippedLines());
    }
}
