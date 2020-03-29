package info.fetter.logstashforwarder.config;

import info.fetter.logstashforwarder.protocol.StdoutClient;
import org.apache.commons.lang.builder.ToStringBuilder;

public class OutputSection {
    private NetworkSection network;
    private StdoutSection stdout;
    private KafkaSection kafka;

    public KafkaSection getKafka() {
        return kafka;
    }

    public void setKafka(KafkaSection kafka) {
        this.kafka = kafka;
    }

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
                append("kafka",kafka).
                toString();
    }

}
