package info.fetter.logstashforwarder.util;


/**
 * 封装返回的类
 *
 * @param <T>
 */
public class ReturnJsonType<T> {
    //解决json大小写
    //@JsonProperty
    private Integer resCode;
    //@JsonProperty
    private String resMsg;
    //@JsonProperty
    private T data;

    public Integer getResCode() {
        return resCode;
    }

    public void setResCode(Integer resCode) {
        this.resCode = resCode;
    }

    public String getResMsg() {
        return resMsg;
    }

    public void setResMsg(String resMsg) {
        this.resMsg = resMsg;
    }

    //@JsonIgnore
    public T getData() {
        return data;
    }

    //@JsonIgnore
    public void setData(T data) {
        this.data = data;
    }


    public ReturnJsonType(Integer resCode, String resMsg, T data) {
        this.resCode = resCode;
        this.resMsg = resMsg;
        this.data = data;
    }

    public ReturnJsonType(Integer resCode, String resMsg) {
        this.resCode = resCode;
        this.resMsg = resMsg;
    }

    public ReturnJsonType() {
    }


}

