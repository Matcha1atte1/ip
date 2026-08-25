package harvey.command;

import harvey.HarveyException;
import harvey.storage.Storage;
import harvey.task.Deadline;
import harvey.task.Event;
import harvey.task.Task;
import harvey.task.Todo;
import harvey.task.TaskList;
import harvey.ui.Ui;
/**
 * Adds a new task to the list.
 * Covers {@code todo}, {@code deadline} and {@code event}, which differ only in how the
 * task is built from what the user typed.
 */
public class AddCommand extends Command {
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
    /** Which of the three task-creating instructions this is. */
    private final CommandType type;

    /** Everything the user typed after the command word. */
    private final String argument;

    /**
     * Creates a command that will add one task.
     *
     * @param type     the instruction typed, one of {@code TODO}, {@code DEADLINE} or {@code EVENT}
     * @param argument the description and any dates, as typed
     */
    public AddCommand(CommandType type, String argument) {
        this.type = type;
        this.argument = argument;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws HarveyException {
        Task task = createTask(type, argument);
        tasks.add(task);
        ui.showReply("Got it. I've added this task:" + System.lineSeparator()
                + "  " + task + System.lineSeparator()
                + "Now you have " + tasks.size() + " tasks in the list.");
        storage.save(tasks.asList());
    }

    /**
     * Builds the task described by an {@code todo}, {@code deadline} or {@code event} command.
     * The returned object is a {@link Todo}, {@link Deadline} or {@link Event}, but the
     * declared return type is {@code Task} so that the caller can store any of them in the
     * same array without caring which kind it is. This is polymorphism at work: the caller
     * later calls {@code toString()} on the stored task and each subclass supplies its own
     * version.
     *
     * @param command  the command that was typed, one of {@link CommandType#TODO},
     *                 {@link CommandType#DEADLINE} or {@link CommandType#EVENT}
     * @param argument everything typed after the command word
     * @return the new task
     * @throws HarveyException if the description or any required date is missing
     */
    private static Task createTask(CommandType command, String argument) throws HarveyException {
        if (argument.isEmpty()) {
            // "event" starts with a vowel, so it needs "An" rather than "A".
            String article = (command == CommandType.EVENT) ? "An " : "A ";
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
            // parseDate rejects anything that is not a real date, so a Deadline can never
            // be built holding text that only looks like one.
            return new Deadline(parts[0], Deadline.parseDate(parts[1]));
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
}
