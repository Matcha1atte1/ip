package harvey.command;

import harvey.HarveyException;
import harvey.storage.Storage;
import harvey.task.TaskList;
import harvey.ui.Ui;
/**
 * One instruction from the user, in a form that can carry itself out.
 * <p>
 * Previously {@link Harvey} held a switch that named every instruction and spelled out
 * what each one did. Each branch of that switch is now its own subclass, so the code for
 * an instruction sits in one place with a name on it, and adding another instruction means
 * adding a class rather than editing a growing switch.
 * <p>
 * This is an abstract class rather than an interface because {@link #isExit()} has a
 * sensible default that almost every subclass wants to inherit unchanged.
 */
public abstract class Command {
    /**
     * Carries out this instruction.
     * <p>
     * The three collaborators are passed in rather than stored, so a command holds only
     * what the user typed and can be created without knowing which task list or file it
     * will eventually act on.
     *
     * @param tasks   the task list to read or change
     * @param ui      used to tell the user what happened
     * @param storage used to save the list if this command changed it
     * @throws HarveyException if the instruction cannot be carried out as asked
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws HarveyException;

    /**
     * Returns whether Harvey should stop after this command.
     * Only {@link ExitCommand} overrides this, so the default is to keep going.
     *
     * @return true if this command ends the session
     */
    public boolean isExit() {
        return false;
    }
}
