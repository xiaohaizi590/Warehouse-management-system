package net.togogo.client.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import net.togogo.client.AppContext;
import net.togogo.dto.LoginRequest;
import net.togogo.dto.LoginResponse;

public class LoginView {

    private final AppContext ctx;
    private final Runnable onLoginSuccess;

    public LoginView(AppContext ctx, Runnable onLoginSuccess) {
        this.ctx = ctx;
        this.onLoginSuccess = onLoginSuccess;
    }

    public Parent build() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(80, 60, 60, 60));
        root.setAlignment(Pos.CENTER);
        root.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));

        Label titleLabel = new Label("图书管理系统");
        titleLabel.setFont(Font.font("Arial", 32));
        titleLabel.setTextFill(Color.DARKSLATEBLUE);

        Label subtitleLabel = new Label("用户登录");
        subtitleLabel.setFont(Font.font("Arial", 18));
        subtitleLabel.setTextFill(Color.GRAY);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        Label accountLabel = new Label("账号：");
        accountLabel.setFont(Font.font("Arial", 14));
        TextField accountField = new TextField();
        accountField.setPromptText("请输入用户名");
        accountField.setPrefWidth(300);

        Label passwordLabel = new Label("密码：");
        passwordLabel.setFont(Font.font("Arial", 14));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码");
        passwordField.setPrefWidth(300);

        grid.add(accountLabel, 0, 0);
        grid.add(accountField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);

        Button loginBtn = new Button("登 录");
        loginBtn.setPrefWidth(300);
        loginBtn.setPrefHeight(40);
        loginBtn.setFont(Font.font("Arial", 14));
        loginBtn.setStyle("-fx-background-color: #4A90D9; -fx-text-fill: white; -fx-border-radius: 5;");

        Button registerBtn = new Button("注 册");
        registerBtn.setPrefWidth(300);
        registerBtn.setPrefHeight(40);
        registerBtn.setFont(Font.font("Arial", 14));
        registerBtn.setStyle("-fx-background-color: #5CB85C; -fx-text-fill: white; -fx-border-radius: 5;");

        Label statusLabel = new Label("");
        statusLabel.setMinHeight(25);
        statusLabel.setFont(Font.font("Arial", 12));
        statusLabel.setTextFill(Color.RED);

        accountField.setOnAction(e -> loginBtn.fire());
        passwordField.setOnAction(e -> loginBtn.fire());

        loginBtn.setOnAction(e -> {
            String account = accountField.getText().trim();
            String password = passwordField.getText().trim();
            if (account.isEmpty()) { statusLabel.setText("账号不能为空"); return; }
            if (password.isEmpty()) { statusLabel.setText("密码不能为空"); return; }

            statusLabel.setText("登录中...");
            loginBtn.setDisable(true);

            ctx.getExecutorService().submit(() -> {
                try {
                    LoginRequest request = new LoginRequest();
                    request.setAccount(account);
                    request.setPassword(password);
                    LoginResponse result = ctx.getAuthenticationService().login(request);
                    Platform.runLater(() -> {
                        loginBtn.setDisable(false);
                        ctx.setCurrentUser(result);
                        ctx.getTokenStore().setToken(result.getToken());
                        onLoginSuccess.run();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        loginBtn.setDisable(false);
                        statusLabel.setTextFill(Color.RED);
                        statusLabel.setText("登录失败：" + ex.getMessage());
                    });
                }
            });
        });

        registerBtn.setOnAction(e -> {
            Scene scene = new Scene(new RegisterView(ctx, () -> {
                ctx.getPrimaryStage().setScene(SceneCache.getLoginScene(ctx, onLoginSuccess));
            }).build());
            ctx.getPrimaryStage().setScene(scene);
        });

        root.getChildren().addAll(titleLabel, subtitleLabel, grid, loginBtn, registerBtn, statusLabel);
        return root;
    }
}