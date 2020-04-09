package info.fetter.logstashforwarder.config;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author maming
 * 添加kafka输出配置节点
 * add 2020-03-29
 */
public class KafkaSection {

    private List<String> hosts;

    private String topic;

    private int keep_alive;

    private String charset;

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }




    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getKeep_alive() {
        return keep_alive;
    }

    public void setKeep_alive(int keep_alive) {
        this.keep_alive = keep_alive;
    }



}
