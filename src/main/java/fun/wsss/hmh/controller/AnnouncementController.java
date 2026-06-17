package fun.wsss.hmh.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.wsss.hmh.common.Result;
import fun.wsss.hmh.entity.Announcement;
import fun.wsss.hmh.entity.User;
import fun.wsss.hmh.service.AnnouncementService;
import fun.wsss.hmh.service.UserService;
import fun.wsss.hmh.utils.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserService userService;

    @GetMapping("/list")
    public Result getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer rows,
            @RequestParam(required = false) String title) {
        var pageResult = announcementService.getList(page, rows, title);
        return Result.success(pageResult);
    }

    @PostMapping("/save")
    public Result save(@RequestBody Announcement announcement, HttpServletRequest request) {
        // 验证用户登录状态
        User user = TokenUtil.getUserFromToken(request, userService);
        if (user == null) {
            return Result.error("用户未登录");
        }

        // 验证权限：只有管理员和经理可以发布公告
        if (!"管理员".equals(user.getRoleName()) && !"经理".equals(user.getRoleName())) {
            return Result.error().message("权限不足，无法发布公告");
        }

        // 设置发布者信息
        announcement.setPublisherId(user.getId());
        announcement.setPublisherName(user.getTrueName());

        announcementService.saveAnnouncement(announcement);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id, HttpServletRequest request) {
        // 验证用户登录状态
        User user = TokenUtil.getUserFromToken(request, userService);
        if (user == null) {
            return Result.error("用户未登录");
        }

        // 验证权限：只有管理员可以删除公告
        if (!"管理员".equals(user.getRoleName())) {
            return Result.error().message("权限不足，无法删除公告");
        }

        announcementService.deleteAnnouncement(id);
        return Result.success();
    }

    @GetMapping("/page")
    public Result getAnnouncementPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Announcement> page = announcementService.getAnnouncementPage(current, size);
        return Result.success(page);
    }

    /**
     * 获取最新公告
     */
    @GetMapping("/latest")
    public Result getLatestAnnouncements() {
        try {
            // 查询最新的3条公告
            LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Announcement::getStatus, 1) // 只查询状态为1的公告
                   .orderByDesc(Announcement::getCreateTime)
                   .last("LIMIT 3");
            
            List<Announcement> announcementList = announcementService.list(wrapper);
            return Result.success(announcementList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("获取最新公告失败: " + e.getMessage());
        }
    }
}
