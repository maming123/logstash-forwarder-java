package info.fetter.logstashforwarder.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

public class StdoutSection {
    @Override
    public String toString() {
        return new ToStringBuilder(this).
                toString();
    }
}
