package org.jeecg.modules.taskjob.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @version 1.0
 * @description: SubTaskModel 父任务向子任务传递数据
 * @author: wayne
 * @date 2023/5/23 15:02
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SubTaskModel implements Serializable{
    private static final long serialVersionUID = 1L;

    /**自定义线程号*/
    private int threadNumber;

}
