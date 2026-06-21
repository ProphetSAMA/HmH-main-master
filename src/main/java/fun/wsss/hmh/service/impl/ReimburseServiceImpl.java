package fun.wsss.hmh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.wsss.hmh.dao.ReimburseDao;
import fun.wsss.hmh.entity.Reimburse;
import fun.wsss.hmh.service.ReimburseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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
                .map(Reimburse::getMoney)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return result;
    }

    @Override
    public BigDecimal getTotalAmount() {
        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reimburse::getStatus, 1);
        List<Reimburse> list = list(wrapper);
        return list.stream()
                .map(Reimburse::getMoney)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Integer getPendingCount() {
        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reimburse::getStatus, 3);
        return Math.toIntExact(count(wrapper));
    }

    @Override
    public Integer getRejectedCount() {
        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reimburse::getStatus, 2);
        return Math.toIntExact(count(wrapper));
    }

    @Override
    public Integer getApprovedCount() {
        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reimburse::getStatus, 1);
        return Math.toIntExact(count(wrapper));
    }

    @Override
    public Integer getMonthlyUserCount() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        Date startDate = Date.from(startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Reimburse::getCreateTime, startDate);
        wrapper.select(Reimburse::getSqUserId);
        wrapper.groupBy(Reimburse::getSqUserId);
        List<Reimburse> list = list(wrapper);
        return list.size();
    }

    @Override
    public Double getGrowthRate() {
        LocalDate now = LocalDate.now();
        LocalDate thisMonthStart = now.withDayOfMonth(1);
        LocalDate lastMonthStart = thisMonthStart.minusMonths(1);

        Date thisStart = Date.from(thisMonthStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date lastStart = Date.from(lastMonthStart.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<Reimburse> thisMonthWrapper = new LambdaQueryWrapper<>();
        thisMonthWrapper.eq(Reimburse::getStatus, 1)
                .ge(Reimburse::getCreateTime, thisStart);
        BigDecimal thisMonthAmount = list(thisMonthWrapper).stream()
                .map(Reimburse::getMoney)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LambdaQueryWrapper<Reimburse> lastMonthWrapper = new LambdaQueryWrapper<>();
        lastMonthWrapper.eq(Reimburse::getStatus, 1)
                .ge(Reimburse::getCreateTime, lastStart)
                .lt(Reimburse::getCreateTime, thisStart);
        BigDecimal lastMonthAmount = list(lastMonthWrapper).stream()
                .map(Reimburse::getMoney)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (lastMonthAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return thisMonthAmount.subtract(lastMonthAmount)
                .divide(lastMonthAmount, 4, RoundingMode.HALF_UP)
                .doubleValue() * 100;
    }

    @Override
    public BigDecimal getMonthlyAmount(LocalDate month) {
        LocalDate start = month.withDayOfMonth(1);
        LocalDate end = start.plusMonths(1);
        Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reimburse::getStatus, 1)
                .ge(Reimburse::getCreateTime, startDate)
                .lt(Reimburse::getCreateTime, endDate);
        List<Reimburse> list = list(wrapper);
        return list.stream()
                .map(Reimburse::getMoney)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getMonthlyAmount() {
        return getMonthlyAmount(LocalDate.now());
    }

    @Override
    public Integer getMonthlyCount(LocalDate month) {
        LocalDate start = month.withDayOfMonth(1);
        LocalDate end = start.plusMonths(1);
        Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Reimburse::getCreateTime, startDate)
                .lt(Reimburse::getCreateTime, endDate);
        return Math.toIntExact(count(wrapper));
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reimburse::getStatus, 1);
        List<Reimburse> list = list(wrapper);

        Map<String, BigDecimal> categoryMap = new LinkedHashMap<>();
        for (Reimburse r : list) {
            String typeName = r.getTypeName() != null ? r.getTypeName() : "未知";
            BigDecimal money = r.getMoney() != null ? r.getMoney() : BigDecimal.ZERO;
            categoryMap.merge(typeName, money, BigDecimal::add);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categoryMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("category", entry.getKey());
            map.put("amount", entry.getValue());
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Reimburse> getAvailableForInvoice() {
        LambdaQueryWrapper<Reimburse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reimburse::getStatus, 1);
        wrapper.orderByDesc(Reimburse::getCreateTime);
        return list(wrapper);
    }
}
