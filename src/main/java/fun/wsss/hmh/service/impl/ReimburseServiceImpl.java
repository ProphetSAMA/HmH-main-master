package fun.wsss.hmh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.wsss.hmh.dao.ReimburseDao;
import fun.wsss.hmh.entity.Reimburse;
import fun.wsss.hmh.service.ReimburseService;
import fun.wsss.hmh.utils.UserContext;
import fun.wsss.hmh.utils.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    @Override
    public BigDecimal getTotalAmount() {
        // 示例实现，实际应从数据库查询
        return new BigDecimal("10000.00");
    }

    @Override
    public Integer getPendingCount() {
        // 查询状态为3（待审核）的报销单数量
        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reimburse::getStatus, 3);
        return Math.toIntExact(count(wrapper));
    }

    @Override
    public Integer getRejectedCount() {
        // 示例实现
        return 2;
    }

    @Override
    public Integer getApprovedCount() {
        // 示例实现
        return 15;
    }

    @Override
    public Integer getMonthlyUserCount() {
        // 示例实现
        return 8;
    }

    @Override
    public Double getGrowthRate() {
        // 示例实现
        return 12.5;
    }

    @Override
    public BigDecimal getMonthlyAmount(LocalDate month) {
        // 将LocalDate转换为Date
        Calendar calendar = Calendar.getInstance();
        calendar.set(month.getYear(), month.getMonthValue() - 1, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime();
        
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.SECOND, -1);
        Date endDate = calendar.getTime();
        
        // 查询指定月份已通过的报销单
        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reimburse::getStatus, 1) // 状态为2（已通过）
               .between(Reimburse::getCreateTime, startDate, endDate);
        
        List<Reimburse> list = list(wrapper);
        
        // 计算总金额
        BigDecimal total = BigDecimal.ZERO;
        for (Reimburse reimburse : list) {
            if (reimburse.getMoney() != null) {
                // 将Double转换为BigDecimal
                total = total.add(BigDecimal.valueOf(reimburse.getMoney()));
            }
        }
        
        return total;
    }

    @Override
    public BigDecimal getMonthlyAmount() {
        // 获取当前月份
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        return getMonthlyAmount(currentMonth);
    }

    @Override
    public Integer getMonthlyCount(LocalDate month) {
        // 示例实现
        return 3;
    }

    @Override
    public List<Map<String, Object>> getCategoryData() {
        // 示例实现
        List<Map<String, Object>> data = new ArrayList<>();
        
        Map<String, Object> category1 = new HashMap<>();
        category1.put("name", "差旅费");
        category1.put("value", 4000);
        data.add(category1);
        
        Map<String, Object> category2 = new HashMap<>();
        category2.put("name", "办公用品");
        category2.put("value", 2500);
        data.add(category2);
        
        Map<String, Object> category3 = new HashMap<>();
        category3.put("name", "招待费");
        category3.put("value", 1800);
        data.add(category3);
        
        Map<String, Object> category4 = new HashMap<>();
        category4.put("name", "其他");
        category4.put("value", 1700);
        data.add(category4);
        
        return data;
    }

    /**
     * 获取可用于关联发票的报销单
     */
    @Override
    public List<Reimburse> getAvailableForInvoice() {
        try {
            // 获取当前用户ID
            Integer userId = UserUtil.getCurrentUserId();
            System.out.println("当前用户ID: " + userId); // 添加调试信息
            
            // 查询当前用户的待审核或已通过的报销单
            LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Reimburse::getSqUserId, userId)
                   .in(Reimburse::getStatus, 1, 3) // 1-已通过, 3-待审核
                   .orderByDesc(Reimburse::getCreateTime);
            
            List<Reimburse> result = list(wrapper);
            System.out.println("查询到的报销单数量: " + result.size()); // 添加调试信息
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
