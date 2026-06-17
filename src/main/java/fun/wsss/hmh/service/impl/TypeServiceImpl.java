package fun.wsss.hmh.service.impl;

import fun.wsss.hmh.dao.TypeDao;
import fun.wsss.hmh.entity.Type;
import fun.wsss.hmh.service.TypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 类型Service实现类
 * @author h
 */
@Service("typeService")
public class TypeServiceImpl implements TypeService {

	@Autowired
	private TypeDao typeDao;

	/**
	 * 查询所有类型
	 * @return 类型列表
	 */
	@Override
	public List<Type> list() {
		return typeDao.list();
	}
}