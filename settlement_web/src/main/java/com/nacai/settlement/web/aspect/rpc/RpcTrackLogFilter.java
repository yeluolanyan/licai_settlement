package com.nacai.settlement.web.aspect.rpc;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Description: dubbo服务请求响应日志
 * Created by shan on 2018/5/29.
 */
@Activate(group = {Constants.CONSUMER, Constants.PROVIDER})
public class RpcTrackLogFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(RpcTrackLogFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        long start = System.currentTimeMillis();
        Result result = null;
        String traceId = getTraceId(invocation);
        String serviceSide = getServiceSide();
        String interfaceName = invoker.getInterface().getSimpleName();
        try {
            logger.info("{} consumer dubbo {}#{}({}) - {}", new Object[]{serviceSide, interfaceName, invocation.getMethodName(), JSON.toJSONString(invocation.getArguments()), traceId});
            result = invoker.invoke(invocation);
        } catch (RpcException ex) {
            logger.warn("{} consumer dubbo服务异常 - {})", serviceSide, traceId);
            throw ex;
        } finally {
            long milis = System.currentTimeMillis() - start;
            if (result != null && result.hasException()) {
                logger.error("{} consumer dubbo响应：{}, 耗时：{}ms - {}", new Object[]{serviceSide, result.getException(), milis, traceId});
            } else {
                String resultStr = JSON.toJSONString(result == null ? "" : result.getValue());
                if (resultStr.length() > 200) {
                    resultStr = resultStr.substring(0, 200);
                }
                logger.info("{} consumer dubbo响应：{}, 耗时：{}ms - {}", new Object[]{serviceSide, resultStr, milis, traceId});
            }
        }
        return result;
    }

    private String getTraceId(Invocation invocation) {
        if (RpcTrackHolder.getTrackId() != null && !"".equals(RpcTrackHolder.getTrackId().trim())) {
            RpcContext.getContext().setAttachment("trackId", RpcTrackHolder.getTrackId());
            return RpcTrackHolder.getTrackId();
        }
        String traceId = invocation.getAttachment("trackId");
        if (traceId == null || "".equals(traceId.trim())) {
            traceId = UUID.randomUUID().toString();
        }
        RpcTrackHolder.setTrackId(traceId);
        RpcContext.getContext().getAttachments().put("trackId", traceId);
        return traceId;
    }

    private String getServiceSide() {
        String side = RpcContext.getContext().getUrl().getParameter("side");
        if ("consumer".equals(side)) return "调用下游";
        return "####";
    }
}
