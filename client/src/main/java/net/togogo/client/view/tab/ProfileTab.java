package net.togogo.client.view.tab;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import net.togogo.client.AppContext;
import net.togogo.dto.UserDTO;

public class ProfileTab {

    private final AppContext ctx;

    public ProfileTab(AppContext ctx) {
        this.ctx = ctx;
    }

    public Tab build() {
        Tab tab = new Tab("个人信息");
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("个人中心");
        titleLabel.setFont(Font.font("Arial", 24));
        titleLabel.setTextFill(Color.DARKSLATEBLUE);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(15);
        infoGrid.setAlignment(Pos.CENTER);

        Label idValue = addRow(infoGrid, "用户ID：", 0);
        Label usernameValue = addRow(infoGrid, "用户名：", 1);
        Label emailValue = addRow(infoGrid, "邮箱：", 2);
        Label phoneValue = addRow(infoGrid, "手机号：", 3);
        Label roleValue = addRow(infoGrid, "角色：", 4);
        Label createTimeValue = addRow(infoGrid, "创建时间：", 5);

        Button refreshInfoBtn = new Button("刷新信息");
        refreshInfoBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 14px;");
        refreshInfoBtn.setPrefWidth(200);

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        root.getChildren().addAll(titleLabel, infoGrid, refreshInfoBtn, statusLabel);

        Runnable loadProfile = () -> {
            statusLabel.setText("加载中...");
            ctx.getExecutorService().submit(() -> {
                try {
                    UserDTO dto = ctx.getUserService().getUserById(ctx.getCurrentUser().getId());
                    Platform.runLater(() -> {
                        idValue.setText(dto.getId() != null ? dto.getId().toString() : "-");
                        usernameValue.setText(dto.getUsername() != null ? dto.getUsername() : "-");
                        emailValue.setText(dto.getEmail() != null ? dto.getEmail() : "-");
                        phoneValue.setText(dto.getPhoneNumber() != null ? dto.getPhoneNumber() : "-");
                        roleValue.setText(dto.getRole() != null ? dto.getRole().name() : "-");
                        createTimeValue.setText(dto.getCreateTime() != null ? dto.getCreateTime().toString().replace("T", " ") : "-");
                        statusLabel.setText("");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> statusLabel.setText("加载失败：" + ex.getMessage()));
                }
            });
        };

        refreshInfoBtn.setOnAction(e -> loadProfile.run());
        loadProfile.run();
        tab.setContent(root);
        return tab;
    }

    private static Label addRow(GridPane grid, String labelText, int row) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", 14));
        Label value = new Label();
        value.setFont(Font.font("Arial", 14));
        grid.add(label, 0, row);
        grid.add(value, 1, row);
        return value;
    }
}