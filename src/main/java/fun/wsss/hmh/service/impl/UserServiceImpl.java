package fun.wsss.hmh.service.impl;


import fun.wsss.hmh.dao.UserDao;
import fun.wsss.hmh.entity.User;
import fun.wsss.hmh.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;
import java.util.Map;

/**
 * 用户Service实现类
 * @author h
 */
@Service("userService")
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    /**
     * 用户登录
     *
     * @param user 用户信息
     * @return User
     */
    @Override
    public User login(User user) {
        return userDao.login(user);
    }

    /**
     * 查找用户
     *
     * @param map 查询条件
     * @return 用户列表
     */
    @Override
    public List<User> find(Map<String, Object> map) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        
        // 添加查询条件
        if (map.get("userName") != null && !map.get("userName").toString().isEmpty()) {
            queryWrapper.like("userName", map.get("userName"));
        }
        if (map.get("roleName") != null && !map.get("roleName").toString().isEmpty()) {
            queryWrapper.eq("roleName", map.get("roleName"));
        }
        
        // 添加分页
        if (map.get("start") != null && map.get("size") != null) {
            queryWrapper.last("LIMIT " + map.get("start") + "," + map.get("size"));
        }
        
        return userDao.selectList(queryWrapper);
    }

    /**
     * 获取总记录数
     *
     * @param map 查询条件
     * @return 总记录数
     */
    @Override
    public Long getTotal(Map<String, Object> map) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        
        // 添加查询条件
        if (map.get("userName") != null && !map.get("userName").toString().isEmpty()) {
            queryWrapper.like("userName", map.get("userName"));
        }
        if (map.get("roleName") != null && !map.get("roleName").toString().isEmpty()) {
            queryWrapper.eq("roleName", map.get("roleName"));
        }
        
        return userDao.selectCount(queryWrapper);
    }

    /**
     * 更新用户
     *
     * @param user 用户信息
     * @return 更新结果
     */
    @Override
    public int update(User user) {
        return userDao.updateById(user);
    }

    /**
     * 添加用户
     *
     * @param user 用户信息
     * @return 添加结果
     */
    @Override
    public int add(User user) {
        return userDao.insert(user);
    }

    /**
     * 根据用户id删除用户
     *
     * @param id 用户id
     * @return 删除结果
     */
    @Override
    public int delete(Integer id) {
        return userDao.deleteById(id);
    }

    /**
     * 根据用户id查询用户信息
     *
     * @param id 用户id
     * @return 用户信息
     */
    @Override
    public User findById(Integer id) {
        return userDao.selectById(id);
    }

    @Override
    public User getUserById(int id) {
        return userDao.getUserById(id);
    }
}
