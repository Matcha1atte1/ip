package harvey.command;

import harvey.storage.Storage;
import harvey.task.TaskList;
import harvey.ui.Ui;
/**
 * Ends the session.
 * <p>
 * It does nothing when executed: the parting message is printed by {@link Harvey} once the
 * loop has finished, so that it also appears when the input simply runs out and no
 * {@code bye} was ever typed.
 */
public class ExitCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        // Nothing to do; isExit() below is the whole of this command's effect.
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
