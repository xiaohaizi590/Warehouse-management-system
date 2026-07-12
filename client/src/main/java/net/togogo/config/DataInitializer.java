package net.togogo.config;

import net.togogo.entity.User;
import net.togogo.repository.UserRepository;
import net.togogo.util.PasswordUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = User.builder()
                        .username("admin")
                        .password(PasswordUtil.encode("admin123"))
                        .phone("13800138000")
                        .role(User.Role.ADMIN)
                        .build();
                userRepository.save(admin);

                User user = User.builder()
                        .username("user")
                        .password(PasswordUtil.encode("user123"))
                        .phone("13800138001")
                        .role(User.Role.USER)
                        .build();
                userRepository.save(user);
            }
        };
    }
}