package fun.wsss.hmh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.wsss.hmh.common.exception.BusinessException;
import fun.wsss.hmh.entity.Announcement;
import fun.wsss.hmh.dao.AnnouncementDao;
import fun.wsss.hmh.service.AnnouncementService;
import fun.wsss.hmh.utils.UserContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementDao, Announcement> implements AnnouncementService {

    @Override
    public Page<Announcement> getList(int page, int rows, String title) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(title), Announcement::getTitle, title)
                .eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getCreateTime);

        return page(new Page<>(page, rows), queryWrapper);
    }
    

    @Override
    public void saveAnnouncement(Announcement announcement) {
        // 获取当前登录用户
        var currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 确保用户ID不为空
        if (currentUser.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 新增公告
        if (announcement.getId() == null) {
            announcement.setCreateUserId(currentUser.getId().longValue());
            announcement.setCreateUserName(currentUser.getTrueName());
            announcement.setStatus(1);
            announcement.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            announcement.setUpdateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        }
        // 更新公告
        else {
            // 验证是否是原创建人
            var original = getById(announcement.getId());
            if (original == null) {
                throw new BusinessException("公告不存在");
            }
            
            // 确保可以比较用户ID
            Long originalUserId = original.getCreateUserId();
            Integer currentUserId = currentUser.getId();
            if (originalUserId == null || currentUserId == null || !originalUserId.equals(currentUserId.longValue())) {
                throw new BusinessException("只能修改自己发布的公告");
            }

            // 只更新标题和内容
            original.setTitle(announcement.getTitle());
            original.setContent(announcement.getContent());
            original.setUpdateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            announcement = original;
        }

        saveOrUpdate(announcement);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        // 获取当前登录用户
        var currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }

        // 验证公告是否存在
        var announcement = getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        // 验证是否是原创建人
        if (!announcement.getCreateUserId().equals(currentUser.getId())) {
            throw new BusinessException("只能删除自己发布的公告");
        }

        // 逻辑删除
        announcement.setStatus(0);
        updateById(announcement);
    }

    @Override
    public Page<Announcement> getAnnouncementPage(Integer current, Integer size) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getCreateTime);
        
        return page(new Page<>(current, size), queryWrapper);
    }
}
