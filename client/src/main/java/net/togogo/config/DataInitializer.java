package net.togogo.config;

import net.togogo.entity.Inventory;
import net.togogo.entity.User;
import net.togogo.repository.InventoryRepository;
import net.togogo.repository.UserRepository;
import net.togogo.util.PasswordUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, InventoryRepository inventoryRepository) {
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

            if (inventoryRepository.count() == 0) {
                inventoryRepository.save(Inventory.builder()
                        .productName("无线蓝牙耳机")
                        .productCode("PRD-001")
                        .barcode("6901234567890")
                        .supplier("深圳电子科技有限公司")
                        .category("电子产品")
                        .description("高品质无线蓝牙耳机，支持主动降噪")
                        .quantity(100)
                        .unitPrice(299.0)
                        .minStock(10)
                        .location("A区-01货架")
                        .build());

                inventoryRepository.save(Inventory.builder()
                        .productName("机械键盘")
                        .productCode("PRD-002")
                        .barcode("6901234567891")
                        .supplier("东莞数码配件厂")
                        .category("电脑配件")
                        .description("RGB背光机械键盘，青轴")
                        .quantity(50)
                        .unitPrice(459.0)
                        .minStock(5)
                        .location("A区-02货架")
                        .build());

                inventoryRepository.save(Inventory.builder()
                        .productName("运动T恤")
                        .productCode("PRD-003")
                        .barcode("6901234567892")
                        .supplier("广州服装有限公司")
                        .category("服装")
                        .description("纯棉透气运动T恤，多种颜色")
                        .quantity(200)
                        .unitPrice(89.0)
                        .minStock(20)
                        .location("B区-01货架")
                        .build());

                inventoryRepository.save(Inventory.builder()
                        .productName("办公笔记本")
                        .productCode("PRD-004")
                        .barcode("6901234567893")
                        .supplier("义乌文具批发")
                        .category("办公用品")
                        .description("A5办公笔记本，100页")
                        .quantity(500)
                        .unitPrice(15.0)
                        .minStock(50)
                        .location("C区-01货架")
                        .build());

                inventoryRepository.save(Inventory.builder()
                        .productName("保温杯")
                        .productCode("PRD-005")
                        .barcode("6901234567894")
                        .supplier("浙江永康五金")
                        .category("日用品")
                        .description("304不锈钢保温杯，500ml")
                        .quantity(150)
                        .unitPrice(69.0)
                        .minStock(15)
                        .location("B区-02货架")
                        .build());
            }
        };
    }
}