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
     * @param folderName the folder holding the save file
     * @param fileName   the name of the save file
     */
    public Harvey(String folderName, String fileName) {
        this.ui = new Ui();
        this.storage = new Storage(folderName, fileName);
    }

    /** Greets the user, then answers commands until they say goodbye or the input ends. */
    public void run() {
        ui.showGreeting();
        tasks = loadTasks();

        while (ui.hasNextCommand()) {
            String input = ui.readCommand();

            // Every step below may reject the input by throwing HarveyException.
            // Catching it here, once, means each step can simply describe what is
            // wrong and stop, instead of passing failure codes back up by hand.
            try {
                // Turning the typed line into a Command up front means the switch below
                // deals in a fixed set of values rather than in free-form text.
                Command command = Parser.parseCommand(input);
                String argument = Parser.parseArgument(input);

                if (command == Command.BYE) {
                    break;
                }

                runCommand(command, argument);

                // Saving once here, after the command has run, covers everything that
                // changed the list without repeating the call in each branch. LIST reaches
                // this line too, which harmlessly writes the same content back.
                storage.save(tasks.asList());
            } catch (HarveyException e) {
                // getMessage() returns the explanation the thrower wrote for the user.
                ui.showError(e.getMessage());
            }
        }

        ui.showFarewell();
    }

    /**
     * Carries out one command that has already been understood.
     *
     * @param command  what the user asked for
     * @param argument everything typed after the command word
     * @throws HarveyException if the command cannot be carried out as asked
     */
    private void runCommand(Command command, String argument) throws HarveyException {
        switch (command) {
        case LIST:
            if (tasks.isEmpty()) {
                throw new HarveyException("Your list is empty. Add something with, say: "
                        + Command.TODO.getExample());
            }
            ui.showTaskList(tasks);
            break;
        case MARK:
            Task marked = tasks.get(Parser.parseTaskNumber(argument, tasks, command));
            marked.markAsDone();
            ui.showReply("Nice! I've marked this task as done:" + System.lineSeparator()
                    + "  " + marked);
            break;
        case UNMARK:
            Task unmarked = tasks.get(Parser.parseTaskNumber(argument, tasks, command));
            unmarked.markAsNotDone();
            ui.showReply("OK, I've marked this task as not done yet:" + System.lineSeparator()
                    + "  " + unmarked);
            break;
        case DELETE:
            Task removed = tasks.delete(Parser.parseTaskNumber(argument, tasks, command));
            ui.showReply("Noted. I've removed this task:" + System.lineSeparator()
                    + "  " + removed + System.lineSeparator()
                    + "Now you have " + tasks.size() + " tasks in the list.");
            break;
        default:
            // The three task-creating commands, which differ only inside createTask.
            Task task = Parser.createTask(command, argument);
            tasks.add(task);
            ui.showReply("Got it. I've added this task:" + System.lineSeparator()
                    + "  " + task + System.lineSeparator()
                    + "Now you have " + tasks.size() + " tasks in the list.");
            break;
        }
    }

    /**
     * Reads the saved tasks, reporting anything that went wrong rather than refusing
     * to start.
     *
     * @return the saved tasks, or an empty list if the file could not be read
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
     * @param args not used
     */
    public static void main(String[] args) {
        new Harvey(DATA_FOLDER, DATA_FILE).run();
    }
}
