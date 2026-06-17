package fun.wsss.hmh.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import fun.wsss.hmh.entity.Announcement;

public interface AnnouncementService extends IService<Announcement> {
    Page<Announcement> getList(int page, int rows, String title);

    void saveAnnouncement(Announcement announcement);

    void deleteAnnouncement(Long id);

    /**
     * 获取公告分页数据
     * @param current 当前页
     * @param size 每页大小
     * @return 分页数据
     */
    Page<Announcement> getAnnouncementPage(Integer current, Integer size);
}
