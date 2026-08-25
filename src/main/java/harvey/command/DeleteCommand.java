package harvey.command;

import harvey.HarveyException;
import harvey.parser.Parser;
import harvey.storage.Storage;
import harvey.task.Task;
import harvey.task.TaskList;
import harvey.ui.Ui;
/** Removes one task from the list. */
public class DeleteCommand extends Command {
    /** The task number the user typed, still unchecked. */
    private final String argument;

    /**
     * Creates a command that will remove one task.
     *
     * @param argument the task number, as typed
     */
    public DeleteCommand(String argument) {
        this.argument = argument;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws HarveyException {
        Task removed = tasks.delete(Parser.parseTaskNumber(argument, tasks, CommandType.DELETE));
        ui.showReply("Noted. I've removed this task:" + System.lineSeparator()
                + "  " + removed + System.lineSeparator()
                + "Now you have " + tasks.size() + " tasks in the list.");
        storage.save(tasks.asList());
    }
}
