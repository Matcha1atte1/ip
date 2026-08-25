package harvey.ui;

import harvey.task.Task;
import harvey.task.TaskList;

import java.util.Scanner;
/**
 * Handles everything Harvey shows the user and everything the user types back.
 * <p>
 * Pulling this out of {@link Harvey} means the rest of the program never calls
 * {@code System.out} or touches a {@link Scanner} directly. Two things follow from that:
 * the wording and layout of Harvey's replies can be changed in one file, and if the
 * chatbot later grows a graphical window, only this class has to be replaced.
 */
public class Ui {
    /** Horizontal line used to separate Harvey's replies from the user's input. */
    private static final String DIVIDER = "____________________________________________________________";

    /** ASCII art of the chatbot's name, shown once on startup. */
    private static final String BANNER = " _   _     _     ____  __     __ _____ __   __\n"
            + "| | | |   / \\   |  _ \\ \\ \\   / /| ____|\\ \\ / /\n"
            + "| |_| |  / _ \\  | |_) | \\ \\ / / |  _|   \\ V / \n"
            + "|  _  | / ___ \\ |  _ <   \\ V /  | |___   | |  \n"
            + "|_| |_|/_/   \\_\\|_| \\_\\   \\_/   |_____|  |_|  ";

    /** Prefix put in front of anything that went wrong, so mistakes read consistently. */
    private static final String ERROR_PREFIX = "Sorry! ";

    /** Reads the lines the user types. Kept as a field so one Scanner serves the whole run. */
    private final Scanner scanner;

    /** Creates a user interface that reads from standard input and writes to standard output. */
    public Ui() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Returns whether the user has typed anything more.
     * This is false once input runs out, e.g. the user pressed Ctrl-D.
     *
     * @return true if there is another line to read.
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next line the user typed.
     * Trimming here means stray spaces around the input do not hide the command word,
     * so no caller has to remember to do it.
     *
     * @return the line, without surrounding spaces.
     */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /** Prints the banner and welcome message shown when Harvey starts up. */
    public void showGreeting() {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println("Hello! I'm Harvey.");
        System.out.println("What can I do for you?");
        System.out.println(DIVIDER);
    }

    /**
     * Prints a single reply from Harvey, wrapped in a divider so it stands out
     * from the lines the user typed.
     *
     * @param message the text to show to the user.
     */
    public void showReply(String message) {
        System.out.println(message);
        System.out.println(DIVIDER);
    }

    /**
     * Reports something that went wrong, in the same shape as any other reply.
     * Callers pass only the explanation; the apology in front is added here so that
     * every error reads the same way.
     *
     * @param message what went wrong, phrased for the user.
     */
    public void showError(String message) {
        showReply(ERROR_PREFIX + message);
    }

    /** Prints the parting message shown when the user says goodbye or input runs out. */
    public void showFarewell() {
        showReply("Bye. Hope to see you again soon!");
    }

    /**
     * Prints the stored tasks, numbered from 1 as the user refers to them.
     * <p>
     * The numbering is display, not storage, so it is done here rather than in
     * {@link TaskList}: the list itself has no opinion about how it should look.
     *
     * @param tasks the tasks to show, assumed not empty.
     */
    public void showTaskList(TaskList tasks) {
        StringBuilder message = new StringBuilder("Here are the tasks in your list:");
        int taskNumber = 1;
        for (Task task : tasks.asList()) {
            // Task.toString() supplies the "[X] description" part.
            message.append(System.lineSeparator())
                    .append(taskNumber).append('.').append(task);
            taskNumber++;
        }
        showReply(message.toString());
    }
}
