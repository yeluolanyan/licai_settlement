package com.nacai.settlement.web.aspect.rpc;


/**
 * Description: RPC调用轨迹id
 * Created by shan on 2018/5/29.
 */
public class RpcTrackHolder {

    // 调用链id
    private static final ThreadLocal<String> trackIdLocal = new ThreadLocal<String>();

    public static void setTrackId(String trackId) {
        trackIdLocal.set(trackId);
    }

    public static String getTrackId() {
        return trackIdLocal.get();
    }

}
