package info.fetter.logstashforwarder.config;

import info.fetter.logstashforwarder.protocol.StdoutClient;
import org.apache.commons.lang.builder.ToStringBuilder;

public class OutputSection {
    private NetworkSection network;
    private StdoutSection stdout;

    public NetworkSection getNetwork() {
        return network;
    }

    public void setNetwork(NetworkSection network) {
        this.network = network;
    }

    public StdoutSection getStdout() {
        return stdout;
    }

    public void setStdout(StdoutSection stdout) {
        this.stdout = stdout;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("network", network).
                append("stdout", stdout).
                toString();
    }

}
