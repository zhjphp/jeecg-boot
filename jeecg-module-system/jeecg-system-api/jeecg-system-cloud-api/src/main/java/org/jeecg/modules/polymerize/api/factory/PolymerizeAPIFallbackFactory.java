package org.jeecg.modules.polymerize.api.factory;

import org.jeecg.modules.polymerize.api.IPolymerizeAPI;
import org.jeecg.modules.polymerize.api.fallback.PolymerizeAPIFallback;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @description: PolymerizeAPIFallbackFactory
 * @author: wayne
 * @date 2023/5/11 13:57
 */
@Component
public class PolymerizeAPIFallbackFactory implements FallbackFactory<IPolymerizeAPI> {

    @Override
    public IPolymerizeAPI create(Throwable cause) {
        PolymerizeAPIFallback fallback = new PolymerizeAPIFallback();
        fallback.setCause(cause);
        return fallback;
    }

}
