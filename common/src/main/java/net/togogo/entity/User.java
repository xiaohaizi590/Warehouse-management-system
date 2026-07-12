package net.togogo.entity;
//数据库实体类，对应数据库中的表这类的操作

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name= "t_user")//对应数据库中的表名

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    private String email;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "phone_number",nullable = false,unique = true,length = 11)
    private String phone;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)//将枚举类型映射为字符串存储在数据库中
    private Role role;

    /**
     * JPA 生命周期回调方法：在实体对象被持久化（插入数据库）之前自动执行。
     * 作用：如果创建时间为空，则自动设置为当前系统时间。
     * 好处：业务代码中无需手动 setCreateTime(new Date())，框架自动帮你填充。
     */
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (role==null){
            role=Role.USER;
        }
    }

    public enum Role {
        USER,
        ADMIN
    }
}
