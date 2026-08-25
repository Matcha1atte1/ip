package harvey.parser;

import harvey.HarveyException;
import harvey.command.AddCommand;
import harvey.command.Command;
import harvey.command.CommandType;
import harvey.command.DeleteCommand;
import harvey.command.ExitCommand;
import harvey.command.ListCommand;
import harvey.command.MarkCommand;
import harvey.command.UnmarkCommand;
import harvey.task.Deadline;
import harvey.task.Event;
import harvey.task.Task;
import harvey.task.TaskList;
import harvey.task.Todo;
/**
 * Works out what the user meant by what they typed.
 * <p>
 * Everything here turns text into something the rest of the program can act on: a
 * {@link CommandType}, a task number, or a {@link Task}. Keeping it in one class means the
 * rules about what counts as valid input, and the phrasing used to explain a mistake,
 * are all in one file rather than mixed in with the code that carries the command out.
 * <p>
 * The methods are static because parsing needs no memory of what came before; each call
 * depends only on the text it is given.
 */
public class Parser {
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

    /**
     * Turns one typed line into the command it asks for.
     * <p>
     * This is the only method the main loop needs: it decides which subclass of
     * {@link Command} the line means, and hands back something that knows how to carry
     * itself out. The loop therefore never names the individual instructions.
     *
     * @param fullCommand one line as the user typed it, already trimmed
     * @return the command that line asks for
     * @throws HarveyException if the line does not name a command Harvey knows
     */
    public static Command parse(String fullCommand) throws HarveyException {
        CommandType type = parseCommandType(fullCommand);
        String argument = parseArgument(fullCommand);

        switch (type) {
        case BYE:
            return new ExitCommand();
        case LIST:
            return new ListCommand();
        case MARK:
            return new MarkCommand(argument);
        case UNMARK:
            return new UnmarkCommand(argument);
        case DELETE:
            return new DeleteCommand(argument);
        default:
            // The three task-creating instructions, which AddCommand tells apart itself.
            return new AddCommand(type, argument);
        }
    }

    /**
     * Finds which command the user typed.
     *
     * @param input one line as the user typed it, already trimmed
     * @return the command that line invokes
     * @throws HarveyException if the first word is not a command Harvey knows
     */
    private static CommandType parseCommandType(String input) throws HarveyException {
        return CommandType.fromKeyword(splitOffKeyword(input)[0]);
    }

    /**
     * Returns everything the user typed after the command word.
     *
     * @param input one line as the user typed it, already trimmed
     * @return the argument, or an empty string if there was none
     */
    private static String parseArgument(String input) {
        String[] words = splitOffKeyword(input);
        return (words.length > 1) ? words[1].trim() : "";
    }

    /**
     * Splits a line into the command word and everything after it.
     * Every command is a single word optionally followed by arguments, so splitting
     * once is enough; the limit of 2 keeps any further spaces inside the argument.
     *
     * @param input one line as the user typed it
     * @return an array of one or two parts
     */
    private static String[] splitOffKeyword(String input) {
        return input.split(" ", 2);
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
    public static Task createTask(CommandType command, String argument) throws HarveyException {
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

    /**
     * Converts the argument of a {@code mark} or {@code unmark} command into a task number.
     * Whether that number names an existing task is checked by {@link TaskList}, which is
     * the class that knows how long the list is.
     *
     * @param argument  the text typed after the command word
     * @param tasks     the current task list, used to reject a number when it is empty
     * @param command   the command being run, used to make the error messages specific
     * @return the task number the user typed, counting from 1
     * @throws HarveyException if the argument is missing or is not a number
     */
    public static int parseTaskNumber(String argument, TaskList tasks, CommandType command)
            throws HarveyException {
        if (argument.isEmpty()) {
            throw new HarveyException("Tell me which task to " + command.getKeyword()
                    + ". For example: " + command.getExample());
        }
        if (tasks.isEmpty()) {
            throw new HarveyException("You have no tasks yet, so there is nothing to "
                    + command.getKeyword() + ".");
        }

        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            // The user typed something that is not a number, e.g. "mark book".
            // The original exception is not shown to the user; the advice below is more useful.
            throw new HarveyException("\"" + argument + "\" is not a task number. Use the number "
                    + "shown by list, for example: " + command.getExample());
        }
    }
}
