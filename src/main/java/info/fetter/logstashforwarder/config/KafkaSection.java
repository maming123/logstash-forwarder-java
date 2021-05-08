package info.fetter.logstashforwarder.config;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author maming
 * 添加kafka输出配置节点
 * add 2020-03-29
 */
public class KafkaSection {

    @JsonProperty("hosts")
    private List<String> hosts;

    @JsonProperty("topic")
    private String topic;

    /**
     * //所有follower都响应了才认为消息提交成功，即"committed",ack是判别请求是否为完整的条件。我们指定了“all”将会阻塞消息，这种设置使性能最低，但是是最可靠的
     * //参数设置成0，producer端不确定消息是否发送成功，只是发出去，并不等待broker返回响应，数据可能丢失，但是优势是对于吞吐量高，不要求保证完整一致性的需求来说（比如日志处理），这是好的方式,
     * //参数设置成-1或者all，producer会在所有备份的partition收到消息时得到broker的确认，这个设置可以得到最高的可靠性保证
     */
    @JsonProperty("acks")
    private String acks;
    /**
     * //retries = MAX 无限重试，直到你意识到出现了问题:) 如果请求失败，生产者会自动重试，我们指定是0次即不启动重试，如果启用重试，则会有重复消息的可能性。
     */
    @JsonProperty("retries")
    private Integer retries;
    /**
     * //producer将试图批处理消息记录，以减少请求次数(16384/1024=16KB).默认的批量处理消息字节数 kafka发送端批量发送的的缓存大小，默认是16kB。这个参数一般和kafka.producer.linger配合使用,batch.size小，吞吐量低，延时低，kafka.producer.batch.size大，吞吐量高，延时高
     */
    @JsonProperty("batch_size")
    private Integer batchSize;
    /**
     * //batch.size当批量的数据大小达到设定值后，就会立即发送，不顾下面的linger.ms
     * //延迟1ms发送，这项设置将通过增加小的延迟来完成--即，不是立即发送一条记录，producer将会等待给定的延迟时间以允许其他消息记录发送，这些消息记录可以批量处理
     */
    @JsonProperty("linger_ms")
    private Integer lingerMs;

    /**
     * //producer可以用来缓存数据的内存大小。33554432/(1024*1024)=32M,指定producer端用于缓存消息的缓冲区的大小，单位是字节，默认32M，采用异步发送消息的架构，Java版Producer启动时会首先创建一块内存缓冲区用于保存待发送消息，然后由另一个专属线程负责从缓冲区中读取消息执行真正的发送，这部分内存空间的大小就是由buffer.memory参数指定。该参数指定的内存大小几乎可以认为是producer程序使用的内存大小，若producer程序要给很多分区发送消息，那么就需要仔细设置该参数防止过小的内存缓冲区降低了producer程序整体的吞吐量 链接：https://www.jianshu.com/p/17ef8ab711e9
     */
    @JsonProperty("buffer_memory")
    private Integer bufferMemory;

    @JsonProperty("charset")
    private String charset;


    /**
     * 0：不输出 1：输出
     */
    private Integer output2kafka;

    public Integer getOutput2kafka() {
        return output2kafka;
    }

    public void setOutput2kafka(Integer output2kafka) {
        this.output2kafka = output2kafka;
    }

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


    public String getAcks() {
        return acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Integer getLingerMs() {
        return lingerMs;
    }

    public void setLingerMs(Integer lingerMs) {
        this.lingerMs = lingerMs;
    }

    public Integer getBufferMemory() {
        return bufferMemory;
    }

    public void setBufferMemory(Integer bufferMemory) {
        this.bufferMemory = bufferMemory;
    }
}
