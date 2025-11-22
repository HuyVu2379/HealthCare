package fit.iut.student.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableRetry 
public class PaymentServiceApplication {

    public static void main(String[] args) {
        java.security.Security.setProperty("networkaddress.cache.ttl", "0");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");

        SpringApplication.run(PaymentServiceApplication.class, args);
    }

}
