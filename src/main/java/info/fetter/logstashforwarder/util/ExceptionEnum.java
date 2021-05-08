package info.fetter.logstashforwarder.util;


/**
 * 异常枚举类
 */
public enum ExceptionEnum {
    SUCESS(0,"成功"),
    FAIL(1,"失败")
    ;

    private Integer Status;

    private String Message;

    ExceptionEnum(Integer status, String message) {
        this.Status = status;
        this.Message = message;
    }

    public Integer getStatus() {
        return Status;
    }

    public String getMessage() {
        return Message;
    }
}
