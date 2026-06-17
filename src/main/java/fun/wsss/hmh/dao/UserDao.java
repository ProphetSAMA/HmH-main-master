package fun.wsss.hmh.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.wsss.hmh.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 用户Dao接口
 * @author h
 */
@Mapper
public interface UserDao extends BaseMapper<User> {

	/**
	 * 用户登录
	 * @param user 用户
	 * @return 用户
	 */
	@Select("SELECT * FROM t_user WHERE userName = #{userName} AND password = #{password} AND roleName = #{roleName}")
	User login(User user);
	
	/**
	 * 获取总记录数
	 * @param map 查询条件
	 * @return 总记录数
	 */
    Long getTotal(Map<String, Object> map);
	
	/**
	 * 更新用户
	 * @param user 用户
	 * @return 更新的记录数
	 */
    int update(User user);
	
	/**
	 * 添加用户
	 * @param user 用户
	 * @return 添加的记录数
	 */
    int add(User user);
	
	/**
	 * 删除用户
	 * @param id 用户id
	 * @return 删除的记录数
	 */
    int delete(Integer id);

	/**
	 * 根据用户id查询用户信息
	 * @param id 用户id
	 * @return 用户信息
	 */
	@Select("SELECT * FROM t_user WHERE id = #{id}")
	User getUserById(int id);
}
