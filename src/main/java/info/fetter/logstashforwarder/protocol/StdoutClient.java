package info.fetter.logstashforwarder.protocol;

import info.fetter.logstashforwarder.Event;
import info.fetter.logstashforwarder.ProtocolAdapter;
import info.fetter.logstashforwarder.util.AdapterException;
import info.fetter.logstashforwarder.util.GetUTCTimeUtil;
import info.fetter.logstashforwarder.util.JsonHelper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StdoutClient implements ProtocolAdapter {
    private final static Logger logger = Logger.getLogger(StdoutClient.class);

    public StdoutClient() {
    }

    private int sendData(Map<String,byte[]> keyValues) throws IOException {
        int bytesSent = 0;
        System.out.print("stdout adapter: ");
        for(String key : keyValues.keySet()) {
            byte[] value = keyValues.get(key);
            System.out.print(new String(value));
            bytesSent += value.length;
        }
        System.out.println();
        System.out.flush();
        return bytesSent;
    }

    private int sendJsonData(Map<String, byte[]> keyValues) throws IOException {
        int bytesSent = 0;
        System.out.print("stdout adapter: ");
        //自定义map 添加@version @timestamp
        Map<String, String> customMap = new HashMap<String, String>();
        for (String key : keyValues.keySet()) {
            byte[] value = keyValues.get(key);
            customMap.put(key, new String(value));
            bytesSent += value.length;
        }
        //map转换成json
        customMap.put("@timestamp", GetUTCTimeUtil.getUTCTimeStr());
        String json = JsonHelper.pureToJson(customMap);

        System.out.println(json);
        System.out.flush();
        return bytesSent;
    }

    public int sendFrame(List<Map<String,byte[]>> keyValuesList) throws IOException {
        int bytesSent = 0;
        for(Map<String,byte[]> keyValues : keyValuesList) {
            logger.trace("Adding data frame");
            //bytesSent += sendData(keyValues);
            bytesSent += sendJsonData(keyValues);
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
