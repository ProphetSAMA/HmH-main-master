package fun.wsss.hmh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.wsss.hmh.dao.ReimburseDao;
import fun.wsss.hmh.entity.Reimburse;
import fun.wsss.hmh.service.ReimburseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报销Service实现类
 *
 * @author h
 */
@Service("reimburseService")
public class ReimburseServiceImpl extends ServiceImpl<ReimburseDao, Reimburse> implements ReimburseService {

    @Autowired
    private ReimburseDao reimburseDao;

    /**
     * 保存报销
     *
     * @param reimburse 报销信息
     * @return 保存结果
     */
    @Override
    public boolean save(Reimburse reimburse) {
        return reimburseDao.insert(reimburse) > 0;
    }

    /**
     * 更新报销
     *
     * @param reimburse 报销信息
     * @return 更新结果
     */
    @Override
    public int update(Reimburse reimburse) {
        return reimburseDao.updateById(reimburse);
    }

    /**
     * 删除报销
     *
     * @param reimburse 报销信息
     * @return 删除结果
     */
    @Override
    public Integer delete(Reimburse reimburse) {
        return reimburseDao.deleteById(reimburse.getId());
    }

    /**
     * 根据报销id获取报销信息
     *
     * @param reimburse 报销信息
     * @return 报销信息
     */
    @Override
    public Reimburse getReimburseById(Reimburse reimburse) {
        return reimburseDao.selectById(reimburse.getId());
    }

    /**
     * 查询报销信息
     *
     * @param map 查询条件
     * @return 报销列表
     */
    @Override
    public List<Reimburse> list(Map<String, Object> map) {
        QueryWrapper<Reimburse> queryWrapper = new QueryWrapper<>();
        if (map.get("type") != null && !map.get("type").toString().isEmpty()) {
            queryWrapper.eq("type", map.get("type"));
        }
        if (map.get("spName") != null && !map.get("spName").toString().isEmpty()) {
            queryWrapper.like("sp_name", map.get("spName"));
        }
        if (map.get("sqUserId") != null && !map.get("sqUserId").toString().isEmpty()) {
            queryWrapper.eq("sq_user_id", map.get("sqUserId"));
        }
        if (map.get("status") != null && !map.get("status").toString().isEmpty()) {
            queryWrapper.eq("status", map.get("status"));
        }
        queryWrapper.orderByDesc("id");
        return reimburseDao.selectList(queryWrapper.last("LIMIT " + map.get("start") + ", " + map.get("size")));
    }

    /**
     * 获取总记录数
     *
     * @param map 查询条件
     * @return 总记录数
     */
    @Override
    public Long getTotal(Map<String, Object> map) {
        QueryWrapper<Reimburse> queryWrapper = new QueryWrapper<>();
        if (map.get("type") != null && !map.get("type").toString().isEmpty()) {
            queryWrapper.eq("type", map.get("type"));
        }
        if (map.get("spName") != null && !map.get("spName").toString().isEmpty()) {
            queryWrapper.like("sp_name", map.get("spName"));
        }
        if (map.get("sqUserId") != null && !map.get("sqUserId").toString().isEmpty()) {
            queryWrapper.eq("sq_user_id", map.get("sqUserId"));
        }
        if (map.get("status") != null && !map.get("status").toString().isEmpty()) {
            queryWrapper.eq("status", map.get("status"));
        }
        if (map.get("managerId") != null && !map.get("managerId").toString().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper.eq("status", 3).or().eq("sp_user_id", map.get("managerId")));
        }
        return reimburseDao.selectCount(queryWrapper);
    }

    /**
     * 获取报销统计信息
     *
     * @return 报销统计信息
     */
    @Override
    public Map<String, Object> getReimburseStats() {
        QueryWrapper<Reimburse> pendingWrapper = new QueryWrapper<Reimburse>().eq("status", 3);
        QueryWrapper<Reimburse> approvedWrapper = new QueryWrapper<Reimburse>().eq("status", 1);

        Map<String, Object> result = new HashMap<>();
        result.put("pendingCount", reimburseDao.selectCount(pendingWrapper));
        result.put("approvedCount", reimburseDao.selectCount(approvedWrapper));
        result.put("totalAmount", reimburseDao.selectList(null).stream()
                .mapToDouble(Reimburse::getMoney)
                .sum());

        return result;
    }
}
