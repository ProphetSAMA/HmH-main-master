package fun.wsss.hmh.service.impl;


import fun.wsss.hmh.dao.UserDao;
import fun.wsss.hmh.entity.User;
import fun.wsss.hmh.service.UserService;
import fun.wsss.hmh.utils.PasswordUtil;
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
        // 先根据用户名查询用户
        User dbUser = userDao.selectOne(
                new QueryWrapper<User>().eq("userName", user.getUserName())
        );

        if (dbUser == null) {
            return null;
        }

        // 验证密码
        if (PasswordUtil.verifyPassword(user.getPassword(), dbUser.getPassword())) {
            return dbUser;
        }

        return null;
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

        // 添加分页 - 使用参数化查询防止SQL注入
        if (map.get("start") != null && map.get("size") != null) {
            try {
                int start = Integer.parseInt(map.get("start").toString());
                int size = Integer.parseInt(map.get("size").toString());
                if (start >= 0 && size > 0 && size <= 100) {
                    queryWrapper.last("LIMIT " + start + "," + size);
                }
            } catch (NumberFormatException e) {
                // 忽略无效的分页参数
            }
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
        // 如果包含密码更新，需要加密
        if (user.getPassword() != null) {
            user.setPassword(PasswordUtil.encryptPassword(user.getPassword()));
        }
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
        // 加密密码
        if (user.getPassword() != null) {
            user.setPassword(PasswordUtil.encryptPassword(user.getPassword()));
        }
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

    @Override
    public int getUserCount() {
        return Math.toIntExact(userDao.selectCount(null));
    }
}
