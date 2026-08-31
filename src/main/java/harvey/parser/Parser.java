package harvey.parser;

import harvey.HarveyException;
import harvey.command.AddCommand;
import harvey.command.Command;
import harvey.command.CommandType;
import harvey.command.DeleteCommand;
import harvey.command.ExitCommand;
import harvey.command.FindCommand;
import harvey.command.ListCommand;
import harvey.command.MarkCommand;
import harvey.command.UnmarkCommand;
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
     * Turns one typed line into the command it asks for.
     * <p>
     * This is the only method the main loop needs: it decides which subclass of
     * {@link Command} the line means, and hands back something that knows how to carry
     * itself out. The loop therefore never names the individual instructions.
     *
     * @param fullCommand one line as the user typed it, already trimmed.
     * @return the command that line asks for.
     * @throws HarveyException if the line does not name a command Harvey knows.
     */
    public static Command parse(String fullCommand) throws HarveyException {
        CommandType type = parseCommandType(fullCommand);
        String argument = parseArgument(fullCommand);

        switch (type) {
            case BYE:
                return new ExitCommand();
            case LIST:
                return new ListCommand();
            case FIND:
                return new FindCommand(argument);
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
     * @param input one line as the user typed it, already trimmed.
     * @return the command that line invokes.
     * @throws HarveyException if the first word is not a command Harvey knows.
     */
    private static CommandType parseCommandType(String input) throws HarveyException {
        return CommandType.fromKeyword(splitOffKeyword(input)[0]);
    }

    /**
     * Returns everything the user typed after the command word.
     *
     * @param input one line as the user typed it, already trimmed.
     * @return the argument, or an empty string if there was none.
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
     * @param input one line as the user typed it.
     * @return an array of one or two parts.
     */
    private static String[] splitOffKeyword(String input) {
        return input.split(" ", 2);
    }

}
