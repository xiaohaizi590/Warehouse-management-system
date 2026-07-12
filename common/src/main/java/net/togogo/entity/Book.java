// common/src/main/java/net/togogo/entity/Book.java
package net.togogo.entity;

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
@Table(name = "t_book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;           // 书名

    @Column(nullable = false, length = 100)
    private String author;          // 作者

    @Column(length = 50,unique = true)
    private String isbn;            // ISBN编号

    @Column(length = 100)
    private String publisher;       // 出版社

    @Column(name = "publish_date")
    private LocalDateTime publishDate; // 出版日期

    @Column(length = 50)
    private String category;        // 分类（如：科技、文学、历史）

    @Column(columnDefinition = "TEXT")
    private String description;     // 简介

    @Column(nullable = false)
    private Integer stock;          // 库存数量

    @Column(nullable = false)
    private Integer available;      // 可借数量

    @Column(name = "create_time")
    private LocalDateTime createTime; // 创建时间

    @Column(name = "update_time")
    private LocalDateTime updateTime; // 更新时间

    @Version private Integer version;// 乐观锁版本号

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime =LocalDateTime.now();
        }
        if (available == null) {
            available = stock;
        }
    }
    @PreUpdate
    protected void onUpdate() {
        if (available != null && stock!=null &&available > stock) {
            throw new IllegalStateException("可借数量不能大于库存数量");
        }


    }
}