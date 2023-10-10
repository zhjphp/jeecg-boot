package org.jeecg.modules.polymerize.plugins.inteface;

/**
 * 插件接口
 */
public interface PluginInterface {

    /**插件包名,必须在这个包下*/
    static String packageName = "org.jeecg.modules.polymerize.plugins.plugin";

    /**插件执行指令*/
    String run(String... params);

}
