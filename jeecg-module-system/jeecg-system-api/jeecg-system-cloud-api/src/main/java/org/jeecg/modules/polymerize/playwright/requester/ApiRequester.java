package org.jeecg.modules.polymerize.playwright.requester;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.playwright.util.OkHttpUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @version 1.0
 * @description: Api请求器
 * @author: wayne
 * @date 2023/9/7 11:27
 */
@Slf4j
@Component
public class ApiRequester {

    // @Autowired
    private OkHttpClient okHttpClient = OkHttpUtil.getOkHttpClient();

    public enum ValueType {
        STRING("string"),
        INT("int"),
        FLOAT("float"),
        BOOLEAN("boolean");
        public final String typeName;
        ValueType(String typeName) { this.typeName = typeName; }
    }

    public String request(String method, String url, String contentType, String headers, String bodies, String urlParams) throws Exception {
        switch (method) {
            case "get":
                return get(url, headers, bodies, urlParams);
            case "post":
                return post(url, contentType, headers, bodies, urlParams);
            default:
                throw new RuntimeException("不支持的请求method: " + method);
        }
    }

    /**
     * get
     */
    public String get(String url, String headers, String bodies, String urlParams) throws Exception {
        Request.Builder requestBuilder = new Request.Builder();
        Headers requestHeaders = makeHeadersBuilder(makeHeadersMap(headers));
        // 请求参数
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        Map<String, String> bodiesMap = makeBodiesMap(bodies);
        Map<String, String> urlParamsMap = makeBodiesMap(urlParams);
        Map<String, String> allParams = Stream.of(bodiesMap, urlParamsMap).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        log.info("allParams: {}", allParams);
        url = makeUrlParamsString(allParams, url);

        log.info("get url: {}", url);
        Request request = new Request.Builder().url(url).get().headers(requestHeaders).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            String result = response.body().string();
            log.info("请求地址:{}, 请求结果:{}", url, result);
            return result;
        } catch (Exception e) {
            log.info(e.toString());
            throw new Exception("请求地址: " + url + ", 请求异常: " + ExceptionUtil.stacktraceToOneLineString(e));
        }
    }

    /**
     * post
     */
    public String post(String url, String contentType, String headers, String bodies, String urlParams) throws Exception {
        Map<String, String> urlParamsMap = makeBodiesMap(urlParams);
        url = makeUrlParamsString(urlParamsMap, url);
        Headers requestHeaders = makeHeadersBuilder(makeHeadersMap(headers));
        Map<String, String> bodiesMap = makeBodiesMap(bodies);
        RequestBody requestBody = makeRequestBodies(contentType, bodiesMap);
        Request.Builder requestBuilder = new Request.Builder();
        Request request = requestBuilder.url(url).headers(requestHeaders).post(requestBody).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            String result = response.body().string();
            log.info("请求地址:{}, 请求结果:{}", url, result);
            return result;
        } catch (Exception e) {
            throw new Exception("请求地址: " + url + ", 请求异常: " + ExceptionUtil.stacktraceToOneLineString(e));
        }
    }

    public String makeUrlParamsString(Map<String, String> urlParamsMap, String url) {
        // 判断url中是否有"#"
        Boolean jingFlag = false;
        if (url.contains("#")) {
            // HttpUrl.Builder无法处理"#",如果包含"#",替换为*字符
            url = url.replace("#", "*");
            jingFlag = true;
        }
        Request.Builder requestBuilder = new Request.Builder();
        // 请求参数
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (String key : urlParamsMap.keySet()) {
            String[] arr = key.split("->");
            String name = arr[0];
            // 默认值为string
            String value = urlParamsMap.get(key);
            if (oConvertUtils.isNotEmpty(value)) {
                urlBuilder.addQueryParameter(name, value);
                log.info("makeUrlParamsString urlBuilder add: name={}, value={}", name, value);
            }
        }
        String newUrl = urlBuilder.build().toString();
        if (jingFlag) {
            // 把"#"与"*"替换回来
            newUrl = newUrl.replace("*", "#");
        }
        log.info("makeUrlParamsString: url={}", newUrl);
        return newUrl;
    }

    public Map<String, String> makeUrlParamsMap(String urlParam) {
        Map<String, String> urlParamsMap = new HashMap<String, String>();
        if (oConvertUtils.isNotEmpty(urlParam)) {
            // 根据换行符拆分header
            List<String> urlParamsList = Arrays.stream(urlParam.split("\n")).collect(Collectors.toList());
            // 将headerList转换为map
            for (String statement : urlParamsList) {
                // 按"="拆分字符串
                String[] tmp = statement.split("=");
                // 只有定义了完整name和value的语句才会被解析
                if (tmp.length == 2) {
                    String k = tmp[0];
                    String v = tmp[1];
                    urlParamsMap.put(k, v);
                }
            }
        }
        log.info(urlParamsMap.toString());

        return urlParamsMap;
    }

    /**
     * 构建okHttp的请求body
     * 只有json请求数据才会进行数据类型转换
     * 格式
     * param->type=some_string
     */
    public RequestBody makeRequestBodies(String contentType, Map<String, String> bodiesMap) throws Exception {
        RequestBody requestBody = null;
        if (oConvertUtils.isEmpty(contentType)) {
            throw new RuntimeException("contentType 为空");
        }
        // 根据不同ContentType生成请求body
        switch (contentType) {
            case "application/x-www-form-urlencoded":
                FormBody.Builder formBodyBuilder = new FormBody.Builder();
                for (String key : bodiesMap.keySet()) {
                    String[] arr = key.split("->");
                    String name = arr[0];
                    // 默认值为string
                    String value = bodiesMap.get(key);
                    if (oConvertUtils.isNotEmpty(value)) {
                        formBodyBuilder.add(name, value);
                    }
                }
                requestBody = formBodyBuilder.build();
                break;
            case "multipart/form-data":
                MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
                for (String key : bodiesMap.keySet()) {
                    String[] arr = key.split("->");
                    String name = arr[0];
                    // 默认值为string
                    String value = bodiesMap.get(key);
                    if (oConvertUtils.isNotEmpty(value)) {
                        multipartBodyBuilder.addFormDataPart(name, value);
                    }
                }
                requestBody = multipartBodyBuilder.build();
                break;
            case "application/json;charset=utf-8":
                // 准备转为json格式的map
                Map<String, Object> newMap = new HashMap<>();
                for (String key : bodiesMap.keySet()) {
                    String[] arr = key.split("->");
                    String name = arr[0];
                    // 默认值为string
                    String value = bodiesMap.get(key);
                    // 如果定义了类型
                    if (arr.length > 1) {
                        String type = arr[1];
                        // 如果类型为int
                        if (type.equals(ValueType.INT.typeName)) {
                            try {
                                Integer v = Integer.parseInt(value);
                                if (oConvertUtils.isNotEmpty(v)) {
                                    newMap.put(name, v);
                                } else {
                                    log.info("body 参数 {} 值为空,不加入body", name);
                                }
                            } catch (RuntimeException e) {
                                throw new RuntimeException("类型转换错误:" + key + "=" + value + ",转为NUMBER, error: " + e.getMessage());
                            }
                        } else if (type.equals(ValueType.FLOAT.typeName)) {
                            try {
                                Float v = Float.parseFloat(value);
                                if (oConvertUtils.isNotEmpty(v)) {
                                    newMap.put(name, v);
                                } else {
                                    log.info("body 参数 {} 值为空,不加入body", name);
                                }
                            } catch (RuntimeException e) {
                                throw new RuntimeException("类型转换错误:" + key + "=" + value + ",转为NUMBER, error: " + e.getMessage());
                            }
                        } else if (type.equals(ValueType.BOOLEAN.typeName)) {
                            try {
                                Boolean v = Boolean.parseBoolean(value);
                                if (oConvertUtils.isNotEmpty(v)) {
                                    newMap.put(name, v);
                                } else {
                                    log.info("body 参数 {} 值为空,不加入body", name);
                                }
                            } catch (RuntimeException e) {
                                throw new RuntimeException("类型转换错误:" + key + "=" + value + ",转为BOOLEAN, error: " + e.getMessage());
                            }
                        } else if (type.equals(ValueType.STRING.typeName)) {
                            // 默认为string,不做处理
                        } else {
                            throw new RuntimeException("不支持的数据类型:" + key);
                        }
                    } else {
                        // 如果没有定义类型
                        if (oConvertUtils.isNotEmpty(value)){
                            newMap.put(name, value);
                        } else {
                            log.info("body 参数 {} 值为空,不加入body", name);
                        }
                    }
                }
                // body转为json
                String json = JSON.toJSONString(newMap);
                log.info("body-json请求体: {}", json);
                MediaType mediaType = MediaType.Companion.parse("application/json; charset=utf-8");
                requestBody = RequestBody.create(json, mediaType);
                break;
            default:
                throw new RuntimeException("不支持的请求类型: " + contentType);
        }
        return requestBody;
    }

    /**
     * 组装bodyMap
     */
    public Map<String, String> makeBodiesMap(String bodies) {
        Map<String, String> bodiesMap = new HashMap<String, String>();
        if (oConvertUtils.isNotEmpty(bodies)) {
            // 根据换行符拆分header
            List<String> bodiesList = Arrays.stream(bodies.split("\n")).collect(Collectors.toList());
            // 将bodiesList转换为map
            for (String statement : bodiesList) {
                // 按"="拆分字符串
                String[] tmp = statement.split("=");
                // 只有定义了完整name和value的语句才会被解析
                if (tmp.length == 2) {
                    String k = tmp[0];
                    String v = tmp[1];
                    bodiesMap.put(k, v);
                }
            }
        }
        log.info(bodiesMap.toString());
        return bodiesMap;
    }

    /**
     * 组装headerMap
     */
    public Map<String, String> makeHeadersMap(String headers) {
        Map<String, String> headersMap = new HashMap<String, String>();
        if (oConvertUtils.isNotEmpty(headers)) {
            // 根据换行符拆分header
            List<String> headersList = Arrays.stream(headers.split("\n")).collect(Collectors.toList());
            // 将headerList转换为map
            for (String statement : headersList) {
                // 按"="拆分字符串
                String[] tmp = statement.split("=");
                // 只有定义了完整name和value的语句才会被解析
                if (tmp.length == 2) {
                    String k = tmp[0];
                    String v = tmp[1];
                    headersMap.put(k, v);
                }
            }
        }
        log.info(headersMap.toString());

        return headersMap;
    }

    /**
     * 构建okhttp的请求header
     */
    public Headers makeHeadersBuilder(Map<String, String> headersMap) {
        Headers.Builder headersBuilder = new Headers.Builder();
        if (oConvertUtils.isNotEmpty(headersMap)) {
            for (String key : headersMap.keySet()) {
                String[] arr = key.split("->");
                String name = arr[0];
                // 默认值为string
                String value = headersMap.get(key);
                headersBuilder.add(name, value);
            }
        }
        return headersBuilder.build();
    }


}
