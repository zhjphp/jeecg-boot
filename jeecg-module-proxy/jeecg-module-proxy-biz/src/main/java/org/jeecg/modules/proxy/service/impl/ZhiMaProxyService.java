package org.jeecg.modules.proxy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.proxy.service.IIPProxyService;
import org.jeecg.modules.proxy.vo.IPProxyVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @version 1.0
 * @description: 芝麻代理服务 https://www.zmhttp.com/
 * @author: wayne
 * @date 2023/6/1 22:16
 */
@Slf4j
@RefreshScope
@Service(value = "ZhiMaProxyService")
public class ZhiMaProxyService implements IIPProxyService {

    @Value("${ip-proxy.zhima-proxy.exclusive-type-api}")
    private String exclusiveApi;

    @Value("${ip-proxy.zhima-proxy.tunnel-type-api}")
    private String tunnelApi;

    private OkHttpClient okHttpClient = new OkHttpClient();

    private static String scheme = "http";

    /**
     * 获取一个IP
     *
     * @param type IP类型
     * @return
     * @throws Exception
     */
    @Override
    public IPProxyVO getOne(int type) throws Exception {
        IPProxyVO ipProxyVO = new IPProxyVO();
        JSONArray resultArray = requestExclusiveApi(type, 1);
        JSONObject result = resultArray.getJSONObject(0);
        ipProxyVO.setIp(result.getString("ip"));
        ipProxyVO.setPort(result.getInteger("port"));
        ipProxyVO.setExpireTime(result.getDate("expire_time"));
        ipProxyVO.setScheme(scheme);
        return ipProxyVO;
    }

    /**
     * 请求独享IP接口
     *
     * @param type ip类型
     * @param num ip数量
     * @return
     * @throws Exception
     */
    public JSONArray requestExclusiveApi(int type, int num) throws Exception {
        tunnelApi = tunnelApi + "&num=" + String.valueOf(num);
        log.info("ZhiMa API: {}", tunnelApi);
        Request request = new Request.Builder().url(exclusiveApi).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            String result = response.body().string();
            log.info("ZhiMa response: {}", result);
            return getResponseData(result);
        } catch (Exception e) {
            log.error("获取ZhiMa代理IP失败: {}, {}", e.getMessage(), e.toString());
            throw new Exception("获取ZhiMa代理IP失败: {}" + e.getMessage());
        }
    }

    /**
     * 处理相应数据
     *
     * @param response 响应数据
     * @return
     * @throws Exception
     */
    public JSONArray getResponseData(String response) throws Exception {

        JSONObject resultObj = JSON.parseObject(response);
        if ( (resultObj.getInteger("code") != 0) && (resultObj.getBoolean("success").equals(false)) ) {
            log.error("获取ZhiMa代理IP失败: {}", response);
            throw new Exception("获取ZhiMa代理IP失败: " + response);
        }
        JSONArray result =  resultObj.getJSONArray("data");
        if ( oConvertUtils.isEmpty(result) || result.size() < 1 ) {
            log.error("获取ZhiMa代理IP失败: {}", response);
            throw new Exception("获取ZhiMa代理IP失败: " + response);
        }
        return result;
    }

}
