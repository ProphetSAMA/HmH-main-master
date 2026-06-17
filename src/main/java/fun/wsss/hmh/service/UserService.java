package fun.wsss.hmh.service;


import fun.wsss.hmh.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 用户Service接口
 * @author h
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param user 用户信息
     * @return User
     */
    User login(User user);

    /**
     * 查找用户
     * @param map 查询条件
     * @return 用户列表
     */
    List<User> find(Map<String, Object> map);

    /**
     * 获取总记录数
     *
     * @param map 查询条件
     * @return 总记录数
     */
    Long getTotal(Map<String, Object> map);

    /**
     * 更新用户
     *
     * @param user 用户信息
     * @return 更新结果
     */
    int update(User user);

    /**
     * 添加用户
     *
     * @param user 用户信息
     * @return 添加结果
     */
    int add(User user);

    /**
     * 根据用户id删除用户
     *
     * @param id 用户id
     * @return 删除结果
     */
    int delete(Integer id);

    /**
     * 根据用户id查询用户信息
     *
     * @param id 用户id
     * @return 用户信息
     */
    User findById(Integer id);

    /**
     * 根据用户id查询用户信息
     *
     * @param id 用户id
     * @return 用户信息
     */
    User getUserById(int id);
}
