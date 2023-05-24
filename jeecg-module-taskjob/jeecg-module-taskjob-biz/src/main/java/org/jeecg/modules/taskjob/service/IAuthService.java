package org.jeecg.modules.taskjob.service;

public interface IAuthService {

    /**
     * 获取临时令牌
     * 模拟登陆接口，获取模拟 Token
     *
     * @return String
     */
    String getTemporaryToken();

}
