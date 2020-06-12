package info.fetter.logstashforwarder.protocol;

import info.fetter.logstashforwarder.Event;
import info.fetter.logstashforwarder.ProtocolAdapter;
import info.fetter.logstashforwarder.util.AdapterException;
import info.fetter.logstashforwarder.util.GetUTCTimeUtil;
import info.fetter.logstashforwarder.util.JsonHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class KafkaClient implements ProtocolAdapter {
    private final static Logger logger = Logger.getLogger(KafkaClient.class);

    private final KafkaProducer<String, String> producer;

    private String topic = "";
    private String charset="UTF-8";
    private Integer output2kafka=0;

    public KafkaClient(String hosts,String topic,String acks,Integer retries,Integer batchSize
            ,Integer lingerMs,Integer bufferMemory,String charset,Integer output2kafka) {

        if(StringUtils.isNotBlank(charset))
        {
            this.charset=charset;
        }
        this.topic =topic;
        this.output2kafka=output2kafka;
        Properties props = new Properties();
        props.put("bootstrap.servers", hosts);//xxx服务器ip
        //所有follower都响应了才认为消息提交成功，即"committed",ack是判别请求是否为完整的条件。我们指定了“all”将会阻塞消息，这种设置使性能最低，但是是最可靠的
        //参数设置成0，producer端不确定消息是否发送成功，只是发出去，并不等待broker返回响应，数据可能丢失，但是优势是对于吞吐量高，不要求保证完整一致性的需求来说（比如日志处理），这是好的方式,
        //参数设置成-1或者all，producer会在所有备份的partition收到消息时得到broker的确认，这个设置可以得到最高的可靠性保证
        props.put("acks",acks );//"all"
        //retries = MAX 无限重试，直到你意识到出现了问题:) 如果请求失败，生产者会自动重试，我们指定是0次即不启动重试，如果启用重试，则会有重复消息的可能性。
        props.put("retries",retries); //0
        //producer将试图批处理消息记录，以减少请求次数(16384/1024=16KB).默认的批量处理消息字节数 kafka发送端批量发送的的缓存大小，默认是16kB。这个参数一般和kafka.producer.linger配合使用,batch.size小，吞吐量低，延时低，kafka.producer.batch.size大，吞吐量高，延时高
        props.put("batch.size",batchSize );//16384
        //batch.size当批量的数据大小达到设定值后，就会立即发送，不顾下面的linger.ms
        //延迟1ms发送，这项设置将通过增加小的延迟来完成--即，不是立即发送一条记录，producer将会等待给定的延迟时间以允许其他消息记录发送，这些消息记录可以批量处理
        props.put("linger.ms", lingerMs);//1
        //producer可以用来缓存数据的内存大小。33554432/(1024*1024)=32M,指定producer端用于缓存消息的缓冲区的大小，单位是字节，默认32M，采用异步发送消息的架构，Java版Producer启动时会首先创建一块内存缓冲区用于保存待发送消息，然后由另一个专属线程负责从缓冲区中读取消息执行真正的发送，这部分内存空间的大小就是由buffer.memory参数指定。该参数指定的内存大小几乎可以认为是producer程序使用的内存大小，若producer程序要给很多分区发送消息，那么就需要仔细设置该参数防止过小的内存缓冲区降低了producer程序整体的吞吐量 链接：https://www.jianshu.com/p/17ef8ab711e9
        props.put("buffer.memory", bufferMemory);//33554432
        //当producer发送请求给broker后，broker需要在规定时间范围内将处理结果返回给producer。超时时间默认30s
        props.put("request.timeout.ms", 30000);
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<String, String>(props);
    }

    private int sendData(Map<String,byte[]> keyValues) throws IOException {
        int bytesSent = 0;
        //自定义map 添加@version @timestamp
        Map<String,String> customMap =new HashMap<String, String>();
        for(String key : keyValues.keySet()) {
            byte[] value = keyValues.get(key);
            //System.out.print(" "+key+" - "+ new String(value));
            String data = new String(value);
            //customMap.put(key,new String(value));
            // "GBK"
            customMap.put(key,new String(value,charset));
            bytesSent += value.length;
        }
        //map转换成json
        //customMap.put("@version","1");
        customMap.put("@timestamp", GetUTCTimeUtil.getUTCTimeStr());
        String json =JsonHelper.pureToJson(customMap);
        //String json2 = new String(json.getBytes("GBK"),"utf-8");
        //System.out.println(Charset.defaultCharset().toString());
        //System.out.println(json);
        try {
            //send()方法是异步的，添加消息到缓冲区等待发送，并立即返回。这允许生产者将单个的消息批量在一起发送来提高效率
            if(output2kafka.equals(1)) {
                producer.send(new ProducerRecord<String, String>(topic, json));
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
        //System.out.println();
        //System.out.flush();
        return bytesSent;
    }

    public int sendFrame(List<Map<String,byte[]>> keyValuesList) throws IOException {
        int bytesSent = 0;
        for(Map<String,byte[]> keyValues : keyValuesList) {
            logger.trace("Adding data frame");
            bytesSent += sendData(keyValues);
        }
        return bytesSent;
    }

    public int sendEvents(List<Event> eventList) throws AdapterException {
        try {
            int numberOfEvents = eventList.size();
            if(logger.isInfoEnabled()) {

                if(null!=eventList && eventList.size()>0) {
                    String fileName="";
                    List<String> list =new ArrayList<>();
                    for(int i=0;i<numberOfEvents;i++) {
                        byte[] fileNameByte = eventList.get(i).getValue("file");
                        fileName = new String(fileNameByte, charset);
                        if(!list.contains(fileName)){
                            list.add(fileName);
                        }
                    }
                        fileName =String.join(",",list);
                        logger.info("file: " + fileName + " Sending " + numberOfEvents + " events");
                        System.out.println(GetUTCTimeUtil.getUTCTimeStr() + " file: " + fileName + " Sending " + numberOfEvents + " events");
                    }
                }

            List<Map<String,byte[]>> keyValuesList = new ArrayList<Map<String,byte[]>>(numberOfEvents);
            for(Event event : eventList) {
                keyValuesList.add(event.getKeyValues());
            }
            return sendFrame(keyValuesList);
        } catch(Exception e) {
            throw new AdapterException(e);
        }
    }

    public void close() throws AdapterException {

    }

    public String getServer() {
        return null;
    }

    public int getPort() {
        return 0;
    }
}
