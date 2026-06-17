package fun.wsss.hmh.entity;

import lombok.Data;

/**
 * 分页Bean
 */
@Data
public class PageBean {
    
    /**
     * 当前页码
     */
    private int page;
    
    /**
     * 每页记录数
     */
    private int pageSize;
    
    /**
     * 起始记录索引
     */
    private int start;
    
    /**
     * 构造函数
     * @param page 当前页码
     * @param pageSize 每页记录数
     */
    public PageBean(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
        this.start = (page - 1) * pageSize;
    }
    
    /**
     * 获取起始记录索引
     * @return 起始记录索引
     */
    public int getStart() {
        return start;
    }
    
    /**
     * 获取每页记录数
     * @return 每页记录数
     */
    public int getPageSize() {
        return pageSize;
    }
    
    /**
     * 获取当前页码
     * @return 当前页码
     */
    public int getPage() {
        return page;
    }
}
