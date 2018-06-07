package com.nacai.settlement.api.entity.response;import java.io.Serializable;/** * Created by shan on 2018/5/29. */public class ResponseEntity<T> implements Serializable {    private static final long serialVersionUID = -8265746840464119980L;    /**     * 接口返回状态：SUCCESS:成功 FAIL:失败 UNKOWN:未知异常（出现此状态时建议查询）     * <p>     * 当且仅当 orderStatus 返回 SUCCESS 才表示此次接口调用成功     */    private String orderStatus;    /**     * 接口返回状态码 接口调用成功时，状态码为CG310000     * <p>     * 该状态码可用于查询对应错误信息     */    private String reCode;    /**     * 对应错误描述信息     */    private String reMsg;    /**     * 某些接口的响应数据     */    private T data;    public ResponseEntity() {    }    public ResponseEntity(String code,String msg) {        this.reCode = code;        this.reMsg = msg;    }    public ResponseEntity(ResponseInfoInterface responseInfoInterface) {        this.reCode = responseInfoInterface.getCode();        this.reMsg = responseInfoInterface.getMsg();    }    public ResponseEntity(ResponseStatusEnum statusEnum) {        this.orderStatus = statusEnum.getStatus();        this.reCode = statusEnum.getCode();        this.reMsg = statusEnum.getMsg();        this.data = null;    }    public String getOrderStatus() {        return orderStatus;    }    public void setOrderStatus(String orderStatus) {        this.orderStatus = orderStatus;    }    public String getReCode() {        return reCode;    }    public void setReCode(String reCode) {        this.reCode = reCode;    }    public String getReMsg() {        return reMsg;    }    public void setReMsg(String reMsg) {        this.reMsg = reMsg;    }    public T getData() {        return data;    }    public void setData(T data) {        this.data = data;    }    @Override    public String toString() {        return "ResponseEntity{" +                "orderStatus='" + orderStatus + '\'' +                ", reCode='" + reCode + '\'' +                ", reMsg='" + reMsg + '\'' +                ", data=" + (data == null ? null : data.toString()) +                '}';    }}