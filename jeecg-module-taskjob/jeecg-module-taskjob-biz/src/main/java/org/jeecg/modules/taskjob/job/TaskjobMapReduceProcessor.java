package org.jeecg.modules.taskjob.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.taskjob.model.SubTaskModel;
import org.jeecg.modules.taskjob.service.IConsumerService;
import org.jeecg.modules.taskjob.service.IProducerService;
import org.springframework.stereotype.Component;
import tech.powerjob.common.exception.PowerJobCheckedException;
import tech.powerjob.common.serialize.JsonUtils;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import tech.powerjob.worker.log.OmsLogger;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 信源任务处理程序
 * 控制台参数示例:
 * {"taskIds": "1656199386577870850,1656199339245150210", "threadCount": 10}
 *
 * @version 1.0
 * @description: TaskjobMapReduceProcessor
 * @author: wayne
 * @date 2023/5/23 9:43
 */
@Slf4j
@Component
public class TaskjobMapReduceProcessor implements MapReduceProcessor {

    @Resource
    private IProducerService producerService;

    @Resource
    private IConsumerService consumerService;

    /**
     * 任务处理程序
     *
     * @param taskContext
     * @return ProcessResult
     * @throws Exception
     */
    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        OmsLogger omsLogger = taskContext.getOmsLogger();
        log.info("isRootTask:{} ", isRootTask());
        // 当前执行的jobId
        long jobId = taskContext.getJobId();
        // 实例id
        long instanceId = taskContext.getInstanceId();

        if (isRootTask()) {
            try {
                log.info("================ Producer Process ================");
                omsLogger.info("================ Producer Process ================");
                omsLogger.info("Producer任务taskContext:{}", JsonUtils.toJSONString(taskContext));
                log.info("Producer任务 taskContext:{}", JsonUtils.toJSONString(taskContext));
                // 获取任务执行参数 (使用json格式,taskId使用逗号间隔)
                String jobParam = taskContext.getJobParams();
                if (oConvertUtils.isEmpty(jobParam)) {
                    throw new Exception("没有指定 taskId");
                }
                // 获取任务执行参数 (使用json格式,)
                JSONObject jobParamObject = JSON.parseObject(jobParam);
                // 获取任务开启的线程数
                int threadCount = jobParamObject.getIntValue("threadCount");
                // 获取taskIds,用逗号间隔
                String taskIds = jobParamObject.getString("taskIds");
                producerService.doJob(jobId, taskIds, instanceId, omsLogger);
                // 开启子任务线程
                List<SubTaskModel> subTasks = Lists.newLinkedList();
                for (int i = 0; i < threadCount; i++) {
                    subTasks.add(new SubTaskModel(i + 1));
                }
                map(subTasks, "MAP_SUB_TASK");

                return new ProcessResult(true, "Producer Result Success");
            } catch (Exception e) {
                // e.printStackTrace();
                log.error("Producer执行出错, jobId={}, instanceId={}, 原因: {}, StackTrace: {}", jobId, instanceId, e.getMessage(), e);
                omsLogger.error("Producer执行出错, jobId={}, instanceId={}, 原因: {}, StackTrace: {}", jobId, instanceId, e.getMessage(), e);
                return new ProcessResult(false, e.getMessage());
            }
        } else {
            try {
                log.info("================ Consumer Process ================");
                omsLogger.info("================ Consumer Process ================");
                omsLogger.info("Consumer任务taskContext:{}", JsonUtils.toJSONString(taskContext));
                log.info("Consumer任务 taskContext:{}", JsonUtils.toJSONString(taskContext));
                consumerService.doJob(jobId, instanceId, omsLogger);
                return new ProcessResult(true, "Consumer Result Success");
            } catch (Exception e) {
                // e.printStackTrace();
                log.error("Consumer执行出错, jobId={}, instanceId={}, 原因: {}, StackTrace: {}", jobId, instanceId, e.getMessage(), e);
                omsLogger.error("Consumer执行出错, jobId={}, instanceId={}, 原因: {}, StackTrace: {}", jobId, instanceId, e.getMessage(), e);
                return new ProcessResult(false, e.getMessage());
            }
        }
    }

    /**
     * reduce处理程序
     *
     * @param taskContext
     * @param taskResults
     * @return ProcessResult
     */
    @Override
    public ProcessResult reduce(TaskContext taskContext, List<TaskResult> taskResults) {
        log.info("================ reduce ================");
        OmsLogger omsLogger = taskContext.getOmsLogger();
        // 成功数量
        AtomicLong successCnt = new AtomicLong(0);
        // 失败数量
        AtomicLong failCnt = new AtomicLong(0);
        // 结果
        AtomicBoolean resultCnt = new AtomicBoolean(true);
        // 遍历所有子任务,全部成功才算成功
        for (TaskResult tr: taskResults) {
            if (tr.isSuccess()) {
                successCnt.incrementAndGet();
            } else {
                failCnt.incrementAndGet();
                resultCnt.set(false);
            }
        }
        if (resultCnt.get()) {
            log.info("任务执行成功, jobId=" + taskContext.getJobId() + ", instanceId=" + taskContext.getInstanceId() + ", 成功任务数量:" + successCnt.get() + ", 失败任务数量:" + failCnt.get());
            omsLogger.info("任务执行成功, jobId=" + taskContext.getJobId() + ", instanceId=" + taskContext.getInstanceId() + ", 成功任务数量:" + successCnt.get() + ", 失败任务数量:" + failCnt.get());
        } else {
            log.error("任务执行失败, jobId=" + taskContext.getJobId() + ", instanceId=" + taskContext.getInstanceId() + ", 成功任务数量:" + successCnt.get() + ", 失败任务数量:" + failCnt.get());
            omsLogger.error("任务执行失败, jobId=" + taskContext.getJobId() + ", instanceId=" + taskContext.getInstanceId() + ", 成功任务数量:" + successCnt.get() + ", 失败任务数量:" + failCnt.get());
        }
        // 该结果将作为任务最终的执行结果
        return new ProcessResult(resultCnt.get(), "任务执行结束, jobId=" + taskContext.getJobId() + ", 成功任务数量:" + successCnt.get() + ", 失败任务数量:" + failCnt.get());
    }

}
