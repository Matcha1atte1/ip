import java.util.Scanner;

/**
 * Entry point of the Harvey chatbot.
 * At this stage Harvey stores three kinds of task ({@link Todo}, {@link Deadline} and
 * {@link Event}), lists them back on request, and remembers which have been marked as done.
 */
public class Harvey {
    /** Horizontal line used to separate Harvey's replies from the user's input. */
    private static final String DIVIDER = "____________________________________________________________";

    /** Command that ends the conversation. */
    private static final String COMMAND_BYE = "bye";

    /** Command that shows everything stored so far. */
    private static final String COMMAND_LIST = "list";

    /** Command that marks a task as done, used as {@code mark <task number>}. */
    private static final String COMMAND_MARK = "mark";

    /** Command that marks a task as not done, used as {@code unmark <task number>}. */
    private static final String COMMAND_UNMARK = "unmark";

    /** Command that adds a task with no date attached, used as {@code todo <description>}. */
    private static final String COMMAND_TODO = "todo";

    /** Command that adds a due task, used as {@code deadline <description> /by <when>}. */
    private static final String COMMAND_DEADLINE = "deadline";

    /** Command that adds a task spanning a period, used as {@code event <description> /from <start> /to <end>}. */
    private static final String COMMAND_EVENT = "event";

    /** Separator that introduces the due date of a deadline. */
    private static final String OPTION_BY = "/by";

    /** Separator that introduces the start time of an event. */
    private static final String OPTION_FROM = "/from";

    /** Separator that introduces the end time of an event. */
    private static final String OPTION_TO = "/to";

    /** Largest number of tasks Harvey can hold, as allowed by the Level-2 requirements. */
    private static final int MAX_TASKS = 100;

    /** ASCII art of the chatbot's name, shown once on startup. */
    private static final String BANNER = " _   _     _     ____  __     __ _____ __   __\n"
            + "| | | |   / \\   |  _ \\ \\ \\   / /| ____|\\ \\ / /\n"
            + "| |_| |  / _ \\  | |_) | \\ \\ / / |  _|   \\ V / \n"
            + "|  _  | / ___ \\ |  _ <   \\ V /  | |___   | |  \n"
            + "|_| |_|/_/   \\_\\|_| \\_\\   \\_/   |_____|  |_|  ";

    public static void main(String[] args) {
        // Each Task carries its own description and completion status,
        // so a single array is enough.
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        showGreeting();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();

            // Every command is a single word, optionally followed by arguments,
            // so splitting once here keeps the branches below simple.
            String[] words = input.split(" ", 2);
            String command = words[0];
            String argument = (words.length > 1) ? words[1].trim() : "";

            if (command.equals(COMMAND_BYE)) {
                break;
            } else if (command.equals(COMMAND_LIST)) {
                showReply("Here are the tasks in your list:" + System.lineSeparator()
                        + formatTasks(tasks, taskCount));
            } else if (command.equals(COMMAND_MARK) || command.equals(COMMAND_UNMARK)) {
                // The two commands differ only in the status they set and the message they show,
                // so they share one branch instead of duplicating the number-checking code.
                boolean isMarking = command.equals(COMMAND_MARK);

                // Task numbers shown to the user start at 1, so subtract 1 for the array index.
                int index = parseTaskNumber(argument, taskCount);
                if (index < 0) {
                    showReply("Sorry, that is not a valid task number.");
                } else {
                    String message;
                    if (isMarking) {
                        tasks[index].markAsDone();
                        message = "Nice! I've marked this task as done:";
                    } else {
                        tasks[index].markAsNotDone();
                        message = "OK, I've marked this task as not done yet:";
                    }
                    showReply(message + System.lineSeparator() + "  " + tasks[index]);
                }
            } else if (command.equals(COMMAND_TODO)
                    || command.equals(COMMAND_DEADLINE)
                    || command.equals(COMMAND_EVENT)) {
                // createTask returns null when the arguments are missing or malformed.
                Task task = createTask(command, argument);
                if (task == null) {
                    showReply("Sorry, I could not understand that " + command + ".");
                } else {
                    tasks[taskCount] = task;
                    taskCount++;
                    showReply("Got it. I've added this task:" + System.lineSeparator()
                            + "  " + task + System.lineSeparator()
                            + "Now you have " + taskCount + " tasks in the list.");
                }
            } else {
                showReply("Sorry, I don't know what \"" + command + "\" means.");
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
     * @param command  the command word that was typed
     * @param argument everything typed after the command word
     * @return the new task, or {@code null} if the argument is missing or malformed
     */
    private static Task createTask(String command, String argument) {
        if (argument.isEmpty()) {
            return null;
        }

        if (command.equals(COMMAND_TODO)) {
            return new Todo(argument);
        }

        if (command.equals(COMMAND_DEADLINE)) {
            // "return book /by Sunday" splits into "return book" and "Sunday".
            String[] parts = splitAtOption(argument, OPTION_BY);
            return (parts == null) ? null : new Deadline(parts[0], parts[1]);
        }

        // An event needs two separators, so split at "/from" first and then at "/to".
        String[] fromParts = splitAtOption(argument, OPTION_FROM);
        if (fromParts == null) {
            return null;
        }
        String[] toParts = splitAtOption(fromParts[1], OPTION_TO);
        return (toParts == null) ? null : new Event(fromParts[0], toParts[0], toParts[1]);
    }

    /**
     * Splits text at the first occurrence of an option such as {@code /by}.
     *
     * @param text   the text to split
     * @param option the option to split at
     * @return the text before and after the option, or {@code null} if the option is
     *         absent or either side of it is empty
     */
    private static String[] splitAtOption(String text, String option) {
        int optionPosition = text.indexOf(option);
        if (optionPosition < 0) {
            return null;
        }

        String before = text.substring(0, optionPosition).trim();
        String after = text.substring(optionPosition + option.length()).trim();
        if (before.isEmpty() || after.isEmpty()) {
            return null;
        }
        return new String[] {before, after};
    }

    /**
     * Builds the numbered list of stored tasks as a single block of text.
     *
     * @param tasks     the stored tasks
     * @param taskCount how many entries of {@code tasks} are actually in use
     * @return the tasks numbered from 1, one per line
     */
    private static String formatTasks(Task[] tasks, int taskCount) {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < taskCount; i++) {
            if (i > 0) {
                list.append(System.lineSeparator());
            }
            // Task.toString() supplies the "[X] description" part.
            list.append(i + 1).append('.').append(tasks[i]);
        }
        return list.toString();
    }

    /**
     * Converts the argument of a {@code mark} or {@code unmark} command into a valid array index.
     *
     * @param argument  the text typed after the command word
     * @param taskCount how many tasks are currently stored
     * @return the zero-based index of the task, or -1 if the argument is not a valid task number
     */
    private static int parseTaskNumber(String argument, int taskCount) {
        int index;
        try {
            index = Integer.parseInt(argument.trim()) - 1;
        } catch (NumberFormatException e) {
            // The user typed something that is not a number, e.g. "mark book".
            return -1;
        }
        return (index >= 0 && index < taskCount) ? index : -1;
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
