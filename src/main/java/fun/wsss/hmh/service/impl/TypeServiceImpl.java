package fun.wsss.hmh.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.wsss.hmh.dao.TypeDao;
import fun.wsss.hmh.entity.Type;
import fun.wsss.hmh.service.TypeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 报销类型服务实现类
 */
@Service
public class TypeServiceImpl extends ServiceImpl<TypeDao, Type> implements TypeService {
    
    @Override
    public List<Type> getAllTypes() {
        return this.list();
    }
    
    @Override
    public Type getTypeById(Integer id) {
        return this.getById(id);
    }
}