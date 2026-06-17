package fun.wsss.hmh.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.wsss.hmh.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementDao extends BaseMapper<Announcement> {
}
