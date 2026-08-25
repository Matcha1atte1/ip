
/**
 * Entry point of the Harvey chatbot.
 * At this stage Harvey stores three kinds of task ({@link Todo}, {@link Deadline} and
 * {@link Event}), lists them back on request, and remembers which have been marked as done.
 * Input it cannot carry out is reported through {@link HarveyException} instead of crashing.
 */
public class Harvey {
    /** Folder holding the save file, relative to the folder the program is started from. */
    private static final String DATA_FOLDER = "data";

    /** Name of the save file inside {@link #DATA_FOLDER}. */
    private static final String DATA_FILE = "harvey.txt";


    public static void main(String[] args) {
        // Created once and reused, so the file location is decided in a single place.
        Storage storage = new Storage(DATA_FOLDER, DATA_FILE);

        // All reading and printing goes through this object from here on.
        Ui ui = new Ui();

        ui.showGreeting();

        // An ArrayList grows as tasks are added and closes the gap when one is
        // removed, so Harvey no longer needs a fixed capacity or its own counter.
        // It starts as whatever was saved last time rather than empty.
        TaskList tasks;
        try {
            tasks = new TaskList(storage.load());

            // Damaged lines are skipped rather than reported one by one, but the user is
            // told that some tasks are missing so the silence is not mistaken for the
            // file having been empty.
            if (storage.getSkippedLines() > 0) {
                ui.showError("I could not understand " + storage.getSkippedLines()
                        + " line(s) in your saved file, so those tasks were left out. "
                        + "Everything else was loaded.");
            }
        } catch (HarveyException e) {
            // Being unable to read the saved file is not a reason to refuse to start,
            // so Harvey says what went wrong and carries on with nothing loaded.
            ui.showError(e.getMessage() + " Starting with an empty list.");
            tasks = new TaskList();
        }

        while (ui.hasNextCommand()) {
            String input = ui.readCommand();

            String argument = Parser.parseArgument(input);

            // Every step below may reject the input by throwing HarveyException.
            // Catching it here, once, means each step can simply describe what is
            // wrong and stop, instead of passing failure codes back up by hand.
            try {
                // Turning the typed line into a Command up front means the switch below
                // deals in a fixed set of values rather than in free-form text.
                Command command = Parser.parseCommand(input);

                if (command == Command.BYE) {
                    break;
                }

                switch (command) {
                case LIST:
                    if (tasks.isEmpty()) {
                        throw new HarveyException("Your list is empty. Add something with, say: "
                                + Command.TODO.getExample());
                    }
                    ui.showReply("Here are the tasks in your list:" + System.lineSeparator()
                            + formatTasks(tasks));
                    break;
                case MARK:
                    // Task numbers shown to the user start at 1, so subtract 1 for the list index.
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

                // Saving once here, after the switch, covers every command that changed the
                // list without repeating the call in each branch. LIST reaches this line
                // too, which harmlessly writes the same content back.
                storage.save(tasks.asList());
            } catch (HarveyException e) {
                // getMessage() returns the explanation the thrower wrote for the user.
                ui.showError(e.getMessage());
            }
        }

        ui.showFarewell();
    }


    /**
     * Builds the numbered list of stored tasks as a single block of text.
     *
     * @param tasks the stored tasks
     * @return the tasks numbered from 1, one per line
     * @throws HarveyException never in practice, since only existing numbers are asked for
     */
    private static String formatTasks(TaskList tasks) throws HarveyException {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            if (i > 0) {
                list.append(System.lineSeparator());
            }
            // Task.toString() supplies the "[X] description" part.
            list.append(i + 1).append('.').append(tasks.get(i + 1));
        }
        return list.toString();
    }
}
