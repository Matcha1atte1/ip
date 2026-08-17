import java.util.Scanner;

/**
 * Entry point of the Harvey chatbot.
 * At this stage Harvey stores the text the user enters and lists it back on request.
 */
public class Harvey {
    /** Horizontal line used to separate Harvey's replies from the user's input. */
    private static final String DIVIDER = "____________________________________________________________";

    /** Command that ends the conversation. */
    private static final String COMMAND_BYE = "bye";

    /** Command that shows everything stored so far. */
    private static final String COMMAND_LIST = "list";

    /** Largest number of tasks Harvey can hold, as allowed by the Level-2 requirements. */
    private static final int MAX_TASKS = 100;

    /** ASCII art of the chatbot's name, shown once on startup. */
    private static final String BANNER = " _   _     _     ____  __     __ _____ __   __\n"
            + "| | | |   / \\   |  _ \\ \\ \\   / /| ____|\\ \\ / /\n"
            + "| |_| |  / _ \\  | |_) | \\ \\ / / |  _|   \\ V / \n"
            + "|  _  | / ___ \\ |  _ <   \\ V /  | |___   | |  \n"
            + "|_| |_|/_/   \\_\\|_| \\_\\   \\_/   |_____|  |_|  ";

    public static void main(String[] args) {
        String[] tasks = new String[MAX_TASKS];
        int taskCount = 0;

        showGreeting();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();

            if (input.equals(COMMAND_BYE)) {
                break;
            } else if (input.equals(COMMAND_LIST)) {
                showReply(formatTasks(tasks, taskCount));
            } else {
                tasks[taskCount] = input;
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
    private static String formatTasks(String[] tasks, int taskCount) {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < taskCount; i++) {
            if (i > 0) {
                list.append(System.lineSeparator());
            }
            list.append(i + 1).append(". ").append(tasks[i]);
        }
        return list.toString();
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
