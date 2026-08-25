import java.util.ArrayList;
import java.util.Scanner;

/**
 * Entry point of the Harvey chatbot.
 * At this stage Harvey stores three kinds of task ({@link Todo}, {@link Deadline} and
 * {@link Event}), lists them back on request, and remembers which have been marked as done.
 * Input it cannot carry out is reported through {@link HarveyException} instead of crashing.
 */
public class Harvey {
    /** Horizontal line used to separate Harvey's replies from the user's input. */
    private static final String DIVIDER = "____________________________________________________________";

    /** Folder holding the save file, relative to the folder the program is started from. */
    private static final String DATA_FOLDER = "data";

    /** Name of the save file inside {@link #DATA_FOLDER}. */
    private static final String DATA_FILE = "harvey.txt";

    /**
     * Character reserved for separating fields in the save file.
     * Task text containing it could not be read back, so it is refused on the way in.
     */
    private static final String RESERVED_CHARACTER = "|";

    /** Separator that introduces the due date of a deadline. */
    private static final String OPTION_BY = "/by";

    /** Separator that introduces the start time of an event. */
    private static final String OPTION_FROM = "/from";

    /** Separator that introduces the end time of an event. */
    private static final String OPTION_TO = "/to";

    /** ASCII art of the chatbot's name, shown once on startup. */
    private static final String BANNER = " _   _     _     ____  __     __ _____ __   __\n"
            + "| | | |   / \\   |  _ \\ \\ \\   / /| ____|\\ \\ / /\n"
            + "| |_| |  / _ \\  | |_) | \\ \\ / / |  _|   \\ V / \n"
            + "|  _  | / ___ \\ |  _ <   \\ V /  | |___   | |  \n"
            + "|_| |_|/_/   \\_\\|_| \\_\\   \\_/   |_____|  |_|  ";

    public static void main(String[] args) {
        // Created once and reused, so the file location is decided in a single place.
        Storage storage = new Storage(DATA_FOLDER, DATA_FILE);

        showGreeting();

        // An ArrayList grows as tasks are added and closes the gap when one is
        // removed, so Harvey no longer needs a fixed capacity or its own counter.
        // It starts as whatever was saved last time rather than empty.
        ArrayList<Task> tasks;
        try {
            tasks = storage.load();

            // Damaged lines are skipped rather than reported one by one, but the user is
            // told that some tasks are missing so the silence is not mistaken for the
            // file having been empty.
            if (storage.getSkippedLines() > 0) {
                showReply("Sorry! I could not understand " + storage.getSkippedLines()
                        + " line(s) in your saved file, so those tasks were left out. "
                        + "Everything else was loaded.");
            }
        } catch (HarveyException e) {
            // Being unable to read the saved file is not a reason to refuse to start,
            // so Harvey says what went wrong and carries on with nothing loaded.
            showReply("Sorry! " + e.getMessage() + " Starting with an empty list.");
            tasks = new ArrayList<>();
        }

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            // Trimming here means stray spaces around the input do not hide the command word.
            String input = scanner.nextLine().trim();

            // Every command is a single word, optionally followed by arguments,
            // so splitting once here keeps the branches below simple.
            String[] words = input.split(" ", 2);
            String keyword = words[0];
            String argument = (words.length > 1) ? words[1].trim() : "";

            // Every step below may reject the input by throwing HarveyException.
            // Catching it here, once, means each step can simply describe what is
            // wrong and stop, instead of passing failure codes back up by hand.
            try {
                // Turning the typed word into a Command up front means the switch below
                // deals in a fixed set of values rather than in free-form text.
                Command command = Command.fromKeyword(keyword);

                if (command == Command.BYE) {
                    break;
                }

                switch (command) {
                case LIST:
                    if (tasks.isEmpty()) {
                        throw new HarveyException("Your list is empty. Add something with, say: "
                                + Command.TODO.getExample());
                    }
                    showReply("Here are the tasks in your list:" + System.lineSeparator()
                            + formatTasks(tasks));
                    break;
                case MARK:
                    // Task numbers shown to the user start at 1, so subtract 1 for the list index.
                    Task marked = tasks.get(parseTaskNumber(argument, tasks.size(), command));
                    marked.markAsDone();
                    showReply("Nice! I've marked this task as done:" + System.lineSeparator()
                            + "  " + marked);
                    break;
                case UNMARK:
                    Task unmarked = tasks.get(parseTaskNumber(argument, tasks.size(), command));
                    unmarked.markAsNotDone();
                    showReply("OK, I've marked this task as not done yet:" + System.lineSeparator()
                            + "  " + unmarked);
                    break;
                case DELETE:
                    // remove() returns the task it took out and shifts the rest down,
                    // so the remaining tasks stay numbered 1, 2, 3, ... with no gap.
                    Task removed = tasks.remove(parseTaskNumber(argument, tasks.size(), command));
                    showReply("Noted. I've removed this task:" + System.lineSeparator()
                            + "  " + removed + System.lineSeparator()
                            + "Now you have " + tasks.size() + " tasks in the list.");
                    break;
                default:
                    // The three task-creating commands, which differ only inside createTask.
                    Task task = createTask(command, argument);
                    tasks.add(task);
                    showReply("Got it. I've added this task:" + System.lineSeparator()
                            + "  " + task + System.lineSeparator()
                            + "Now you have " + tasks.size() + " tasks in the list.");
                    break;
                }

                // Saving once here, after the switch, covers every command that changed the
                // list without repeating the call in each branch. LIST reaches this line
                // too, which harmlessly writes the same content back.
                storage.save(tasks);
            } catch (HarveyException e) {
                // getMessage() returns the explanation the thrower wrote for the user.
                showReply("Sorry! " + e.getMessage());
            }
        }

        showReply("Bye. Hope to see you again soon!");
    }

    /**
     * Builds the task described by an {@code todo}, {@code deadline} or {@code event} command.
     * The returned object is a {@link Todo}, {@link Deadline} or {@link Event}, but the
     * declared return type is {@code Task} so that the caller can store any of them in the
     * same array without caring which kind it is. This is polymorphism at work: the caller
     * later calls {@code toString()} on the stored task and each subclass supplies its own
     * version.
     *
     * @param command  the command that was typed, one of {@link Command#TODO},
     *                 {@link Command#DEADLINE} or {@link Command#EVENT}
     * @param argument everything typed after the command word
     * @return the new task
     * @throws HarveyException if the description or any required date is missing
     */
    private static Task createTask(Command command, String argument) throws HarveyException {
        if (argument.isEmpty()) {
            // "event" starts with a vowel, so it needs "An" rather than "A".
            String article = (command == Command.EVENT) ? "An " : "A ";
            throw new HarveyException(article + command.getKeyword() + " needs a description. "
                    + "For example: " + command.getExample());
        }

        // Checked once here, before the argument is split up, so it covers the description
        // and every date field of all three task types.
        if (argument.contains(RESERVED_CHARACTER)) {
            throw new HarveyException("Please leave out the \"" + RESERVED_CHARACTER
                    + "\" character. I use it to separate fields when saving your tasks, "
                    + "so a task containing it could not be loaded back.");
        }

        switch (command) {
        case TODO:
            return new Todo(argument);
        case DEADLINE:
            // "return book /by Sunday" splits into "return book" and "Sunday".
            String[] parts = splitAtOption(argument, OPTION_BY,
                    "A deadline needs a due date after " + OPTION_BY + ". For example: "
                            + command.getExample());
            return new Deadline(parts[0], parts[1]);
        default:
            // An event needs two separators, so split at "/from" first and then at "/to".
            String eventHelp = "An event needs a start after " + OPTION_FROM + " and an end after "
                    + OPTION_TO + ". For example: " + command.getExample();
            String[] fromParts = splitAtOption(argument, OPTION_FROM, eventHelp);
            String[] toParts = splitAtOption(fromParts[1], OPTION_TO, eventHelp);
            return new Event(fromParts[0], toParts[0], toParts[1]);
        }
    }

    /**
     * Splits text at the first occurrence of an option such as {@code /by}.
     *
     * @param text            the text to split
     * @param option          the option to split at
     * @param errorMessage    the explanation to show the user if the split is not possible
     * @return the text before and after the option
     * @throws HarveyException if the option is absent or either side of it is empty
     */
    private static String[] splitAtOption(String text, String option, String errorMessage)
            throws HarveyException {
        int optionPosition = text.indexOf(option);
        if (optionPosition < 0) {
            throw new HarveyException(errorMessage);
        }

        String before = text.substring(0, optionPosition).trim();
        String after = text.substring(optionPosition + option.length()).trim();
        if (before.isEmpty() || after.isEmpty()) {
            throw new HarveyException(errorMessage);
        }
        return new String[] {before, after};
    }

    /**
     * Builds the numbered list of stored tasks as a single block of text.
     *
     * @param tasks the stored tasks
     * @return the tasks numbered from 1, one per line
     */
    private static String formatTasks(ArrayList<Task> tasks) {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            if (i > 0) {
                list.append(System.lineSeparator());
            }
            // Task.toString() supplies the "[X] description" part.
            list.append(i + 1).append('.').append(tasks.get(i));
        }
        return list.toString();
    }

    /**
     * Converts the argument of a {@code mark} or {@code unmark} command into a valid array index.
     *
     * @param argument  the text typed after the command word
     * @param taskCount how many tasks are currently stored
     * @param command   the command being run, used to make the error messages specific
     * @return the zero-based index of the task
     * @throws HarveyException if the argument is missing, is not a number, or names a task
     *                         that does not exist
     */
    private static int parseTaskNumber(String argument, int taskCount, Command command)
            throws HarveyException {
        if (argument.isEmpty()) {
            throw new HarveyException("Tell me which task to " + command.getKeyword()
                    + ". For example: " + command.getExample());
        }
        if (taskCount == 0) {
            throw new HarveyException("You have no tasks yet, so there is nothing to "
                    + command.getKeyword() + ".");
        }

        int index;
        try {
            index = Integer.parseInt(argument) - 1;
        } catch (NumberFormatException e) {
            // The user typed something that is not a number, e.g. "mark book".
            // The original exception is not shown to the user; the advice below is more useful.
            throw new HarveyException("\"" + argument + "\" is not a task number. Use the number "
                    + "shown by list, for example: " + command.getExample());
        }

        if (index < 0 || index >= taskCount) {
            throw new HarveyException("There is no task " + (index + 1) + ". You have "
                    + taskCount + " task(s), so pick a number from 1 to " + taskCount + ".");
        }
        return index;
    }

    /** Prints the banner and welcome message shown when Harvey starts up. */
    private static void showGreeting() {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println("Hello! I'm Harvey.");
        System.out.println("What can I do for you?");
        System.out.println(DIVIDER);
    }

    /**
     * Prints a single reply from Harvey, wrapped in dividers so it stands out
     * from the lines the user typed.
     *
     * @param message the text to show to the user
     */
    private static void showReply(String message) {
        System.out.println(message);
        System.out.println(DIVIDER);
    }
}
