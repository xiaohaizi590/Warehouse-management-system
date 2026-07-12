package net.togogo.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "net.togogo")
@EnableJpaRepositories(basePackages = "net.togogo.repository")
@EntityScan(basePackages = "net.togogo.entity")
public class ClientApplication {

}