package uk.gov.justice.framework.tools.swarm.command;

import com.beust.jcommander.JCommander;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

import static uk.gov.justice.framework.tools.common.command.helper.JCommanderUtils.createInstanceAndAddToJCommander;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

public class SwarmCommandTest {

    @Test
    public void shouldFireAddSystemProperty() {

        final JCommander jCommander = new JCommander();
        String[] argv = { "execute", "-D", "java.system.property=test", "-D", "some.other.property=true"};
        createInstanceAndAddToJCommander(jCommander, Execute.class);

        jCommander.parse(argv);
        final String parsedCommand = jCommander.getParsedCommand();

        assertThat(parsedCommand, is("execute"));

        final SwarmCommand executedCommand = (SwarmCommand) jCommander.getCommands().get(parsedCommand).getObjects().get(0);
        assertThat(executedCommand.getAddedJavaSystemProperties(), hasItem("java.system.property"));
        assertThat(executedCommand.getJavaSystemPropertyValue("java.system.property"), is("test"));
        assertThat(executedCommand.getJavaSystemPropertyValue("some.other.property"), is("true"));

        assertThat(executedCommand.firedArgs().length, is(4));
        assertThat(executedCommand.firedArgs()[0], is("-D"));
        assertThat(executedCommand.firedArgs()[1], is("java.system.property=test"));
        assertThat(executedCommand.firedArgs()[2], is("-D"));
        assertThat(executedCommand.firedArgs()[3], is("some.other.property=true"));

    }

    @Test
    public void shouldFireAddSystemPropertyWithoutValue() {

        final JCommander jCommander = new JCommander();
        String[] argv = { "execute", "-d", "java.system.property"};
        createInstanceAndAddToJCommander(jCommander, Execute.class);

        jCommander.parse(argv);
        final String parsedCommand = jCommander.getParsedCommand();

        assertThat(parsedCommand, is("execute"));

        final SwarmCommand executedCommand = (SwarmCommand) jCommander.getCommands().get(parsedCommand).getObjects().get(0);
        assertThat(executedCommand.getSystemProperty(), is("java.system.property"));

        assertThat(executedCommand.firedArgs().length, is(2));
        assertThat(executedCommand.firedArgs()[0], is("-D"));
        assertThat(executedCommand.firedArgs()[1], is("java.system.property"));

    }

    @Test
    public void shouldFireAddPropertiesFile() {

        final JCommander jCommander = new JCommander();
        Path path = new File("path/to/file.properties").toPath();
        String[] argv = { "execute", "-P", path.toString()};
        createInstanceAndAddToJCommander(jCommander, Execute.class);

        jCommander.parse(argv);
        final String parsedCommand = jCommander.getParsedCommand();

        assertThat(parsedCommand, is("execute"));

        final SwarmCommand executedCommand = (SwarmCommand) jCommander.getCommands().get(parsedCommand).getObjects().get(0);
        assertThat(executedCommand.getPropertiesFileAsPath(), is(path));

        assertThat(executedCommand.firedArgs().length, is(2));
        assertThat(executedCommand.firedArgs()[0], is("-P"));
        assertThat(executedCommand.firedArgs()[1], is("path/to/file.properties"));

    }

    @Test
    public void shouldFireOnBindPublicListenersToAddress() {

        final JCommander jCommander = new JCommander();
        String[] argv = { "execute", "-b", "0.0.0.0:6000"};
        createInstanceAndAddToJCommander(jCommander, Execute.class);

        jCommander.parse(argv);
        final String parsedCommand = jCommander.getParsedCommand();

        assertThat(parsedCommand, is("execute"));

        final SwarmCommand executedCommand = (SwarmCommand) jCommander.getCommands().get(parsedCommand).getObjects().get(0);
        assertThat(executedCommand.getListenerBindAddress(), is("0.0.0.0:6000"));

        assertThat(executedCommand.firedArgs().length, is(2));
        assertThat(executedCommand.firedArgs()[0], is("-b"));
        assertThat(executedCommand.firedArgs()[1], is("0.0.0.0:6000"));

    }

    @Test
    public void shouldFireAddXmlConfigFile() {

        final JCommander jCommander = new JCommander();
        String[] argv = { "execute", "-c", "path/to/file.xml"};
        createInstanceAndAddToJCommander(jCommander, Execute.class);

        jCommander.parse(argv);
        final String parsedCommand = jCommander.getParsedCommand();

        assertThat(parsedCommand, is("execute"));

        final SwarmCommand executedCommand = (SwarmCommand) jCommander.getCommands().get(parsedCommand).getObjects().get(0);
        assertThat(executedCommand.getXmlConfigurationAsPath(), is("path/to/file.xml"));

        assertThat(executedCommand.firedArgs().length, is(2));
        assertThat(executedCommand.firedArgs()[0], is("-c"));
        assertThat(executedCommand.firedArgs()[1], is("path/to/file.xml"));

    }

    @Test
    public void shouldFireAddProjectStagesYaml() {

        final JCommander jCommander = new JCommander();
        Path path = new File("path/to/project-stages.yml").toPath();
        String[] argv = { "execute", "-s", path.toString()};
        createInstanceAndAddToJCommander(jCommander, Execute.class);

        jCommander.parse(argv);
        final String parsedCommand = jCommander.getParsedCommand();

        assertThat(parsedCommand, is("execute"));

        final SwarmCommand executedCommand = (SwarmCommand) jCommander.getCommands().get(parsedCommand).getObjects().get(0);
        assertThat(executedCommand.getProjectStagesYmlAsPath(), is(path));

        assertThat(executedCommand.firedArgs().length, is(2));
        assertThat(executedCommand.firedArgs()[0], is("-s"));
        assertThat(executedCommand.firedArgs()[1], is("path/to/project-stages.yml"));

    }

    @Test
    public void shouldFireOnActiveStageName() {

        final JCommander jCommander = new JCommander();
        String[] argv = { "execute", "-S", "development"};
        createInstanceAndAddToJCommander(jCommander, Execute.class);

        jCommander.parse(argv);
        final String parsedCommand = jCommander.getParsedCommand();

        assertThat(parsedCommand, is("execute"));

        final SwarmCommand executedCommand = (SwarmCommand) jCommander.getCommands().get(parsedCommand).getObjects().get(0);
        assertThat(executedCommand.getActiveStageName(), is("development"));

        assertThat(executedCommand.firedArgs().length, is(2));
        assertThat(executedCommand.firedArgs()[0], is("-S"));
        assertThat(executedCommand.firedArgs()[1], is("development"));

    }

    @Test
    public void shouldFireOnHelpDoubleHyphen() {

        final JCommander jCommander = new JCommander();
        String[] argv = { "execute", "--help"};
        createInstanceAndAddToJCommander(jCommander, Execute.class);

        jCommander.parse(argv);
        final String parsedCommand = jCommander.getParsedCommand();
        assertThat(parsedCommand, is("execute"));

        final SwarmCommand executedCommand = (SwarmCommand) jCommander.getCommands().get(parsedCommand).getObjects().get(0);
        assertThat(executedCommand.getHelpRequested(), is(true));

        assertThat(executedCommand.firedArgs().length, is(1));
        assertThat(executedCommand.firedArgs()[0], is("--help"));


    }

    @Test
    public void shouldFireOnHelpSingleH() {

        final JCommander jCommander = new JCommander();
        String[] argv = { "execute", "-h"};
        createInstanceAndAddToJCommander(jCommander, Execute.class);

        jCommander.parse(argv);
        final String parsedCommand = jCommander.getParsedCommand();
        assertThat(parsedCommand, is("execute"));

        final SwarmCommand executedCommand = (SwarmCommand) jCommander.getCommands().get(parsedCommand).getObjects().get(0);
        assertThat(executedCommand.getHelpRequested(), is(true));

        assertThat(executedCommand.firedArgs().length, is(1));
        assertThat(executedCommand.firedArgs()[0], is("--help"));

    }

    @Test
    public void shouldFireOnVersionDoubleHyphen() {

        final JCommander jCommander = new JCommander();
        String[] argv = { "execute", "--version"};
        createInstanceAndAddToJCommander(jCommander, Execute.class);

        jCommander.parse(argv);
        final String parsedCommand = jCommander.getParsedCommand();
        assertThat(parsedCommand, is("execute"));

        final SwarmCommand executedCommand = (SwarmCommand) jCommander.getCommands().get(parsedCommand).getObjects().get(0);
        assertThat(executedCommand.getVersionRequested(), is(true));

        assertThat(executedCommand.firedArgs().length, is(1));
        assertThat(executedCommand.firedArgs()[0], is("--version"));

    }

    @Test
    public void shouldFireOnVersionSingleV() {

        final JCommander jCommander = new JCommander();
        String[] argv = { "execute", "-v"};
        createInstanceAndAddToJCommander(jCommander, Execute.class);

        jCommander.parse(argv);
        final String parsedCommand = jCommander.getParsedCommand();
        assertThat(parsedCommand, is("execute"));

        final SwarmCommand executedCommand = (SwarmCommand) jCommander.getCommands().get(parsedCommand).getObjects().get(0);
        assertThat(executedCommand.getVersionRequested(), is(true));

        assertThat(executedCommand.firedArgs().length, is(1));
        assertThat(executedCommand.firedArgs()[0], is("--version"));

    }

    @Test
    public void shouldParseAndRebuildStringArray() {

        String[] initialArgs = {
            "execute",
                "-D",
                "java.system.property=test",
                "-D",
                "some.other.property=true",
                "-d",
                "java.system.property",
                "-P",
                "path/to/file.properties",
                "-b",
                "0.0.0.0:6000",
                "-c",
                "path/to/file.xml",
                "-s",
                "path/to/project-stages.yml",
                "-S",
                "development",
                "--help",
                "--version"
        };

        String[] expectedArgs = {
                "-D",
                "java.system.property=test",
                "-D",
                "some.other.property=true",
                "-D",
                "java.system.property",
                "-P",
                "path/to/file.properties",
                "-b",
                "0.0.0.0:6000",
                "-c",
                "path/to/file.xml",
                "-s",
                "path/to/project-stages.yml",
                "-S",
                "development",
                "--help",
                "--version"
        };

        final JCommander jCommander = new JCommander();
        createInstanceAndAddToJCommander(jCommander, Execute.class);

        jCommander.parse(initialArgs);
        final String parsedCommand = jCommander.getParsedCommand();
        assertThat(parsedCommand, is("execute"));

        final SwarmCommand executedCommand = (SwarmCommand) jCommander.getCommands().get(parsedCommand).getObjects().get(0);

        final String[] firedArgs = executedCommand.firedArgs();

        assertThat(firedArgs.length, is(18));
        assertThat(Arrays.asList(firedArgs), hasItems(expectedArgs));

    }
}

