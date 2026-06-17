package fun.wsss.hmh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "fun.wsss.hmh")
@MapperScan("fun.wsss.hmh.dao")
public class HmhApplication {


    public static void main(String[] args) {
        SpringApplication.run(HmhApplication.class, args);
        System.out.println("程序已启动");
    }

}
