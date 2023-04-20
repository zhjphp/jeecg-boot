package org.jeecg.modules.polymerize.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.dto.InformationSourceDTO;
import org.jeecg.modules.polymerize.entity.InformationSource;
import org.jeecg.modules.polymerize.entity.InformationSourceCategory;
import org.jeecg.modules.polymerize.mapper.InformationSourceCategoryMapper;
import org.jeecg.modules.polymerize.mapper.InformationSourceMapper;
import org.jeecg.modules.polymerize.service.IInformationSourceService;
import org.jeecg.modules.polymerize.vo.InformationSourceVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Description: 信源管理
 * @Author: jeecg-boot
 * @Date:   2023-04-17
 * @Version: V1.0
 */
@Slf4j
@Service
public class InformationSourceServiceImpl extends ServiceImpl<InformationSourceMapper, InformationSource> implements IInformationSourceService {

    @Resource
    InformationSourceMapper informationSourceMapper;

    @Resource
    InformationSourceCategoryMapper informationSourceCategoryMapper;

    @Override
    public IPage<InformationSourceVO> queryPageList(InformationSourceDTO informationSourceDTO, Integer pageNo, Integer pageSize) {
        Page<InformationSourceVO> page = new Page<InformationSourceVO>(pageNo, pageSize);
        IPage<InformationSourceVO> list =  informationSourceMapper.selectInformationSource(page, informationSourceDTO);
        return list;
    }

    /**
     * 添加信源
     *
     * @param informationSourceDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(InformationSourceDTO informationSourceDTO) throws JeecgBootException {
        if (oConvertUtils.isNotEmpty(informationSourceDTO.getId())) {
            log.error("主键 id 不为空");
            throw new JeecgBootException("非法操作");
        }
        if (oConvertUtils.isEmpty(informationSourceDTO.getCategoryIds())) {
            log.error("分类 CategoryIds 为空");
            throw new JeecgBootException("请选择分类");
        }
        // 1.增加 polymerize_information_source 表数据
        InformationSource informationSource = informationSourceDTO;
        // 排重
        duplicateCheck(informationSource);
        int insertCount = informationSourceMapper.insert(informationSource);
        String informationSourceId = informationSource.getId();
        log.info("写入 polymerize_information_source 表, 写入条数：" + insertCount + ", id：" + informationSourceId);
        if (insertCount != 1) {
            log.error("polymerize_information_source 表写入失败, 写入条数: " + insertCount);
            throw new JeecgBootException("信源添加失败");
        }
        // 2.批量增加 polymerize_information_source_category 表数据
        multipleInsertInformationSourceCategory(informationSourceId, informationSourceDTO.getCategoryIds());
    }

    /**
     * 编辑信源
     *
     * @param informationSourceDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(InformationSourceDTO informationSourceDTO) throws JeecgBootException {
        if (oConvertUtils.isEmpty(informationSourceDTO.getCategoryIds())) {
            log.error("分类 CategoryIds 为空");
            throw new JeecgBootException("请选择分类");
        }
        String informationSourceId = informationSourceDTO.getId();
        Long dataCount = informationSourceMapper.selectCount(new LambdaQueryWrapper<InformationSource>().eq(InformationSource::getId, informationSourceId));
        if (dataCount > 1) {
            log.error("信源ID不唯一,id: " + informationSourceId);
            throw new JeecgBootException("信源ID不唯一,请联系技术人员");
        } else if (dataCount < 1) {
            log.error("信源ID不存在,id: " + informationSourceId);
            throw new JeecgBootException("信源不存在");
        }
        // 1.修改 polymerize_information_source 表数据
        InformationSource informationSource = informationSourceDTO;
        int updateCount = informationSourceMapper.updateById(informationSource);
        if (updateCount != 1) {
            log.error("polymerize_information_source 表更新失败, 更新数量 " + updateCount);
            throw new JeecgBootException("信源编辑失败");
        }
        log.info("更新 polymerize_information_source 表,id: " + informationSourceId);
        // 2.修改 polymerize_information_source_category 数据
        // 先删除
        int deleteCount = informationSourceCategoryMapper.delete(
                new LambdaQueryWrapper<InformationSourceCategory>().eq(InformationSourceCategory::getInformationSourceId, informationSourceId)
        );
        log.info("删除 polymerize_information_source_Category 表关联数据 " + deleteCount + "条");
        // 再添加
        multipleInsertInformationSourceCategory(informationSourceId, informationSourceDTO.getCategoryIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) throws JeecgBootException {
        // 逻辑删除主表
        int delCount = informationSourceMapper.deleteById(id);
        log.info("删除 polymerize_information_source 表 id: " + id + ", delCount: " + delCount);
        // 真实删除中间表
        int delCount2 = informationSourceCategoryMapper.delete(
                new LambdaQueryWrapper<InformationSourceCategory>().eq(InformationSourceCategory::getInformationSourceId, id)
        );
        log.info("删除 polymerize_information_source_Category 表 information_source_id: " + id + ", delCount: " + delCount2);
    }

    /**
     * 批量插入中间表
     *
     * @param informationSourceId
     * @param categoryIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void multipleInsertInformationSourceCategory(String informationSourceId, String categoryIds) {
        String[] categoryIdArray = categoryIds.split(",");
        if ( (categoryIdArray != null) && (categoryIdArray.length > 0) ) {
            int insertTotal = 0;
            for (String categoryId : categoryIdArray) {
                InformationSourceCategory informationSourceCategory = new InformationSourceCategory();
                informationSourceCategory.setCategoryId(categoryId).setInformationSourceId(informationSourceId);
                int insertCount = informationSourceCategoryMapper.insert(informationSourceCategory);
                log.info("写入 polymerize_information_source_category 表, 写入条数：" + insertCount + ", id：" + informationSourceCategory.getId());
                insertTotal+=insertCount;
            }
            // 写入数量必须符合预期
            if (insertTotal != categoryIdArray.length) {
                log.error("polymerize_information_source_category 表写入失败, 共 " + categoryIdArray.length + " 条数据, 尝试写入 " + insertTotal + " 条数据");
                throw new JeecgBootException("信源操作失败");
            }
        }
    }

    /**
     * 信源排重
     *
     * @param informationSource
     * @return
     */
    public void duplicateCheck(InformationSource informationSource) throws JeecgBootException {
        // 对域名进行排重
        if (informationSourceMapper.selectCount(new LambdaQueryWrapper<InformationSource>().eq(InformationSource::getDomain, informationSource.getDomain())) != 0) {
            log.error("域名为 " + informationSource.getDomain() + " 信源已存在");
            throw new JeecgBootException("域名为 " + informationSource.getDomain() + " 信源已存在");
        }
        // 对名称进行排重
        if (informationSourceMapper.selectCount(new LambdaQueryWrapper<InformationSource>().eq(InformationSource::getName, informationSource.getName())) != 0) {
            log.error("名称为 " + informationSource.getName() + " 信源已存在");
            throw new JeecgBootException("名称为 " + informationSource.getName() + " 信源已存在");
        }
    }

}
