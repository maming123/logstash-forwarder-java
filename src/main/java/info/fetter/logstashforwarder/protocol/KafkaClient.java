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

    public KafkaClient(String hosts,String topic,Integer keepAlive,String charset) {

        if(StringUtils.isNotBlank(charset))
        {
            this.charset=charset;
        }
        this.topic =topic;
        Properties props = new Properties();
        props.put("bootstrap.servers", hosts);//xxx服务器ip
        props.put("acks", "all");//所有follower都响应了才认为消息提交成功，即"committed"
        props.put("retries", 0);//retries = MAX 无限重试，直到你意识到出现了问题:)
        //props.put("batch.size", 16384);//producer将试图批处理消息记录，以减少请求次数.默认的批量处理消息字节数
        //batch.size当批量的数据大小达到设定值后，就会立即发送，不顾下面的linger.ms
        props.put("linger.ms", 1);//延迟1ms发送，这项设置将通过增加小的延迟来完成--即，不是立即发送一条记录，producer将会等待给定的延迟时间以允许其他消息记录发送，这些消息记录可以批量处理
        props.put("buffer.memory", 33554432);//producer可以用来缓存数据的内存大小。
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
        System.out.println(json);
        try {
            producer.send(new ProducerRecord<String, String>(topic, json));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
        System.out.flush();
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
                logger.info("Sending " + numberOfEvents + " events");
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