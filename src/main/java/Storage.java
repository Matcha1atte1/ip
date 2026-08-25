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
}
