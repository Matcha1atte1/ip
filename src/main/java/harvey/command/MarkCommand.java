package harvey.command;

import harvey.HarveyException;
import harvey.storage.Storage;
import harvey.task.Task;
import harvey.task.TaskList;
import harvey.ui.Ui;
/** Marks one task as completed. */
public class MarkCommand extends Command {
    /** The task number the user typed, still unchecked. */
    private final String argument;

    /**
     * Creates a command that will mark one task as done.
     *
     * @param argument the task number, as typed.
     */
    public MarkCommand(String argument) {
        this.argument = argument;
    }

    @Override
    public String execute(TaskList tasks, Ui ui, Storage storage) throws HarveyException {
        Task marked = tasks.get(parseTaskNumber(argument, tasks, CommandType.MARK));
        marked.markAsDone();
        storage.save(tasks.asList());
        return "Nice! I've marked this task as done:" + System.lineSeparator()
                + "  " + marked;
    }
}
