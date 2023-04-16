package org.jeecg.modules.polymerize.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.polymerize.entity.PolymerizeCategory;
import org.jeecg.modules.polymerize.model.TreeSelectModel;

import java.util.List;
import java.util.Map;

/**
 * @Description: 分类字典
 * @Author: wayne
 * @Date:   2023-04-16
 * @Version: V1.0
 */
public interface PolymerizeCategoryMapper extends BaseMapper<PolymerizeCategory> {


    /**
     * 根据父级ID查询树节点数据
     * @param pid
     * @param query
     * @return
     */
    public List<TreeSelectModel> queryListByPid(@Param("pid")  String pid, @Param("query") Map<String, String> query);

    /**
     * 通过code查询分类字典表
     * @param code
     * @return
     */
    @Select("SELECT ID FROM polymerize_category WHERE CODE = #{code,jdbcType=VARCHAR}")
    public String queryIdByCode(@Param("code")  String code);

}
