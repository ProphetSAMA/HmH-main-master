package fun.wsss.hmh.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.wsss.hmh.entity.Reimburse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报销Dao接口
 *
 * @author h
 */
@Mapper
public interface ReimburseDao extends BaseMapper<Reimburse> {
}
