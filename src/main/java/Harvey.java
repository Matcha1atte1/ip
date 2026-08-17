import java.util.Scanner;

/**
 * Entry point of the Harvey chatbot.
 * At this stage Harvey stores the text the user enters, lists it back on request,
 * and remembers which of those tasks have been marked as done.
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

            if (input.equals(COMMAND_BYE)) {
                break;
            } else if (input.equals(COMMAND_LIST)) {
                showReply("Here are the tasks in your list:" + System.lineSeparator()
                        + formatTasks(tasks, taskCount));
            } else if (input.startsWith(COMMAND_MARK + " ") || input.startsWith(COMMAND_UNMARK + " ")) {
                // The two commands differ only in the status they set and the message they show,
                // so they share one branch instead of duplicating the number-checking code.
                boolean isMarking = input.startsWith(COMMAND_MARK + " ");
                String command = isMarking ? COMMAND_MARK : COMMAND_UNMARK;

                // Task numbers shown to the user start at 1, so subtract 1 for the array index.
                int index = parseTaskNumber(input.substring(command.length() + 1), taskCount);
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
            } else {
                tasks[taskCount] = new Task(input);
                taskCount++;
                showReply("added: " + input);
            }
        }

        showReply("Bye. Hope to see you again soon!");
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
