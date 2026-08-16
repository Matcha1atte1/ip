/**
 * Entry point of the Harvey chatbot.
 * At this stage Harvey only greets the user and exits immediately.
 */
public class Harvey {
    /** Horizontal line used to separate Harvey's replies from the user's input. */
    private static final String DIVIDER = "____________________________________________________________";

    /** ASCII art of the chatbot's name, shown once on startup. */
    private static final String BANNER = " _   _     _     ____  __     __ _____ __   __\n"
            + "| | | |   / \\   |  _ \\ \\ \\   / /| ____|\\ \\ / /\n"
            + "| |_| |  / _ \\  | |_) | \\ \\ / / |  _|   \\ V / \n"
            + "|  _  | / ___ \\ |  _ <   \\ V /  | |___   | |  \n"
            + "|_| |_|/_/   \\_\\|_| \\_\\   \\_/   |_____|  |_|  ";

    public static void main(String[] args) {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println("Hello! I'm Harvey.");
        System.out.println("What can I do for you?");
        System.out.println(DIVIDER);
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(DIVIDER);
    }
}
