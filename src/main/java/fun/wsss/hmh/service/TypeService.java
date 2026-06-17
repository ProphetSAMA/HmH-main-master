package fun.wsss.hmh.service;



import fun.wsss.hmh.entity.Type;

import java.util.List;


/**
 * 类型Service接口
 * @author h
 *
 */
public interface TypeService {

	/**
	 * 查询所有类型
	 * @return 类型列表
	 */
	List<Type> list();
	
}
