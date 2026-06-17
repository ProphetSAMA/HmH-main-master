package fun.wsss.hmh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.wsss.hmh.entity.Reimburse;

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

}
