package fun.wsss.hmh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 应用程序入口
 */
@MapperScan("fun.wsss.hmh.dao")
@SpringBootApplication
@EnableTransactionManagement
public class HmhApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HmhApplication.class, args);
    }
}
