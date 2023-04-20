package org.jeecg.modules.polymerize.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.modules.polymerize.entity.PolymerizeCategory;
import org.jeecg.modules.polymerize.mapper.PolymerizeCategoryMapper;
import org.jeecg.modules.polymerize.service.IPolymerizeCategoryService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 总分类树
 * @Author: jeecg-boot
 * @Date:   2023-04-20
 * @Version: V1.0
 */
@Service
public class PolymerizeCategoryServiceImpl extends ServiceImpl<PolymerizeCategoryMapper, PolymerizeCategory> implements IPolymerizeCategoryService {

	@Override
	public void addPolymerizeCategory(PolymerizeCategory polymerizeCategory) {
	   //新增时设置hasChild为0
	    polymerizeCategory.setHasChild(IPolymerizeCategoryService.NOCHILD);
		if(oConvertUtils.isEmpty(polymerizeCategory.getPid())){
			polymerizeCategory.setPid(IPolymerizeCategoryService.ROOT_PID_VALUE);
		}else{
			//如果当前节点父ID不为空 则设置父节点的hasChildren 为1
			PolymerizeCategory parent = baseMapper.selectById(polymerizeCategory.getPid());
			if(parent!=null && !"1".equals(parent.getHasChild())){
				parent.setHasChild("1");
				baseMapper.updateById(parent);
			}
		}
		baseMapper.insert(polymerizeCategory);
	}
	
	@Override
	public void updatePolymerizeCategory(PolymerizeCategory polymerizeCategory) {
		PolymerizeCategory entity = this.getById(polymerizeCategory.getId());
		if(entity==null) {
			throw new JeecgBootException("未找到对应实体");
		}
		String old_pid = entity.getPid();
		String new_pid = polymerizeCategory.getPid();
		if(!old_pid.equals(new_pid)) {
			updateOldParentNode(old_pid);
			if(oConvertUtils.isEmpty(new_pid)){
				polymerizeCategory.setPid(IPolymerizeCategoryService.ROOT_PID_VALUE);
			}
			if(!IPolymerizeCategoryService.ROOT_PID_VALUE.equals(polymerizeCategory.getPid())) {
				baseMapper.updateTreeNodeStatus(polymerizeCategory.getPid(), IPolymerizeCategoryService.HASCHILD);
			}
		}
		baseMapper.updateById(polymerizeCategory);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deletePolymerizeCategory(String id) throws JeecgBootException {
		//查询选中节点下所有子节点一并删除
        id = this.queryTreeChildIds(id);
        if(id.indexOf(",")>0) {
            StringBuffer sb = new StringBuffer();
            String[] idArr = id.split(",");
            for (String idVal : idArr) {
                if(idVal != null){
                    PolymerizeCategory polymerizeCategory = this.getById(idVal);
                    String pidVal = polymerizeCategory.getPid();
                    //查询此节点上一级是否还有其他子节点
                    List<PolymerizeCategory> dataList = baseMapper.selectList(new QueryWrapper<PolymerizeCategory>().eq("pid", pidVal).notIn("id",Arrays.asList(idArr)));
                    boolean flag = (dataList == null || dataList.size() == 0) && !Arrays.asList(idArr).contains(pidVal) && !sb.toString().contains(pidVal);
                    if(flag){
                        //如果当前节点原本有子节点 现在木有了，更新状态
                        sb.append(pidVal).append(",");
                    }
                }
            }
            //批量删除节点
            baseMapper.deleteBatchIds(Arrays.asList(idArr));
            //修改已无子节点的标识
            String[] pidArr = sb.toString().split(",");
            for(String pid : pidArr){
                this.updateOldParentNode(pid);
            }
        }else{
            PolymerizeCategory polymerizeCategory = this.getById(id);
            if(polymerizeCategory==null) {
                throw new JeecgBootException("未找到对应实体");
            }
            updateOldParentNode(polymerizeCategory.getPid());
            baseMapper.deleteById(id);
        }
	}
	
	@Override
    public List<PolymerizeCategory> queryTreeListNoPage(QueryWrapper<PolymerizeCategory> queryWrapper) {
        List<PolymerizeCategory> dataList = baseMapper.selectList(queryWrapper);
        List<PolymerizeCategory> mapList = new ArrayList<>();
        for(PolymerizeCategory data : dataList){
            String pidVal = data.getPid();
            //递归查询子节点的根节点
            if(pidVal != null && !IPolymerizeCategoryService.NOCHILD.equals(pidVal)){
                PolymerizeCategory rootVal = this.getTreeRoot(pidVal);
                if(rootVal != null && !mapList.contains(rootVal)){
                    mapList.add(rootVal);
                }
            }else{
                if(!mapList.contains(data)){
                    mapList.add(data);
                }
            }
        }
        return mapList;
    }

    @Override
    public List<SelectTreeModel> queryListByCode(String parentCode) {
        String pid = ROOT_PID_VALUE;
        if (oConvertUtils.isNotEmpty(parentCode)) {
            LambdaQueryWrapper<PolymerizeCategory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PolymerizeCategory::getPid, parentCode);
            List<PolymerizeCategory> list = baseMapper.selectList(queryWrapper);
            if (list == null || list.size() == 0) {
                throw new JeecgBootException("该编码【" + parentCode + "】不存在，请核实!");
            }
            if (list.size() > 1) {
                throw new JeecgBootException("该编码【" + parentCode + "】存在多个，请核实!");
            }
            pid = list.get(0).getId();
        }
        return baseMapper.queryListByPid(pid, null);
    }

    @Override
    public List<SelectTreeModel> queryListByPid(String pid) {
        if (oConvertUtils.isEmpty(pid)) {
            pid = ROOT_PID_VALUE;
        }
        return baseMapper.queryListByPid(pid, null);
    }

    @Override
    public List<SelectTreeModel> queryListByPidCondition(String pid, Map<String, String> condition) {
        if (oConvertUtils.isEmpty(pid)) {
            pid = ROOT_PID_VALUE;
        }
        return baseMapper.queryListByPid(pid, condition);
    }

	/**
	 * 根据所传pid查询旧的父级节点的子节点并修改相应状态值
	 * @param pid
	 */
	private void updateOldParentNode(String pid) {
		if(!IPolymerizeCategoryService.ROOT_PID_VALUE.equals(pid)) {
			Long count = baseMapper.selectCount(new QueryWrapper<PolymerizeCategory>().eq("pid", pid));
			if(count==null || count<=1) {
				baseMapper.updateTreeNodeStatus(pid, IPolymerizeCategoryService.NOCHILD);
			}
		}
	}

	/**
     * 递归查询节点的根节点
     * @param pidVal
     * @return
     */
    private PolymerizeCategory getTreeRoot(String pidVal){
        PolymerizeCategory data =  baseMapper.selectById(pidVal);
        if(data != null && !IPolymerizeCategoryService.ROOT_PID_VALUE.equals(data.getPid())){
            return this.getTreeRoot(data.getPid());
        }else{
            return data;
        }
    }

    /**
     * 根据id查询所有子节点id
     * @param ids
     * @return
     */
    private String queryTreeChildIds(String ids) {
        //获取id数组
        String[] idArr = ids.split(",");
        StringBuffer sb = new StringBuffer();
        for (String pidVal : idArr) {
            if(pidVal != null){
                if(!sb.toString().contains(pidVal)){
                    if(sb.toString().length() > 0){
                        sb.append(",");
                    }
                    sb.append(pidVal);
                    this.getTreeChildIds(pidVal,sb);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 递归查询所有子节点
     * @param pidVal
     * @param sb
     * @return
     */
    private StringBuffer getTreeChildIds(String pidVal,StringBuffer sb){
        List<PolymerizeCategory> dataList = baseMapper.selectList(new QueryWrapper<PolymerizeCategory>().eq("pid", pidVal));
        if(dataList != null && dataList.size()>0){
            for(PolymerizeCategory tree : dataList) {
                if(!sb.toString().contains(tree.getId())){
                    sb.append(",").append(tree.getId());
                }
                this.getTreeChildIds(tree.getId(),sb);
            }
        }
        return sb;
    }

}
