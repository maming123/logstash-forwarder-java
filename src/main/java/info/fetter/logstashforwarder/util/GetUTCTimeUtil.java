package info.fetter.logstashforwarder.util;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Javen
 *
 */
public final class GetUTCTimeUtil {

    private  DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;

    /**
     * 得到UTC时间，类型为字符串，格式为"yyyy-MM-dd HH:mm:ss"<br />
     * 如果获取失败，返回null
     * @return
     */
    public static String getUTCTimeStr() {
        //当前时间Date
        Date now = new Date();
        //System.out.println(now);
        //Wed Jan 31 23:32:03 GMT+08:00 2018

        //例如我的环境时区为：(UTC+08:00)北京，重庆，香港特别行政区，乌鲁木齐（+0800）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        //System.out.println(sdf.getTimeZone());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        //System.out.println(sdf.format(now));
        return sdf.format(now);
    }

    /**
     * 将UTC时间转换为东八区时间
     * @param UTCTime
     * @return
     */
    public static String getLocalTimeFromUTC(String UTCTime) throws ParseException {
        String utcTime =UTCTime;// "2018-01-31T14:32:19Z";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//设置时区UTC
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
//格式化，转当地时区时间
        Date after = df.parse(utcTime);
        System.out.println(after);
//Wed Jan 31 22:32:19 GMT+08:00 2018
        df.applyPattern("yyyy-MM-dd HH:mm:ss");
//默认时区
        df.setTimeZone(TimeZone.getDefault());
        System.out.println(df.format(after));
//2018-01-31 22:32:19
        return df.format(after);
    }

    public static void main(String[] args) throws ParseException {
        String UTCTimeStr = getUTCTimeStr() ;
        System.out.println(UTCTimeStr);
        System.out.println(getLocalTimeFromUTC(UTCTimeStr));
    }

}