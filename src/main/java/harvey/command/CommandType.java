package harvey.command;

import harvey.HarveyException;
/**
 * The set of instructions Harvey understands, and how each one is written.
 * <p>
 * Each constant carries the keyword the user types and one correct example of the
 * command in use, so the keyword and its help text cannot drift apart. Using an enum
 * instead of separate {@code String} constants means the compiler knows the full list:
 * a misspelt {@code CommandType.DEADLIEN} will not compile, whereas a misspelt string
 * would silently never match.
 */
public enum CommandType {
    BYE("bye", "bye"),
    LIST("list", "list"),
    MARK("mark", "mark 2"),
    UNMARK("unmark", "unmark 2"),
    DELETE("delete", "delete 3"),
    TODO("todo", "todo borrow book"),
    DEADLINE("deadline", "deadline return book /by 2019-10-15"),
    EVENT("event", "event project meeting /from Mon 2pm /to 4pm");

    /** The word the user types to invoke this command. */
    private final String keyword;

    /** A correctly formed use of this command, shown to the user after a mistake. */
    private final String example;

    /**
     * Creates a command. Enum constructors are always private: the constants listed
     * above are the only instances that will ever exist.
     *
     * @param keyword the word the user types.
     * @param example a correct use of the command.
     */
    CommandType(String keyword, String example) {
        this.keyword = keyword;
        this.example = example;
    }

    /**
     * Returns the word the user types to invoke this command.
     *
     * @return the keyword, e.g. {@code delete}.
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Returns a correct use of this command, for showing the user how to fix a mistake.
     *
     * @return one line the user could type, e.g. {@code delete 3}.
     */
    public String getExample() {
        return example;
    }

    /**
     * Finds the command a keyword refers to.
     *
     * @param keyword the first word the user typed.
     * @return the matching command.
     * @throws HarveyException if no command uses that keyword.
     */
    public static CommandType fromKeyword(String keyword) throws HarveyException {
        // values() returns every constant declared above, so this loop automatically
        // covers any command added later.
        for (CommandType command : values()) {
            if (command.keyword.equals(keyword)) {
                return command;
            }
        }

        if (keyword.isEmpty()) {
            throw new HarveyException("You did not type anything. " + listKeywords());
        }
        throw new HarveyException("I don't recognise the command \"" + keyword + "\". " + listKeywords());
    }

    /**
     * Lists every keyword Harvey understands, for use in error messages.
     *
     * @return a sentence naming all the commands.
     */
    public static String listKeywords() {
        StringBuilder keywords = new StringBuilder("I understand: ");
        for (int i = 0; i < values().length; i++) {
            if (i > 0) {
                keywords.append(", ");
            }
            keywords.append(values()[i].keyword);
        }
        return keywords.append('.').toString();
    }
}
