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

public class BorrowManageTab {

    private final AppContext ctx;

    public BorrowManageTab(AppContext ctx) {
        this.ctx = ctx;
    }

    public Tab build() {
        Tab tab = new Tab("借阅管理");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        HBox toolbar = new HBox(10);
        Button refreshBtn = new Button("刷新");
        refreshBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");
        Button allBtn = new Button("查看所有");
        allBtn.setStyle("-fx-background-color: #2C3E50; -fx-text-fill: white;");
        Button overdueBtn = new Button("查看逾期");
        overdueBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
        toolbar.getChildren().addAll(refreshBtn, allBtn, overdueBtn);

        TableView<BorrowRecordDTO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<BorrowRecordDTO, Long> idCol = new TableColumn<>("记录ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<BorrowRecordDTO, Long> bookIdCol = new TableColumn<>("图书ID");
        bookIdCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        TableColumn<BorrowRecordDTO, String> bookTitleCol = new TableColumn<>("书名");
        bookTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        TableColumn<BorrowRecordDTO, Long> userIdCol = new TableColumn<>("用户ID");
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        TableColumn<BorrowRecordDTO, String> userNameCol = new TableColumn<>("用户名");
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
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

        table.getColumns().addAll(idCol, bookIdCol, bookTitleCol, userIdCol, userNameCol, borrowTimeCol, dueTimeCol, returnTimeCol, statusCol);

        HBox pageBox = new HBox(10);
        pageBox.setAlignment(Pos.CENTER);
        Label pageInfo = new Label();
        Button prevBtn = new Button("上一页");
        Button nextBtn = new Button("下一页");
        pageBox.getChildren().addAll(prevBtn, pageInfo, nextBtn);

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        root.getChildren().addAll(toolbar, table, pageBox, statusLabel);

        ObservableList<BorrowRecordDTO> data = FXCollections.observableArrayList();
        table.setItems(data);
        final int[] currentPage = {0};
        final int pageSize = 10;
        final int[] totalPages = {0};
        final boolean[] showOverdueOnly = {false};

        Runnable loadData = () -> {
            statusLabel.setText("加载中...");
            ctx.getExecutorService().submit(() -> {
                try {
                    Page<BorrowRecordDTO> page;
                    Pageable pageable = PageRequest.of(currentPage[0], pageSize, Sort.by(Sort.Direction.DESC, "borrowTime"));
                    if (showOverdueOnly[0]) {
                        page = ctx.getBookService().getOverdueRecords(pageable);
                    } else {
                        page = ctx.getBookService().getAllBorrowRecords(pageable);
                    }
                    Platform.runLater(() -> {
                        data.setAll(page.getContent());
                        totalPages[0] = page.getTotalPages();
                        String mode = showOverdueOnly[0] ? "逾期记录" : "全部借阅";
                        pageInfo.setText("第 " + (currentPage[0] + 1) + " / " + Math.max(1, totalPages[0]) +
                                " 页 (共 " + page.getTotalElements() + " 条 " + mode + ")");
                        prevBtn.setDisable(currentPage[0] <= 0);
                        nextBtn.setDisable(currentPage[0] >= totalPages[0] - 1);
                        statusLabel.setText("");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> statusLabel.setText("查询失败：" + ex.getMessage()));
                }
            });
        };

        prevBtn.setOnAction(e -> { if (currentPage[0] > 0) { currentPage[0]--; loadData.run(); } });
        nextBtn.setOnAction(e -> { if (currentPage[0] < totalPages[0] - 1) { currentPage[0]++; loadData.run(); } });
        refreshBtn.setOnAction(e -> { currentPage[0] = 0; loadData.run(); });

        allBtn.setOnAction(e -> { showOverdueOnly[0] = false; currentPage[0] = 0; loadData.run(); });
        overdueBtn.setOnAction(e -> { showOverdueOnly[0] = true; currentPage[0] = 0; loadData.run(); });

        loadData.run();
        tab.setContent(root);
        return tab;
    }
}