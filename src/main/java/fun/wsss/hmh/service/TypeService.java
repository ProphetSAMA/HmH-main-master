package fun.wsss.hmh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.wsss.hmh.entity.Type;

import java.util.List;

/**
 * 报销类型服务接口
 */
public interface TypeService extends IService<Type> {
    
    /**
     * 获取所有报销类型
     * @return 报销类型列表
     */
    List<Type> getAllTypes();
    
    /**
     * 根据ID获取报销类型
     * @param id 类型ID
     * @return 报销类型
     */
    Type getTypeById(Integer id);
}
