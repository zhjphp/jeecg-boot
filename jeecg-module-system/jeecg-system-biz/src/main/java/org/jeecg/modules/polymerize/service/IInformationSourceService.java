package org.jeecg.modules.polymerize.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.polymerize.dto.InformationSourceDTO;
import org.jeecg.modules.polymerize.entity.InformationSource;
import org.jeecg.modules.polymerize.vo.InformationSourceVO;

/**
 * @Description: 信源管理
 * @Author: jeecg-boot
 * @Date:   2023-04-17
 * @Version: V1.0
 */
public interface IInformationSourceService extends IService<InformationSource> {

    IPage<InformationSourceVO> queryPageList(InformationSourceDTO informationSourceDTO, Integer pageNo, Integer pageSize);

    void add(InformationSourceDTO InformationSourceDTO) throws JeecgBootException;

    void edit(InformationSourceDTO InformationSourceDTO) throws JeecgBootException;

    void delete(String id) throws JeecgBootException;

}
