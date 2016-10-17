package uk.gov.justice.framework.tools.swarm.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.wildfly.swarm.Swarm;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

import static java.lang.String.format;


@Parameters(separators = "=", commandDescription = "A basic swarm command")
public class SwarmCommand implements ShellCommand {


    @DynamicParameter(names = "-D", description = "define java system property")
    private Map<String, String> javaSystemProperty = new HashMap<>();

    @Parameter(names = "-P", converter = StringToPathConverter.class,
            description = "location of a Java .properties file to use as system properties")
    private Path propertiesFile;

    @Parameter(names = "-b", description = "bind the public listeners to an address")
    private String listenerBindAddress;

    @Parameter(names = "-c",
            description = "specify an XML configuration file (such as standalone.xml or a fragment of such)")
    protected String xmlConfigurationPath;

    @Parameter(names = "-s", converter = StringToPathConverter.class,
            description = "specify a project-stages.yml")
    private Path projectStagesYml;

    @Parameter(names = "-S", description = "name of stage to activate from a project-stages.yml file (see -s)")
    private String activeStageName;

    @Parameter(names = {"-h", "--help"}, description = "display relevant help, including any known project-stages.yml keys (see -s)")
    private boolean help;

    @Parameter(names = {"-v", "--version"}, description = "display the version of WildFly Swarm being used")
    private boolean versionRequested;

    @Parameter(names = "-d", description = "return value of a system property")
    private String requestedSystemProperty;

    /*
        If you intend to bootstrap in the jee container use the below
        and create a handler which observes your command as an event

        If you don't wish to use jee container then use this method to
        trigger your application, create an instance etc
     */
    public void run(String[] args) {
        try {
            new Swarm(firedArgs()).start().deploy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        This method is for passing CMD args which are part of swarm CMD api only
     */
    public String[] firedArgs() {

        List<String> preArgs = new ArrayList<>();

        javaSystemProperty.entrySet().stream()
                .forEach(entry -> {
                    preArgs.add("-D");
                    preArgs.add(format("%s=%s", entry.getKey(), entry.getValue()));
                });

        if(requestedSystemProperty != null) {
            preArgs.add("-D");
            preArgs.add(requestedSystemProperty);
        }

        if(propertiesFile != null) {
            preArgs.add("-P");
            preArgs.add(propertiesFile.toString());
        }

        if(listenerBindAddress != null) {
            preArgs.add("-b");
            preArgs.add(listenerBindAddress.toString());
        }

        if(xmlConfigurationPath != null) {
            preArgs.add("-c");
            preArgs.add(xmlConfigurationPath);
        }

        if(activeStageName != null) {
            preArgs.add("-S");
            preArgs.add(activeStageName);
        }

        if(projectStagesYml != null) {
            preArgs.add("-s");
            preArgs.add(projectStagesYml.toString());
        }

        if(help) {
            preArgs.add("--help");
        }

        if(versionRequested == true) {
            preArgs.add("--version");
        }

        return preArgs.toArray(new String[preArgs.size()]);
    }

    public String getJavaSystemPropertyValue(String property) {
        return javaSystemProperty.get(property);
    }

    public Set<String> getAddedJavaSystemProperties() {
        return javaSystemProperty.keySet();
    }

    public Path getPropertiesFileAsPath() {
        return propertiesFile;
    }

    public String getListenerBindAddress() {
        return listenerBindAddress;
    }

    public String getXmlConfigurationAsPath() {
        return xmlConfigurationPath;
    }

    public Path getProjectStagesYmlAsPath() {
        return projectStagesYml;
    }

    public String getActiveStageName() {
        return activeStageName;
    }

    public boolean getHelpRequested() {
        return help;
    }

    public boolean getVersionRequested() {
        return versionRequested;
    }

    public String getSystemProperty() {
        return requestedSystemProperty;
    };
}
