package net.togogo.client.view.tab;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import net.togogo.client.AppContext;
import net.togogo.dto.CreateInventoryRequest;
import net.togogo.dto.InventoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class InventoryManageTab {

    private final AppContext ctx;

    public InventoryManageTab(AppContext ctx) {
        this.ctx = ctx;
    }

    public Tab build() {
        Tab tab = new Tab("商品管理");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        HBox toolbar = new HBox(10);
        Button addBtn = new Button("添加商品");
        addBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white;");
        Button editBtn = new Button("编辑");
        editBtn.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white;");
        Button deleteBtn = new Button("删除");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn);

        HBox searchBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("搜索商品...");
        ComboBox<String> searchType = new ComboBox<>(FXCollections.observableArrayList("按名称", "按分类", "按供应商"));
        searchType.getSelectionModel().select(0);
        Button searchBtn = new Button("搜索");
        Button resetBtn = new Button("重置");
        searchBox.getChildren().addAll(searchField, searchType, searchBtn, resetBtn);

        TableView<InventoryDTO> table = createInventoryTable();

        HBox pageBox = new HBox(10);
        pageBox.setAlignment(Pos.CENTER);
        Label pageInfo = new Label();
        Button prevBtn = new Button("上一页");
        Button nextBtn = new Button("下一页");
        pageBox.getChildren().addAll(prevBtn, pageInfo, nextBtn);

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        root.getChildren().addAll(toolbar, searchBox, table, pageBox, statusLabel);

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

        addBtn.setOnAction(e -> showInventoryDialog(null, () -> { currentPage[0] = 0; loadData.run(); }, statusLabel));
        editBtn.setOnAction(e -> {
            InventoryDTO selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { statusLabel.setText("请选择要编辑的商品"); return; }
            showInventoryDialog(selected, loadData, statusLabel);
        });
        deleteBtn.setOnAction(e -> {
            InventoryDTO selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { statusLabel.setText("请选择要删除的商品"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定删除商品《" + selected.getProductName() + "》？");
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    statusLabel.setText("删除中...");
                    ctx.getExecutorService().submit(() -> {
                        try {
                            ctx.getInventoryService().deleteInventory(selected.getId());
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

    @SuppressWarnings("unchecked")
    private TableView<InventoryDTO> createInventoryTable() {
        TableView<InventoryDTO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().addAll(
                col("ID", "id"), col("商品名称", "productName"), col("商品编码", "productCode"),
                col("分类", "category"), col("库存", "quantity"), col("单价", "unitPrice"),
                col("安全库存", "minStock"), col("存放位置", "location"));
        return table;
    }

    private static TableColumn<InventoryDTO, String> col(String title, String prop) {
        TableColumn<InventoryDTO, String> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        return c;
    }

    private void showInventoryDialog(InventoryDTO existing, Runnable onSave, Label statusLabel) {
        Dialog<CreateInventoryRequest> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "添加商品" : "编辑商品");
        dialog.setHeaderText(existing == null ? "请输入商品信息" : "编辑商品信息");

        ButtonType saveBtnType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField nameField = new TextField(); nameField.setPromptText("商品名称");
        TextField codeField = new TextField(); codeField.setPromptText("商品编码");
        TextField barcodeField = new TextField(); barcodeField.setPromptText("条形码");
        TextField supplierField = new TextField(); supplierField.setPromptText("供应商");
        TextField categoryField = new TextField(); categoryField.setPromptText("分类（如：电子、服装）");
        TextField quantityField = new TextField(); quantityField.setPromptText("库存数量");
        TextField priceField = new TextField(); priceField.setPromptText("单价");
        TextField minStockField = new TextField(); minStockField.setPromptText("安全库存");
        TextField locationField = new TextField(); locationField.setPromptText("存放位置");
        TextArea descArea = new TextArea(); descArea.setPromptText("商品描述"); descArea.setPrefRowCount(3);

        if (existing != null) {
            nameField.setText(existing.getProductName());
            codeField.setText(existing.getProductCode());
            barcodeField.setText(existing.getBarcode());
            supplierField.setText(existing.getSupplier());
            categoryField.setText(existing.getCategory());
            quantityField.setText(existing.getQuantity() != null ? existing.getQuantity().toString() : "");
            priceField.setText(existing.getUnitPrice() != null ? existing.getUnitPrice().toString() : "");
            minStockField.setText(existing.getMinStock() != null ? existing.getMinStock().toString() : "");
            locationField.setText(existing.getLocation());
            descArea.setText(existing.getDescription());
        }

        addRow(grid, "商品名称*：", nameField, 0);
        addRow(grid, "商品编码：", codeField, 1);
        addRow(grid, "条形码：", barcodeField, 2);
        addRow(grid, "供应商：", supplierField, 3);
        addRow(grid, "分类：", categoryField, 4);
        addRow(grid, "库存*：", quantityField, 5);
        addRow(grid, "单价*：", priceField, 6);
        addRow(grid, "安全库存：", minStockField, 7);
        addRow(grid, "存放位置：", locationField, 8);
        addRow(grid, "描述：", descArea, 9);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogBtn -> {
            if (dialogBtn == saveBtnType) {
                if (nameField.getText().trim().isEmpty()) return null;
                CreateInventoryRequest req = new CreateInventoryRequest();
                req.setProductName(nameField.getText().trim());
                req.setProductCode(codeField.getText().trim().isEmpty() ? null : codeField.getText().trim());
                req.setBarcode(barcodeField.getText().trim().isEmpty() ? null : barcodeField.getText().trim());
                req.setSupplier(supplierField.getText().trim().isEmpty() ? null : supplierField.getText().trim());
                req.setCategory(categoryField.getText().trim().isEmpty() ? null : categoryField.getText().trim());
                try { req.setQuantity(quantityField.getText().trim().isEmpty() ? 0 : Integer.parseInt(quantityField.getText().trim())); }
                catch (NumberFormatException e) { return null; }
                try { req.setUnitPrice(priceField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(priceField.getText().trim())); }
                catch (NumberFormatException e) { return null; }
                try { req.setMinStock(minStockField.getText().trim().isEmpty() ? 10 : Integer.parseInt(minStockField.getText().trim())); }
                catch (NumberFormatException e) { return null; }
                req.setLocation(locationField.getText().trim().isEmpty() ? null : locationField.getText().trim());
                req.setDescription(descArea.getText().trim().isEmpty() ? null : descArea.getText().trim());
                return req;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(req -> {
            statusLabel.setText("保存中...");
            ctx.getExecutorService().submit(() -> {
                try {
                    InventoryDTO result = existing == null ? ctx.getInventoryService().createInventory(req) : ctx.getInventoryService().updateInventory(existing.getId(), req);
                    Platform.runLater(() -> {
                        statusLabel.setTextFill(Color.GREEN);
                        statusLabel.setText(existing == null ? "添加成功" : "更新成功");
                        onSave.run();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        statusLabel.setTextFill(Color.RED);
                        statusLabel.setText("保存失败：" + ex.getMessage());
                    });
                }
            });
        });
    }

    private static void addRow(GridPane grid, String labelText, Node field, int row) {
        grid.add(new Label(labelText), 0, row);
        grid.add(field, 1, row);
    }
}