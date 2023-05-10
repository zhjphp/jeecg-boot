package org.jeecg.modules.polymerize.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.polymerize.dto.InformationSourceDTO;
import org.jeecg.modules.polymerize.entity.InformationSource;
import org.jeecg.modules.polymerize.vo.InformationSourceVO;

import java.util.Map;

/**
 * @Description: 信源管理
 * @Author: jeecg-boot
 * @Date:   2023-04-17
 * @Version: V1.0
 */
public interface InformationSourceMapper extends BaseMapper<InformationSource> {

//    IPage<InformationSource> selectInformationSourceByCategoryId(Page<InformationSource> page, @Param("categoryId") String categoryId);

//    IPage<InformationSource> selectInformationSourceByCategoryId(Page<InformationSource> page, @Param("query") Map<String, String> query);

    IPage<InformationSourceVO> selectInformationSource(Page<InformationSourceVO> page, @Param("informationSourceDTO") InformationSourceDTO informationSourceDTO);

    IPage<InformationSourceVO> selectByComponentData(Page<InformationSourceVO> page, @Param("informationSourceDTO") InformationSourceDTO informationSourceDTO, @Param("idArray") String[] idArray);


}
