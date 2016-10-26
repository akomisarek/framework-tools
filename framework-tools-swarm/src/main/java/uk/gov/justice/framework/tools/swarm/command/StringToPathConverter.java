package uk.gov.justice.framework.tools.swarm.command;

import com.beust.jcommander.IStringConverter;

import java.io.File;
import java.nio.file.Path;

public class StringToPathConverter implements IStringConverter<Path> {
    @Override
    public Path convert(String value) {
        return new File(value).toPath();
    }
}
