package net.togogo.client.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import net.togogo.client.AppContext;
import net.togogo.client.view.tab.*;
import net.togogo.dto.LoginResponse;

public class MainView {

    private final AppContext ctx;
    private final Runnable onLogout;

    public MainView(AppContext ctx, Runnable onLogout) {
        this.ctx = ctx;
        this.onLogout = onLogout;
    }

    public Parent build() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 14px;");

        tabPane.getTabs().addAll(
                new BookListTab(ctx).build(),
                new MyBorrowTab(ctx).build(),
                new ProfileTab(ctx).build()
        );

        LoginResponse user = ctx.getCurrentUser();
        if (user != null && "ADMIN".equals(user.getRole())) {
            tabPane.getTabs().addAll(
                    new BookManageTab(ctx).build(),
                    new UserManageTab(ctx).build(),
                    new BorrowManageTab(ctx).build()
            );
        }

        BorderPane mainPane = new BorderPane();
        mainPane.setTop(createTopBar());
        mainPane.setCenter(tabPane);
        return mainPane;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setStyle("-fx-background-color: #2C3E50;");

        LoginResponse user = ctx.getCurrentUser();
        String roleText = (user != null && "ADMIN".equals(user.getRole())) ? " (管理员)" : "";
        Label welcomeLabel = new Label("欢迎，" + (user != null ? user.getUsername() : "") + roleText);
        welcomeLabel.setFont(Font.font("Arial", 14));
        welcomeLabel.setTextFill(Color.WHITE);

        Button logoutBtn = new Button("退出登录");
        logoutBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 12px;");
        logoutBtn.setOnAction(e -> onLogout.run());

        topBar.getChildren().addAll(welcomeLabel, logoutBtn);
        return topBar;
    }
}
