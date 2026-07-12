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
import net.togogo.dto.BorrowRecordDTO;
import net.togogo.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class MyBorrowTab {

    private final AppContext ctx;

    public MyBorrowTab(AppContext ctx) {
        this.ctx = ctx;
    }

    @SuppressWarnings("unchecked")
    public Tab build() {
        Tab tab = new Tab("我的借阅");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Button refreshBtn = new Button("刷新");
        refreshBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");

        TableView<BorrowRecordDTO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<BorrowRecordDTO, Long> idCol = new TableColumn<>("记录ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<BorrowRecordDTO, String> bookTitleCol = new TableColumn<>("书名");
        bookTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        TableColumn<BorrowRecordDTO, String> borrowTimeCol = new TableColumn<>("借阅时间");
        borrowTimeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getBorrowTime() != null ? c.getValue().getBorrowTime().toString().replace("T", " ") : ""));
        TableColumn<BorrowRecordDTO, String> dueTimeCol = new TableColumn<>("应还时间");
        dueTimeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDueTime() != null ? c.getValue().getDueTime().toString().replace("T", " ") : ""));
        TableColumn<BorrowRecordDTO, String> returnTimeCol = new TableColumn<>("归还时间");
        returnTimeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getReturnTime() != null ? c.getValue().getReturnTime().toString().replace("T", " ") : "未归还"));
        TableColumn<BorrowRecordDTO, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(c -> {
            BorrowRecordDTO dto = c.getValue();
            if (dto.getStatus() == BorrowRecord.Borrowstatus.RETURNED) return new SimpleStringProperty("已归还");
            if (dto.getOverdueDays() != null && dto.getOverdueDays() > 0) return new SimpleStringProperty("逾期 " + dto.getOverdueDays() + " 天");
            return new SimpleStringProperty("借阅中");
        });
        TableColumn<BorrowRecordDTO, Integer> renewCol = new TableColumn<>("续借次数");
        renewCol.setCellValueFactory(new PropertyValueFactory<>("renewCount"));

        table.getColumns().addAll(idCol, bookTitleCol, borrowTimeCol, dueTimeCol, returnTimeCol, statusCol, renewCol);

        HBox pageBox = new HBox(10);
        pageBox.setAlignment(Pos.CENTER);
        Label pageInfo = new Label();
        Button prevBtn = new Button("上一页");
        Button nextBtn = new Button("下一页");
        pageBox.getChildren().addAll(prevBtn, pageInfo, nextBtn);

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);
        Button returnBtn = new Button("归还");
        returnBtn.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white;");
        Button renewBtn = new Button("续借");
        renewBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");
        actionBox.getChildren().addAll(refreshBtn, returnBtn, renewBtn);

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        root.getChildren().addAll(table, pageBox, actionBox, statusLabel);

        ObservableList<BorrowRecordDTO> data = FXCollections.observableArrayList();
        table.setItems(data);
        final int[] currentPage = {0};
        final int pageSize = 10;
        final int[] totalPages = {0};

        Runnable loadRecords = () -> {
            statusLabel.setText("加载中...");
            ctx.getExecutorService().submit(() -> {
                try {
                    Long userId = ctx.getCurrentUser().getId();
                    Pageable pageable = PageRequest.of(currentPage[0], pageSize, Sort.by(Sort.Direction.DESC, "borrowTime"));
                    Page<BorrowRecordDTO> page = ctx.getBookService().getBorrowRecordsByUser(userId, pageable);
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

        prevBtn.setOnAction(e -> { if (currentPage[0] > 0) { currentPage[0]--; loadRecords.run(); } });
        nextBtn.setOnAction(e -> { if (currentPage[0] < totalPages[0] - 1) { currentPage[0]++; loadRecords.run(); } });
        refreshBtn.setOnAction(e -> { currentPage[0] = 0; loadRecords.run(); });

        returnBtn.setOnAction(e -> {
            BorrowRecordDTO selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { statusLabel.setText("请选择一条借阅记录"); return; }
            if (selected.getStatus() == BorrowRecord.Borrowstatus.RETURNED) { statusLabel.setText("该书已归还"); return; }
            statusLabel.setText("处理中...");
            returnBtn.setDisable(true);
            ctx.getExecutorService().submit(() -> {
                try {
                    BorrowRecordDTO result = ctx.getBookService().returnBook(selected.getId());
                    Platform.runLater(() -> {
                        returnBtn.setDisable(false);
                        statusLabel.setTextFill(Color.GREEN); 
                        statusLabel.setText("归还成功"); 
                        loadRecords.run(); 
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> { 
                        returnBtn.setDisable(false); 
                        statusLabel.setTextFill(Color.RED); 
                        statusLabel.setText("归还失败：" + ex.getMessage()); 
                    });
                }
            });
        });

        renewBtn.setOnAction(e -> {
            BorrowRecordDTO selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { statusLabel.setText("请选择一条借阅记录"); return; }
            if (selected.getStatus() == BorrowRecord.Borrowstatus.RETURNED) { statusLabel.setText("该书已归还，无法续借"); return; }
            if (selected.getRenewCount() != null && selected.getRenewCount() >= 2) { statusLabel.setText("已达到最大续借次数（2次）"); return; }
            statusLabel.setText("处理中...");
            renewBtn.setDisable(true);
            ctx.getExecutorService().submit(() -> {
                try {
                    BorrowRecordDTO result = ctx.getBookService().renewBook(selected.getId());
                    Platform.runLater(() -> {
                        renewBtn.setDisable(false);
                        statusLabel.setTextFill(Color.GREEN); 
                        statusLabel.setText("续借成功"); 
                        loadRecords.run(); 
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> { 
                        renewBtn.setDisable(false); 
                        statusLabel.setTextFill(Color.RED); 
                        statusLabel.setText("续借失败：" + ex.getMessage()); 
                    });
                }
            });
        });

        loadRecords.run();
        tab.setContent(root);
        return tab;
    }
}