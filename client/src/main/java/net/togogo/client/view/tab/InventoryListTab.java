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
import net.togogo.dto.InventoryDTO;
import net.togogo.dto.StockRequest;
import net.togogo.dto.StockRecordDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class InventoryListTab {

    private final AppContext ctx;

    public InventoryListTab(AppContext ctx) {
        this.ctx = ctx;
    }

    @SuppressWarnings("unchecked")
    public Tab build() {
        Tab tab = new Tab("商品浏览");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("搜索商品...");
        ComboBox<String> searchType = new ComboBox<>(
                FXCollections.observableArrayList("按名称", "按分类", "按供应商"));
        searchType.getSelectionModel().select(0);
        Button searchBtn = new Button("搜索");
        Button resetBtn = new Button("重置");
        searchBox.getChildren().addAll(searchField, searchType, searchBtn, resetBtn);

        TableView<InventoryDTO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().addAll(
                col("ID", "id", Long.class),
                col("商品名称", "productName", String.class),
                col("商品编码", "productCode", String.class),
                col("分类", "category", String.class),
                col("库存", "quantity", Integer.class),
                col("单价", "unitPrice", Double.class),
                col("安全库存", "minStock", Integer.class),
                col("存放位置", "location", String.class));

        HBox pageBox = new HBox(10);
        pageBox.setAlignment(Pos.CENTER);
        Label pageInfo = new Label();
        Button prevBtn = new Button("上一页");
        Button nextBtn = new Button("下一页");
        pageBox.getChildren().addAll(prevBtn, pageInfo, nextBtn);

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);
        Button stockInBtn = new Button("入库");
        stockInBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-size: 14px;");
        Button stockOutBtn = new Button("出库");
        stockOutBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 14px;");
        actionBox.getChildren().addAll(stockInBtn, stockOutBtn);

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        root.getChildren().addAll(searchBox, table, pageBox, actionBox, statusLabel);

        ObservableList<InventoryDTO> data = FXCollections.observableArrayList();
        table.setItems(data);
        final int[] currentPage = {0};
        final int pageSize = 10;
        final int[] totalPages = {0};

        Runnable loadData = () -> {
            statusLabel.setText("加载中...");
            ctx.getExecutorService().submit(() -> {
                try {
                    Page<InventoryDTO> page;
                    String keyword = searchField.getText().trim();
                    String mode = searchType.getValue();
                    Pageable pageable = PageRequest.of(currentPage[0], pageSize, Sort.by(Sort.Direction.DESC, "id"));
                    if (keyword.isEmpty()) {
                        page = ctx.getInventoryService().getAllInventories(pageable);
                    } else {
                        page = switch (mode) {
                            case "按分类" -> ctx.getInventoryService().searchByCategory(keyword, pageable);
                            case "按供应商" -> ctx.getInventoryService().searchBySupplier(keyword, pageable);
                            default -> ctx.getInventoryService().searchByProductName(keyword, pageable);
                        };
                    }
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
        searchBtn.setOnAction(e -> { currentPage[0] = 0; loadData.run(); });
        resetBtn.setOnAction(e -> { searchField.clear(); searchType.getSelectionModel().select(0); currentPage[0] = 0; loadData.run(); });

        stockInBtn.setOnAction(e -> handleStockOperation(table, statusLabel, stockInBtn, true, loadData));
        stockOutBtn.setOnAction(e -> handleStockOperation(table, statusLabel, stockOutBtn, false, loadData));

        loadData.run();
        tab.setContent(root);
        return tab;
    }

    private void handleStockOperation(TableView<InventoryDTO> table, Label statusLabel, Button btn, boolean isStockIn, Runnable reload) {
        InventoryDTO selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("请先选择一个商品"); return; }
        if (!isStockIn && selected.getQuantity() <= 0) { statusLabel.setText("该商品库存不足"); return; }

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle(isStockIn ? "入库" : "出库");
        dialog.setHeaderText((isStockIn ? "入库" : "出库") + "：" + selected.getProductName());
        dialog.setContentText("数量：");

        ButtonType okBtnType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtnType, ButtonType.CANCEL);

        TextField quantityField = new TextField();
        quantityField.setPromptText("请输入数量");
        dialog.getDialogPane().setContent(quantityField);

        dialog.setResultConverter(dialogBtn -> {
            if (dialogBtn == okBtnType) {
                try {
                    return Integer.parseInt(quantityField.getText().trim());
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(quantity -> {
            if (quantity <= 0) { statusLabel.setText("数量必须大于0"); return; }
            statusLabel.setText((isStockIn ? "入库" : "出库") + "中...");
            btn.setDisable(true);

            StockRequest req = new StockRequest();
            req.setInventoryId(selected.getId());
            req.setQuantity(quantity);
            req.setStockType(isStockIn ? "IN" : "OUT");

            ctx.getExecutorService().submit(() -> {
                try {
                    StockRecordDTO result = isStockIn ?
                            ctx.getInventoryService().stockIn(ctx.getCurrentUser().getId(), req) :
                            ctx.getInventoryService().stockOut(ctx.getCurrentUser().getId(), req);
                    Platform.runLater(() -> {
                        btn.setDisable(false);
                        statusLabel.setTextFill(Color.GREEN);
                        statusLabel.setText((isStockIn ? "入库" : "出库") + "成功！");
                        reload.run();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        btn.setDisable(false);
                        statusLabel.setTextFill(Color.RED);
                        statusLabel.setText((isStockIn ? "入库" : "出库") + "失败：" + ex.getMessage());
                    });
                }
            });
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> TableColumn<InventoryDTO, T> col(String title, String prop, Class<T> type) {
        TableColumn<InventoryDTO, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        return c;
    }
}