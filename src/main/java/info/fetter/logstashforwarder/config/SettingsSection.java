package info.fetter.logstashforwarder.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

public class SettingsSection {
    @JsonProperty("cache_id_using_inode")
    private boolean usingInode = false;

    @JsonProperty("enabled_speed_limit")
    private boolean enabledSpeedLimit = true;

    public boolean isUsingInode() {
        return usingInode;
    }

    public void setUsingInode(boolean usingInode) {
        this.usingInode = usingInode;
    }

    public boolean getEnabledSpeedLimit() { return enabledSpeedLimit;}
    public void setEnabledSpeedLimit(boolean enabledSpeedLimit){ this.enabledSpeedLimit =enabledSpeedLimit; }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("usingInode", usingInode).
                append("enabledSpeedLimit", enabledSpeedLimit).
                toString();
    }
}
