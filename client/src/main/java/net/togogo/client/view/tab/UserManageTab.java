package net.togogo.client.view.tab;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import net.togogo.client.AppContext;
import net.togogo.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class UserManageTab {

    private final AppContext ctx;

    public UserManageTab(AppContext ctx) {
        this.ctx = ctx;
    }

    public Tab build() {
        Tab tab = new Tab("用户管理");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        TableView<UserDTO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<UserDTO, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<UserDTO, String> usernameCol = new TableColumn<>("用户名");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<UserDTO, String> emailCol = new TableColumn<>("邮箱");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<UserDTO, String> phoneCol = new TableColumn<>("手机号");
        phoneCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPhoneNumber() != null ? c.getValue().getPhoneNumber() : ""));
        TableColumn<UserDTO, String> roleCol = new TableColumn<>("角色");
        roleCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRole() != null ? c.getValue().getRole().name() : ""));
        TableColumn<UserDTO, String> createTimeCol = new TableColumn<>("创建时间");
        createTimeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreateTime() != null ? c.getValue().getCreateTime().toString().replace("T", " ") : ""));

        table.getColumns().addAll(idCol, usernameCol, emailCol, phoneCol, roleCol, createTimeCol);

        HBox pageBox = new HBox(10);
        pageBox.setAlignment(Pos.CENTER);
        Label pageInfo = new Label();
        Button prevBtn = new Button("上一页");
        Button nextBtn = new Button("下一页");
        pageBox.getChildren().addAll(prevBtn, pageInfo, nextBtn);

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);
        Button refreshBtn = new Button("刷新");
        refreshBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");
        Button deleteUserBtn = new Button("删除用户");
        deleteUserBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
        actionBox.getChildren().addAll(refreshBtn, deleteUserBtn);

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        root.getChildren().addAll(table, pageBox, actionBox, statusLabel);

        ObservableList<UserDTO> data = FXCollections.observableArrayList();
        table.setItems(data);
        final int[] currentPage = {0};
        final int pageSize = 10;
        final int[] totalPages = {0};

        Runnable loadData = () -> {
            statusLabel.setText("加载中...");
            ctx.getExecutorService().submit(() -> {
                try {
                    Pageable pageable = PageRequest.of(currentPage[0], pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
                    Page<UserDTO> page = ctx.getUserService().getAllUsers(pageable);
                    Platform.runLater(() -> {
                        data.setAll(page.getContent());
                        totalPages[0] = page.getTotalPages();
                        pageInfo.setText("第 " + (currentPage[0] + 1) + " / " + Math.max(1, totalPages[0]) +
                                " 页 (共 " + page.getTotalElements() + " 条)");
                        prevBtn.setDisable(currentPage[0] <= 0);
                        nextBtn.setDisable(currentPage[0] >= totalPages[0] - 1);
                        statusLabel.setText("");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> statusLabel.setText("加载失败：" + ex.getMessage()));
                }
            });
        };

        prevBtn.setOnAction(e -> { if (currentPage[0] > 0) { currentPage[0]--; loadData.run(); } });
        nextBtn.setOnAction(e -> { if (currentPage[0] < totalPages[0] - 1) { currentPage[0]++; loadData.run(); } });
        refreshBtn.setOnAction(e -> loadData.run());

        deleteUserBtn.setOnAction(e -> {
            UserDTO selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { statusLabel.setText("请选择要删除的用户"); return; }
            if (selected.getId().equals(ctx.getCurrentUser().getId())) { statusLabel.setText("不能删除自己"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定删除用户「" + selected.getUsername() + "」？");
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    statusLabel.setText("删除中...");
                    ctx.getExecutorService().submit(() -> {
                        try {
                            ctx.getUserService().deleteUser(selected.getId());
                            Platform.runLater(() -> {
                                statusLabel.setTextFill(Color.GREEN); 
                                statusLabel.setText("删除成功"); 
                                loadData.run(); 
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() -> { 
                                statusLabel.setTextFill(Color.RED); 
                                statusLabel.setText("删除失败：" + ex.getMessage()); 
                            });
                        }
                    });
                }
            });
        });

        loadData.run();
        tab.setContent(root);
        return tab;
    }
}