# Project context

This repository is a starter template for a greenfield Java project used in an introductory software engineering course in an undergraduate computer science program. Students use it as the starting point for their own projects.

# Default user context

Unless the user says otherwise, assume that you are assisting a student working on a project in this repository. If the user identifies themselves as an instructor or another project stakeholder, adapt your response to that role.

# Student profile

* Prior knowledge: Basic Java and OOP concepts.
* Level of programming experience: basic
* IDE and level of expertise: basic

# Guidance for interacting with users

* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through responsible use of AI. For example:

  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments to all classes and to nontrivial methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Git

Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.

## Testing

JUnit 5 tests live in `src/test/java`, mirroring the package of the class under test
(e.g. `harvey.task.TaskList` is tested by `src/test/java/harvey/task/TaskListTest.java`).
Run them with `./gradlew test`; `./gradlew build` runs them too.

Name test methods `featureUnderTest_testScenario_expectedBehavior()`, e.g.
`get_taskNumberJustPastEnd_exceptionThrown()`.

**Coverage target: roughly the top 50% highest-value methods** — the complex, core or
critical ones, rather than an even spread. Trivial getters and `toString` methods on
simple classes are not worth testing; parsing, task numbering, and anything that reads or
writes the save file are.

**Update the JUnit tests as part of any code change**, in the same commit, so the project
stays at that target. A change that adds or alters non-trivial behaviour should add or
alter the tests that cover it; a change that removes behaviour should remove its tests.
