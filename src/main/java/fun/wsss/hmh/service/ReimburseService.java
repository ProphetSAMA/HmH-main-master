package fun.wsss.hmh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.wsss.hmh.entity.Reimburse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 报销Service接口
 * @author h
 */
public interface ReimburseService extends IService<Reimburse> {

	/**
	 * 保存报销
	 * @param reimburse 报销信息
	 * @return 保存结果
	 */
    @Override
    boolean save(Reimburse reimburse);

	/**
	 * 更新报销
	 * @param reimburse 报销信息
	 * @return 更新结果
	 */
	int update(Reimburse reimburse);

	/**
	 * 删除报销
	 * @param reimburse 报销信息
	 * @return 删除结果
	 */
	Integer delete(Reimburse reimburse);

	/**
	 * 根据报销id获取报销信息
	 * @param reimburse 报销信息
	 * @return 报销信息
	 */
	Reimburse getReimburseById(Reimburse reimburse);

	/**
	 * 报销列表
	 * @param map 查询条件
	 * @return 报销列表
	 */
	List<Reimburse> list(Map<String, Object> map);

	/**
	 * 获取总记录数
	 * @param map 查询条件
	 * @return 总记录数
	 */
	Long getTotal(Map<String, Object> map);

	/**
	 * 获取报销统计数据
	 * @return 统计结果
	 */
	Map<String, Object> getReimburseStats();

	/**
	 * 获取已批准报销的总金额
	 * @return 总金额
	 */
	BigDecimal getTotalAmount();

	/**
	 * 获取待审批报销数量
	 * @return 待审批数量
	 */
	Integer getPendingCount();

	/**
	 * 获取已拒绝报销数量
	 * @return 已拒绝数量
	 */
	Integer getRejectedCount();

	/**
	 * 获取已批准报销数量
	 * @return 已批准数量
	 */
	Integer getApprovedCount();

	/**
	 * 获取本月报销人数
	 * @return 本月报销人数
	 */
	Integer getMonthlyUserCount();

	/**
	 * 获取同比增长率（百分比）
	 * @return 增长率
	 */
	Double getGrowthRate();

	/**
	 * 获取指定月份的报销金额
	 * @param month 月份
	 * @return 报销金额
	 */
	BigDecimal getMonthlyAmount(LocalDate month);

	/**
	 * 获取当月的报销金额
	 * @return 报销金额
	 */
	BigDecimal getMonthlyAmount();

	/**
	 * 获取指定月份的报销数量
	 * @param month 月份
	 * @return 报销数量
	 */
	Integer getMonthlyCount(LocalDate month);

	/**
	 * 获取报销类别统计数据
	 * @return 类别统计数据列表
	 */
	List<Map<String, Object>> getCategoryStats();

	/**
	 * 获取可用于关联发票的报销单列表
	 * @return 可用报销单列表
	 */
	List<Reimburse> getAvailableForInvoice();

}
