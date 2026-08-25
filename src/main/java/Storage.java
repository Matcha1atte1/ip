import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes the task list to a file on disk, so tasks survive between runs.
 * <p>
 * Keeping all file handling in its own class means {@link Harvey} does not have to know
 * where the tasks are stored or in what format. If the format changes later, only this
 * class and the {@code toFileFormat} methods change.
 */
public class Storage {
    /** Where the tasks are stored, relative to the folder the program is started from. */
    private final Path filePath;

    /**
     * The same separator written as a regular expression, for splitting a line back up.
     * {@code split} treats its argument as a regex, in which {@code |} means "or", so the
     * bar has to be escaped as {@code \|} to stand for a literal bar character.
     */
    private static final String SEPARATOR_REGEX = " \\| ";

    /**
     * Creates a storage that reads and writes the given file.
     * <p>
     * The path is built with {@link Paths#get(String, String...)} from separate folder and
     * file names rather than written as one string like {@code "data/harvey.txt"}. Java then
     * joins them using whatever separator the current operating system uses ({@code /} on
     * macOS and Linux, {@code \} on Windows), so the same code works everywhere. It is also
     * a relative path, so it is resolved against the project folder rather than pointing at
     * one particular computer's hard disk.
     *
     * @param folderName the folder holding the file, e.g. {@code data}
     * @param fileName   the name of the file, e.g. {@code harvey.txt}
     */
    public Storage(String folderName, String fileName) {
        this.filePath = Paths.get(folderName, fileName);
    }

    /**
     * Overwrites the file with the given tasks, one task per line.
     * <p>
     * The whole list is rewritten each time rather than the changed line being edited in
     * place. That is slightly wasteful, but the list is small and it removes any chance of
     * the file drifting out of step with the list held in memory.
     *
     * @param tasks the tasks to store
     * @throws HarveyException if the file cannot be written
     */
    public void save(ArrayList<Task> tasks) throws HarveyException {
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            // Each subclass supplies its own line format, so this loop never needs to ask
            // whether it is holding a Todo, a Deadline or an Event.
            lines.add(task.toFileFormat());
        }

        try {
            // The data folder does not exist in a fresh copy of the project, and writing a
            // file into a missing folder fails. createDirectories does nothing if the
            // folder is already there, so it is safe to call every time.
            Path parentFolder = filePath.getParent();
            if (parentFolder != null) {
                Files.createDirectories(parentFolder);
            }
            Files.write(filePath, lines);
        } catch (IOException e) {
            // IOException is Java's way of reporting that the disk operation failed, e.g.
            // the file is read-only. Translating it into HarveyException means Harvey
            // reports it through the same channel as every other problem.
            throw new HarveyException("I could not save your tasks to " + filePath + ".");
        }
    }

    /**
     * Reads back the tasks previously written by {@link #save(ArrayList)}.
     * <p>
     * Note that loading cannot be polymorphic the way saving is. When saving, each task
     * already exists and can be asked for its own line. When loading there is no task yet
     * to ask, so something has to read the type letter and decide which subclass to build;
     * that decision lives here.
     *
     * @return the stored tasks, in the order they were written
     * @throws HarveyException if the file cannot be read
     */
    public ArrayList<Task> load() throws HarveyException {
        ArrayList<Task> tasks = new ArrayList<>();

        // The file is absent the first time anyone runs Harvey, which is normal rather
        // than a failure, so an empty list is returned instead of an error being raised.
        if (!Files.exists(filePath)) {
            return tasks;
        }

        try {
            for (String line : Files.readAllLines(filePath)) {
                tasks.add(toTask(line));
            }
        } catch (IOException e) {
            throw new HarveyException("I could not read your saved tasks from " + filePath + ".");
        }
        return tasks;
    }

    /**
     * Rebuilds one task from one line of the save file, reversing {@code toFileFormat}.
     *
     * @param line a line such as {@code D | 0 | return book | Sunday}
     * @return the task that line describes
     */
    private static Task toTask(String line) {
        // ["D", "0", "return book", "Sunday"] for the example above. The number of fields
        // depends on the task type, so they are read by position below.
        String[] fields = line.split(SEPARATOR_REGEX);
        String typeLetter = fields[0];
        String doneFlag = fields[1];
        String description = fields[2];

        Task task;
        switch (typeLetter) {
        case "D":
            task = new Deadline(description, fields[3]);
            break;
        case "E":
            task = new Event(description, fields[3], fields[4]);
            break;
        default:
            task = new Todo(description);
            break;
        }

        // Every task is built as not-done, so the stored flag is applied afterwards
        // rather than being passed through four separate constructors.
        if (doneFlag.equals("1")) {
            task.markAsDone();
        }
        return task;
    }
}
