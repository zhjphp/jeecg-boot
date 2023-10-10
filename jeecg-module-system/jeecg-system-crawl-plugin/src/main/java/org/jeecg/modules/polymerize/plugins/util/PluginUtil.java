package org.jeecg.modules.polymerize.plugins.util;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.polymerize.plugins.inteface.PluginInterface;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @version 1.0
 * @description: 插件加载器
 * @author: wayne
 * @date 2023/9/4 16:59
 */
@Slf4j
@Component
public class PluginUtil {

    @Resource
    private SpringUtil springUtil;

    /**
     * 加载插件
     *
     * @param pluginUrl 插件路径
     * @param pluginName 插件名称
     */
    public PluginInterface loadPlugin(String pluginUrl, String pluginName) throws Exception {
        log.info("加载插件: {}", pluginName);
        // 读取jar包
        String fullPluginUrl = pluginUrl + pluginName + ".jar";
        log.info("fullPluginUrl: {}", fullPluginUrl);
        ClassLoader classLoader = ClassLoaderUtil.getClassLoader(fullPluginUrl);
        String pluginClass = PluginInterface.packageName + "." + pluginName;
        Class clazz = classLoader.loadClass(pluginClass);
        String beanName = clazz.getName() + "." + pluginName;
        log.info("beanName: {}", beanName);
        // Constructor constructor = clazz.getConstructor();
        // Object object = constructor.newInstance();
        // 注册bean
        springUtil.registerBean(beanName, clazz);
        // 加载bean
        Object bean = springUtil.getApplicationContext().getBean(beanName);
        if (bean instanceof PluginInterface) {
            // 强转
            PluginInterface plugin = (PluginInterface) bean;
            log.info("加载插件: {}, 成功", pluginName);
            return plugin;
        } else {
            throw new Exception("插件加载错误");
        }
    }

    /**
     * 移除插件
     *
     * @param pluginUrl 插件路径
     * @param pluginName 插件名称
     */
    public void removePlugin(String pluginUrl, String pluginName) throws Exception {
        log.info("移除插件: {}", pluginName);
        String fullPluginUrl = pluginUrl + pluginName + ".jar";
        log.info("fullPluginUrl: {}", fullPluginUrl);
        ClassLoader classLoader = ClassLoaderUtil.getClassLoader(fullPluginUrl);
        String pluginClass = PluginInterface.packageName + "." + pluginName;
        Class clazz = classLoader.loadClass(pluginClass);
        String beanName = clazz.getName() + "." + pluginName;
        log.info("beanName: {}", beanName);
        springUtil.removeBean(beanName);
        log.info("移除插件: {}, 成功", pluginName);
    }

    /**
     * 执行插件
     *
     * @param plugin 插件实例
     * @param param 运行参数
     */
    public String run(PluginInterface plugin, String... param) {
        return plugin.run(param);
    }

}
