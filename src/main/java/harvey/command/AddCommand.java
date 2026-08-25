package harvey.command;

import harvey.HarveyException;
import harvey.parser.Parser;
import harvey.storage.Storage;
import harvey.task.Task;
import harvey.task.TaskList;
import harvey.ui.Ui;
/**
 * Adds a new task to the list.
 * Covers {@code todo}, {@code deadline} and {@code event}, which differ only in how the
 * task is built from what the user typed.
 */
public class AddCommand extends Command {
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
        Task task = Parser.createTask(type, argument);
        tasks.add(task);
        ui.showReply("Got it. I've added this task:" + System.lineSeparator()
                + "  " + task + System.lineSeparator()
                + "Now you have " + tasks.size() + " tasks in the list.");
        storage.save(tasks.asList());
    }
}
