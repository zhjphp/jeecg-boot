package org.jeecg.modules.taskjob.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.powerjob.common.serialize.JsonUtils;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/5/22 21:22
 */
public class MapReduceProcessorDemo implements MapReduceProcessor {

    int y = 0;

    @Override
    public ProcessResult process(TaskContext context) throws Exception {

        OmsLogger omsLogger = context.getOmsLogger();
        long instanceId =  context.getInstanceId();
        long subInstanceId =  context.getSubInstanceId();

        log.info("============== TestMapReduceProcessor#process ==============");
        log.info("isRootTask:{}", isRootTask());
        log.info("taskContext:{}", JsonUtils.toJSONString(context));



        if (isRootTask()) {
            log.info("==== [start root task] ====");
            omsLogger.info("[start root task]");
            List<TestSubTask> subTasks = Lists.newLinkedList();
            log.info("subTasks.add");
            subTasks.add(new TestSubTask("name" + 1, 1));
//            subTasks.add(new TestSubTask("name" + 2, 2));
//            subTasks.add(new TestSubTask("name" + 3, 3));
//            subTasks.add(new TestSubTask("name" + 4, 4));
//            subTasks.add(new TestSubTask("name" + 5, 5));
//            subTasks.add(new TestSubTask("name" + 6, 6));
//            subTasks.add(new TestSubTask("name" + 7, 7));
//            subTasks.add(new TestSubTask("name" + 8, 8));
//            subTasks.add(new TestSubTask("name" + 9, 9));
//            subTasks.add(new TestSubTask("name" + 0, 0));
            for (int x = 0; x < 1;x++) {
                log.info("等待：" + x);
                Thread.sleep(1000);
            }
            log.info("使用map");
            map(subTasks, "MAP_TEST_TASK");
            subTasks.clear();


            omsLogger.info("[DemoMRProcessor] map success~");
            return new ProcessResult(true, "MAP_SUCCESS");
        } else {
            File f = new File("E:\\project\\polymerize\\test\\crawl\\python_test\\1.1");
            StringBuilder sb = new StringBuilder();
            sb.append("eyJDcmF3bElkIjoiMTY1NjEzOT");
            String ppp = new String("");
            String[] cmdArray = new String[]{"python", "test.py" , ppp};
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
            // 环境变量
            Map<String, String> env = processBuilder.environment();
            log.info("环境变量：" + env.toString());
            String path = env.get("Path");
            log.info("pppppppppppppppp变量：" + path);



            Process process = processBuilder.directory(f).redirectErrorStream(true).start();

            BufferedInputStream bufferedInputStream = new BufferedInputStream(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream, Charset.forName("GBK")));
            // command log
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.info(line);
                omsLogger.info(line);
            }
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                // default success
                log.info("shell执行成功, exitValue: {}", exitValue);
                omsLogger.info("shell执行成功, exitValue: {}", exitValue);
            } else {
                throw new Exception("shell执行失败, exitValue: " + exitValue);
            }

            log.info("==== [start subTask task] ====");
            omsLogger.info("[start subTask task]: {}.", JSON.toJSONString(context.getSubTask()));
            log.info("subTask: {}", JsonUtils.toJSONString(context.getSubTask()));
            for (int x = 0; x < 2;x++) {
                log.info("等待：" + x);
                Thread.sleep(1000);
            }
//            y++;
//            omsLogger.info("[当前 Y]: {}.", y);
//            log.info("[当前 Y]: {}.", y);
//            if (y == 3) {
//                log.info("手动失败");
//                omsLogger.info("手动失败");
//                return new ProcessResult(false, "手动失败");
//            } else if (y == 6) {
//                log.info("手动失败");
//                omsLogger.info("手动失败");
//                return new ProcessResult(false, "手动失败");
//            } else {
//                log.info("没有执行");
//            }
            return new ProcessResult(true, "PROCESS_SUCCESS");
        }
    }

    @Override
    public ProcessResult reduce(TaskContext context, List<TaskResult> taskResults) {
        log.info("================ MapReduceProcessorDemo#reduce ================");
//        log.info("TaskContext: {}", JSONObject.toJSONString(context));
//        log.info("List<TaskResult>: {}", JSONObject.toJSONString(taskResults));
        context.getOmsLogger().info("MapReduce job finished, result is {}.", taskResults);

        OmsLogger omsLogger = context.getOmsLogger();

        AtomicLong successCnt = new AtomicLong(0);
        AtomicLong failCnt = new AtomicLong(0);
        boolean res = true;
        for (TaskResult tr: taskResults) {
            if (tr.isSuccess()) {
                successCnt.incrementAndGet();
            } else {
                failCnt.incrementAndGet();
                omsLogger.info("任务id: " + tr.getTaskId() + "失败");
            }
            res = res && tr.isSuccess();
        }

        // 该结果将作为任务最终的执行结果
        return new ProcessResult(res, "success task num:" + successCnt.get() + ", fail task num:" + failCnt.get());

//        boolean success = ThreadLocalRandom.current().nextBoolean();
//        return new ProcessResult(success, context + ": " + success);
    }

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestSubTask {
        private String name;
        private int age;
    }
}
