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
import net.togogo.dto.StockRecordDTO;
import net.togogo.entity.StockRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class MyStockTab {

    private final AppContext ctx;

    public MyStockTab(AppContext ctx) {
        this.ctx = ctx;
    }

    @SuppressWarnings("unchecked")
    public Tab build() {
        Tab tab = new Tab("我的记录");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button refreshBtn = new Button("刷新");
        refreshBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");
        Button allBtn = new Button("全部");
        Button inBtn = new Button("入库");
        inBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white;");
        Button outBtn = new Button("出库");
        outBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
        toolbar.getChildren().addAll(refreshBtn, allBtn, inBtn, outBtn);

        TableView<StockRecordDTO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<StockRecordDTO, Long> idCol = new TableColumn<>("记录ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<StockRecordDTO, String> productNameCol = new TableColumn<>("商品名称");
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        TableColumn<StockRecordDTO, String> productCodeCol = new TableColumn<>("商品编码");
        productCodeCol.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        TableColumn<StockRecordDTO, String> recordTimeCol = new TableColumn<>("操作时间");
        recordTimeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRecordTime() != null ? c.getValue().getRecordTime().toString().replace("T", " ") : ""));
        TableColumn<StockRecordDTO, Integer> quantityCol = new TableColumn<>("数量");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<StockRecordDTO, Double> unitPriceCol = new TableColumn<>("单价");
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        TableColumn<StockRecordDTO, Double> totalAmountCol = new TableColumn<>("总金额");
        totalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        TableColumn<StockRecordDTO, String> typeCol = new TableColumn<>("类型");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStockType() == StockRecord.StockType.IN ? "入库" : "出库"));
        TableColumn<StockRecordDTO, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(c -> {
            StockRecordDTO dto = c.getValue();
            if (dto.getStatus() == StockRecord.StockStatus.COMPLETED) return new SimpleStringProperty("已完成");
            if (dto.getStatus() == StockRecord.StockStatus.PENDING) return new SimpleStringProperty("待处理");
            return new SimpleStringProperty("已取消");
        });

        table.getColumns().addAll(idCol, productNameCol, productCodeCol, recordTimeCol,
                quantityCol, unitPriceCol, totalAmountCol, typeCol, statusCol);

        HBox pageBox = new HBox(10);
        pageBox.setAlignment(Pos.CENTER);
        Label pageInfo = new Label();
        Button prevBtn = new Button("上一页");
        Button nextBtn = new Button("下一页");
        pageBox.getChildren().addAll(prevBtn, pageInfo, nextBtn);

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        root.getChildren().addAll(toolbar, table, pageBox, statusLabel);

        ObservableList<StockRecordDTO> data = FXCollections.observableArrayList();
        table.setItems(data);
        final int[] currentPage = {0};
        final int pageSize = 10;
        final int[] totalPages = {0};

        Runnable loadRecords = () -> {
            statusLabel.setText("加载中...");
            ctx.getExecutorService().submit(() -> {
                try {
                    Long userId = ctx.getCurrentUser().getId();
                    Pageable pageable = PageRequest.of(currentPage[0], pageSize, Sort.by(Sort.Direction.DESC, "recordTime"));
                    Page<StockRecordDTO> page = ctx.getInventoryService().getStockRecordsByUser(userId, pageable);
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

        allBtn.setOnAction(e -> { currentPage[0] = 0; loadRecords.run(); });
        inBtn.setOnAction(e -> loadRecords.run());
        outBtn.setOnAction(e -> loadRecords.run());

        loadRecords.run();
        tab.setContent(root);
        return tab;
    }
}