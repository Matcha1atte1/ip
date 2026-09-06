package harvey.command;

import java.util.Optional;

import harvey.HarveyException;
import harvey.storage.Storage;
import harvey.task.Deadline;
import harvey.task.Event;
import harvey.task.Task;
import harvey.task.TaskList;
import harvey.task.Todo;
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
     * @param type     the instruction typed, one of {@code TODO}, {@code DEADLINE} or {@code EVENT}.
     * @param argument the description and any dates, as typed.
     */
    public AddCommand(CommandType type, String argument) {
        this.type = type;
        this.argument = argument;
    }

    @Override
    public String execute(TaskList tasks, Ui ui, Storage storage) throws HarveyException {
        Task task = createTask(type, argument);
        if (task instanceof Event event) {
            requireNoClash(event, tasks, ui);
        }
        tasks.add(task);

        // Saved before the reply is returned, so that a failure to write the file is
        // reported as an error instead of being hidden behind a cheerful confirmation.
        storage.save(tasks.asList());
        return ui.formatLines("Got it. I've added this task:",
                "  " + task,
                ui.formatTaskCount(tasks.size()));
    }

    /**
     * Checks that an event about to be added does not collide with one already stored.
     * <p>
     * Called before the task is added and before anything is saved, so a refused event
     * leaves both the list and the file exactly as they were. A tutor cannot teach two
     * students at once, so a collision is treated as a mistake to correct rather than a
     * warning to read and ignore.
     *
     * @param event the event being added.
     * @param tasks the tasks already stored.
     * @param ui    the source of the reply format, used to lay the message out.
     * @throws HarveyException if the event overlaps one already in the list.
     */
    private static void requireNoClash(Event event, TaskList tasks, Ui ui) throws HarveyException {
        Optional<Event> clash = tasks.findClash(event);
        if (clash.isEmpty()) {
            return;
        }

        // The task number is what the user needs to delete or inspect the other event,
        // and is the number list shows, so it counts from 1.
        int clashingNumber = tasks.asList().indexOf(clash.get()) + 1;
        throw new HarveyException(ui.formatLines(
                "That clashes with an event you already have:",
                "  " + clash.get(),
                "Nothing was added. Pick a different time, or delete task "
                        + clashingNumber + " first."));
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
     *                 {@link CommandType#DEADLINE} or {@link CommandType#EVENT}.
     * @param argument everything typed after the command word.
     * @return the new task.
     * @throws HarveyException if the description or any required date is missing.
     */
    private static Task createTask(CommandType command, String argument) throws HarveyException {
        assert argument != null : "Parser.parseArgument() returns an empty string when there is no argument";
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
            case EVENT:
                // An event needs two separators, so split at "/from" first and then at "/to".
                String eventHelp = "An event needs a start after " + OPTION_FROM + " and an end after "
                        + OPTION_TO + ". For example: " + command.getExample();
                String[] fromParts = splitAtOption(argument, OPTION_FROM, eventHelp);
                String[] toParts = splitAtOption(fromParts[1], OPTION_TO, eventHelp);
                return new Event(fromParts[0], Event.parseDateTime(toParts[0]),
                        Event.parseDateTime(toParts[1]));
            default:
                // Parser sends only the three commands named above here, so reaching this
                // means a fourth was routed here without being given a task to build.
                throw new HarveyException(command.getKeyword() + " does not create a task.");
        }
    }

    /**
     * Splits text at the first occurrence of an option such as {@code /by}.
     *
     * @param text            the text to split.
     * @param option          the option to split at.
     * @param errorMessage    the explanation to show the user if the split is not possible.
     * @return the text before and after the option.
     * @throws HarveyException if the option is absent or either side of it is empty.
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
