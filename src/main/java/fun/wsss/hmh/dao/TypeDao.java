package fun.wsss.hmh.dao;

import fun.wsss.hmh.entity.Type;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 报销类型Dao
 *
 * @author h
 */
@Repository
public interface TypeDao {

    /**
     * 查询所有报销类型
     *
     * @return 报销类型列表
     */
    public List<Type> list();
}
