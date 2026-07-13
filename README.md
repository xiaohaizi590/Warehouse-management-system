# Warehouse management system

基于 Spring Boot 和 JavaFX 的单体仓库管理系统。//这边主要是要改成仓库管理系统，属于小店类型的，具有应用空间

## 技术栈

- Java 17
- Spring Boot 3.2.0
- Spring Security 6.x
- Spring Data JPA
- JWT (JJWT 0.12.3)
- MySQL 8.x
- JavaFX 21.0.2
- Lombok

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x

### 2. 数据库配置

```sql
CREATE DATABASE nacon72_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 启动应用

```bash
# 进入项目根目录
cd Library-System-JavaFX

# 使用 Maven 插件启动（推荐）
mvn javafx:run -pl client -am
```

### 4. 登录账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | ADMIN |
| user | user123 | USER |

## 项目结构

```
Library-System-JavaFX/
├── pom.xml                           # 父POM
├── common/                           # 公共模块（实体、DTO）
├── client/                           # 单体应用主模块
│   └── src/main/java/net/togogo/
│       ├── client/                   # JavaFX客户端代码
│       ├── config/                   # Spring配置
│       ├── repository/               # JPA数据访问层
│       ├── security/                 # 安全组件
│       └── service/                  # 业务逻辑层
└── docs/
    └── 技术指南.md                    # 详细技术文档
```

## 功能特性

- 用户注册、登录
- JWT 令牌认证
- 图书浏览、搜索
- 图书借阅、归还、续借
- 图书管理（管理员）
- 用户管理（管理员）
- 借阅管理（管理员）

## 详细文档

请查看 [docs/技术指南.md](docs/技术指南.md) 获取完整技术文档。
