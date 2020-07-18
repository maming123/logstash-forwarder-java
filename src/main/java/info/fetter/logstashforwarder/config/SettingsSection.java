package info.fetter.logstashforwarder.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

public class SettingsSection {
    @JsonProperty("cache_id_using_inode")
    private boolean usingInode = false;

    public boolean isUsingInode() {
        return usingInode;
    }

    public void setUsingInode(boolean usingInode) {
        this.usingInode = usingInode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("usingInode", usingInode).
                toString();
    }
}
