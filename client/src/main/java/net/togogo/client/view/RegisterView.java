package net.togogo.client.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import net.togogo.client.AppContext;
import net.togogo.dto.RegisterRequest;
import net.togogo.dto.UserDTO;

public class RegisterView {

    private final AppContext ctx;
    private final Runnable onBackToLogin;

    public RegisterView(AppContext ctx, Runnable onBackToLogin) {
        this.ctx = ctx;
        this.onBackToLogin = onBackToLogin;
    }

    public Parent build() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(60, 40, 40, 40));
        root.setAlignment(Pos.CENTER);
        root.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));

        Label titleLabel = new Label("用户注册");
        titleLabel.setFont(Font.font("Arial", 28));
        titleLabel.setTextFill(Color.DARKSLATEBLUE);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        TextField usernameField = createField(grid, "用户名：", "请输入用户名（3-50字符）", 0);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码（6-20字符）");
        passwordField.setPrefWidth(300);
        grid.add(new Label("密码："), 0, 1);
        grid.add(passwordField, 1, 1);

        TextField phoneField = createField(grid, "手机号：", "请输入11位手机号", 2);

        Button registerBtn = new Button("注 册");
        registerBtn.setPrefWidth(300);
        registerBtn.setPrefHeight(40);
        registerBtn.setFont(Font.font("Arial", 14));
        registerBtn.setStyle("-fx-background-color: #5CB85C; -fx-text-fill: white; -fx-border-radius: 5;");

        Button backBtn = new Button("返 回");
        backBtn.setPrefWidth(300);
        backBtn.setPrefHeight(40);
        backBtn.setFont(Font.font("Arial", 14));
        backBtn.setStyle("-fx-background-color: #777; -fx-text-fill: white; -fx-border-radius: 5;");

        Label statusLabel = new Label("");
        statusLabel.setMinHeight(25);
        statusLabel.setFont(Font.font("Arial", 12));
        statusLabel.setTextFill(Color.RED);

        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String phone = phoneField.getText().trim();

            if (username.isEmpty()) { statusLabel.setText("用户名不能为空"); return; }
            if (username.length() < 3 || username.length() > 50) { statusLabel.setText("用户名长度必须在3-50字符之间"); return; }
            if (password.isEmpty()) { statusLabel.setText("密码不能为空"); return; }
            if (password.length() < 6 || password.length() > 20) { statusLabel.setText("密码长度必须在6-20字符之间"); return; }
            if (phone.isEmpty()) { statusLabel.setText("手机号不能为空"); return; }
            if (phone.length() != 11) { statusLabel.setText("手机号必须为11位"); return; }

            statusLabel.setText("注册中...");
            registerBtn.setDisable(true);

            ctx.getExecutorService().submit(() -> {
                try {
                    RegisterRequest req = new RegisterRequest();
                    req.setUsername(username);
                    req.setPassword(password);
                    req.setPhone(phone);
                    UserDTO result = ctx.getUserService().register(req);
                    Platform.runLater(() -> {
                        registerBtn.setDisable(false);
                        statusLabel.setTextFill(Color.GREEN);
                        statusLabel.setText("注册成功！即将跳转...");
                        new Thread(() -> {
                            try { Thread.sleep(1500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                            Platform.runLater(onBackToLogin);
                        }).start();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        registerBtn.setDisable(false);
                        statusLabel.setTextFill(Color.RED);
                        statusLabel.setText("注册失败：" + ex.getMessage());
                    });
                }
            });
        });

        backBtn.setOnAction(e -> onBackToLogin.run());

        root.getChildren().addAll(titleLabel, grid, registerBtn, backBtn, statusLabel);
        return root;
    }

    private TextField createField(GridPane grid, String labelText, String prompt, int row) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", 14));
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(300);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        return field;
    }
}