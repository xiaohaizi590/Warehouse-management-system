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
import javafx.scene.text.Font;
import net.togogo.client.AppContext;
import net.togogo.dto.BookDTO;
import net.togogo.dto.CreateBookRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

public class BookManageTab {

    private final AppContext ctx;

    public BookManageTab(AppContext ctx) {
        this.ctx = ctx;
    }

    public Tab build() {
        Tab tab = new Tab("图书管理");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        HBox toolbar = new HBox(10);
        Button addBtn = new Button("添加图书");
        addBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white;");
        Button editBtn = new Button("编辑");
        editBtn.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white;");
        Button deleteBtn = new Button("删除");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn);

        HBox searchBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("搜索图书...");
        ComboBox<String> searchType = new ComboBox<>(FXCollections.observableArrayList("按书名", "按作者", "按分类"));
        searchType.getSelectionModel().select(0);
        Button searchBtn = new Button("搜索");
        Button resetBtn = new Button("重置");
        searchBox.getChildren().addAll(searchField, searchType, searchBtn, resetBtn);

        TableView<BookDTO> table = createBookTable();

        HBox pageBox = new HBox(10);
        pageBox.setAlignment(Pos.CENTER);
        Label pageInfo = new Label();
        Button prevBtn = new Button("上一页");
        Button nextBtn = new Button("下一页");
        pageBox.getChildren().addAll(prevBtn, pageInfo, nextBtn);

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        root.getChildren().addAll(toolbar, searchBox, table, pageBox, statusLabel);

        ObservableList<BookDTO> data = FXCollections.observableArrayList();
        table.setItems(data);
        final int[] currentPage = {0};
        final int pageSize = 10;
        final int[] totalPages = {0};

        Runnable loadData = () -> {
            statusLabel.setText("加载中...");
            ctx.getExecutorService().submit(() -> {
                try {
                    Page<BookDTO> page;
                    String keyword = searchField.getText().trim();
                    String mode = searchType.getValue();
                    Pageable pageable = PageRequest.of(currentPage[0], pageSize, Sort.by(Sort.Direction.DESC, "id"));
                    if (keyword.isEmpty()) {
                        page = ctx.getBookService().getAllBooks(pageable);
                    } else {
                        page = switch (mode) {
                            case "按作者" -> ctx.getBookService().searchByAuthor(keyword, pageable);
                            case "按分类" -> ctx.getBookService().searchByCategory(keyword, pageable);
                            default -> ctx.getBookService().searchByTitle(keyword, pageable);
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

        addBtn.setOnAction(e -> showBookDialog(null, () -> { currentPage[0] = 0; loadData.run(); }, statusLabel));
        editBtn.setOnAction(e -> {
            BookDTO selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { statusLabel.setText("请选择要编辑的图书"); return; }
            showBookDialog(selected, loadData, statusLabel);
        });
        deleteBtn.setOnAction(e -> {
            BookDTO selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { statusLabel.setText("请选择要删除的图书"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定删除《" + selected.getTitle() + "》？");
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    statusLabel.setText("删除中...");
                    ctx.getExecutorService().submit(() -> {
                        try {
                            ctx.getBookService().deleteBook(selected.getId());
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
    private TableView<BookDTO> createBookTable() {
        TableView<BookDTO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().addAll(
                col("ID", "id"), col("书名", "title"), col("作者", "author"),
                col("ISBN", "isbn"), col("分类", "category"), col("库存", "stock"), col("可借", "available"));
        return table;
    }

    private static TableColumn<BookDTO, String> col(String title, String prop) {
        TableColumn<BookDTO, String> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        return c;
    }

    private void showBookDialog(BookDTO existing, Runnable onSave, Label statusLabel) {
        Dialog<CreateBookRequest> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "添加图书" : "编辑图书");
        dialog.setHeaderText(existing == null ? "请输入图书信息" : "编辑图书信息");

        ButtonType saveBtnType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField titleField = new TextField(); titleField.setPromptText("书名");
        TextField authorField = new TextField(); authorField.setPromptText("作者");
        TextField isbnField = new TextField(); isbnField.setPromptText("ISBN");
        TextField publisherField = new TextField(); publisherField.setPromptText("出版社");
        TextField categoryField = new TextField(); categoryField.setPromptText("分类（如：文学、科技）");
        TextField stockField = new TextField(); stockField.setPromptText("库存数量");
        TextArea descArea = new TextArea(); descArea.setPromptText("简介"); descArea.setPrefRowCount(3);

        if (existing != null) {
            titleField.setText(existing.getTitle());
            authorField.setText(existing.getAuthor());
            isbnField.setText(existing.getIsbn());
            publisherField.setText(existing.getPublisher());
            categoryField.setText(existing.getCategory());
            stockField.setText(existing.getStock() != null ? existing.getStock().toString() : "");
            descArea.setText(existing.getDescription());
        }

        addRow(grid, "书名*：", titleField, 0);
        addRow(grid, "作者*：", authorField, 1);
        addRow(grid, "ISBN：", isbnField, 2);
        addRow(grid, "出版社：", publisherField, 3);
        addRow(grid, "分类：", categoryField, 4);
        addRow(grid, "库存*：", stockField, 5);
        addRow(grid, "简介：", descArea, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogBtn -> {
            if (dialogBtn == saveBtnType) {
                if (titleField.getText().trim().isEmpty() || authorField.getText().trim().isEmpty()) return null;
                CreateBookRequest req = new CreateBookRequest();
                req.setTitle(titleField.getText().trim());
                req.setAuthor(authorField.getText().trim());
                req.setIsbn(isbnField.getText().trim().isEmpty() ? null : isbnField.getText().trim());
                req.setPublisher(publisherField.getText().trim().isEmpty() ? null : publisherField.getText().trim());
                req.setCategory(categoryField.getText().trim().isEmpty() ? null : categoryField.getText().trim());
                try { req.setStock(stockField.getText().trim().isEmpty() ? 0 : Integer.parseInt(stockField.getText().trim())); }
                catch (NumberFormatException e) { return null; }
                req.setDescription(descArea.getText().trim().isEmpty() ? null : descArea.getText().trim());
                return req;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(req -> {
            statusLabel.setText("保存中...");
            ctx.getExecutorService().submit(() -> {
                try {
                    BookDTO result = existing == null ? ctx.getBookService().createBook(req) : ctx.getBookService().updateBook(existing.getId(), req);
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