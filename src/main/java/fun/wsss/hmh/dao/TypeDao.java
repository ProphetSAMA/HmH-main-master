package fun.wsss.hmh.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.wsss.hmh.entity.Type;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报销类型Dao接口
 */
@Mapper
public interface TypeDao extends BaseMapper<Type> {
}
