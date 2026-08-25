package harvey;

import harvey.command.Command;
import harvey.parser.Parser;
import harvey.storage.Storage;
import harvey.task.TaskList;
import harvey.ui.Ui;
/**
 * The Harvey chatbot.
 * <p>
 * Harvey stores three kinds of task ({@link Todo}, {@link Deadline} and {@link Event}),
 * lists them back on request, remembers which have been marked as done, and saves the
 * list to disk after every change. Input it cannot carry out is reported through
 * {@link HarveyException} instead of crashing.
 * <p>
 * This class is now only the conductor. Reading and printing belong to {@link Ui},
 * working out what the user meant belongs to {@link Parser}, holding the tasks belongs
 * to {@link TaskList}, and the file belongs to {@link Storage}. What is left here is the
 * order those four are used in.
 */
public class Harvey {
    /** Folder holding the save file, relative to the folder the program is started from. */
    private static final String DATA_FOLDER = "data";

    /** Name of the save file inside {@link #DATA_FOLDER}. */
    private static final String DATA_FILE = "harvey.txt";

    /** Reads what the user types and prints everything Harvey says back. */
    private final Ui ui;

    /** Loads the tasks at startup and writes them back after every change. */
    private final Storage storage;

    /** The tasks Harvey is keeping. Filled in by {@link #run()} once the file is read. */
    private TaskList tasks;

    /**
     * Creates a chatbot that saves to the given file.
     * <p>
     * Only the collaborators are built here; nothing is read or printed yet. Loading is
     * left to {@link #run()} so that the greeting can be shown before any complaint about
     * the save file, which would otherwise appear above it.
     *
     * @param folderName the folder holding the save file.
     * @param fileName   the name of the save file.
     */
    public Harvey(String folderName, String fileName) {
        this.ui = new Ui();
        this.storage = new Storage(folderName, fileName);
    }

    /** Greets the user, then answers commands until they say goodbye or the input ends. */
    public void run() {
        ui.showGreeting();
        tasks = loadTasks();

        // The loop no longer names any instruction. Parser decides which command a line
        // means, the command carries itself out, and the command says whether to stop.
        boolean isExit = false;
        while (!isExit && ui.hasNextCommand()) {
            String fullCommand = ui.readCommand();

            // Every step below may reject the input by throwing HarveyException.
            // Catching it here, once, means each step can simply describe what is
            // wrong and stop, instead of passing failure codes back up by hand.
            try {
                Command command = Parser.parse(fullCommand);
                command.execute(tasks, ui, storage);
                isExit = command.isExit();
            } catch (HarveyException e) {
                // getMessage() returns the explanation the thrower wrote for the user.
                ui.showError(e.getMessage());
            }
        }

        // Printed here rather than by ExitCommand, so that it also appears when the
        // input runs out without the user ever typing bye.
        ui.showFarewell();
    }

    /**
     * Reads the saved tasks, reporting anything that went wrong rather than refusing
     * to start.
     *
     * @return the saved tasks, or an empty list if the file could not be read.
     */
    private TaskList loadTasks() {
        try {
            TaskList loaded = new TaskList(storage.load());

            // Damaged lines are skipped rather than reported one by one, but the user is
            // told that some tasks are missing so the silence is not mistaken for the
            // file having been empty.
            if (storage.getSkippedLines() > 0) {
                ui.showError("I could not understand " + storage.getSkippedLines()
                        + " line(s) in your saved file, so those tasks were left out. "
                        + "Everything else was loaded.");
            }
            return loaded;
        } catch (HarveyException e) {
            // Being unable to read the saved file is not a reason to refuse to start,
            // so Harvey says what went wrong and carries on with nothing loaded.
            ui.showError(e.getMessage() + " Starting with an empty list.");
            return new TaskList();
        }
    }

    /**
     * Starts the chatbot.
     *
     * @param args not used.
     */
    public static void main(String[] args) {
        new Harvey(DATA_FOLDER, DATA_FILE).run();
    }
}
