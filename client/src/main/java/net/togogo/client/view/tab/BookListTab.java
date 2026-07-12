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
import net.togogo.dto.BookDTO;
import net.togogo.dto.BorrowRecordDTO;
import net.togogo.dto.BorrowRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class BookListTab {

    private final AppContext ctx;

    public BookListTab(AppContext ctx) {
        this.ctx = ctx;
    }

    @SuppressWarnings("unchecked")
    public Tab build() {
        Tab tab = new Tab("图书浏览");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("搜索图书...");
        ComboBox<String> searchType = new ComboBox<>(
                FXCollections.observableArrayList("按书名", "按作者", "按分类"));
        searchType.getSelectionModel().select(0);
        Button searchBtn = new Button("搜索");
        Button resetBtn = new Button("重置");
        searchBox.getChildren().addAll(searchField, searchType, searchBtn, resetBtn);

        TableView<BookDTO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().addAll(
                col("ID", "id", Long.class),
                col("书名", "title", String.class),
                col("作者", "author", String.class),
                col("ISBN", "isbn", String.class),
                col("分类", "category", String.class),
                col("库存", "stock", Integer.class),
                col("可借", "available", Integer.class));

        HBox pageBox = new HBox(10);
        pageBox.setAlignment(Pos.CENTER);
        Label pageInfo = new Label();
        Button prevBtn = new Button("上一页");
        Button nextBtn = new Button("下一页");
        pageBox.getChildren().addAll(prevBtn, pageInfo, nextBtn);

        Button borrowBtn = new Button("借阅此书");
        borrowBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-size: 14px;");
        HBox actionBox = new HBox(borrowBtn);
        actionBox.setAlignment(Pos.CENTER);

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        root.getChildren().addAll(searchBox, table, pageBox, actionBox, statusLabel);

        ObservableList<BookDTO> bookData = FXCollections.observableArrayList();
        table.setItems(bookData);
        final int[] currentPage = {0};
        final int pageSize = 10;
        final int[] totalPages = {0};

        Runnable loadBooks = () -> {
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
                        bookData.setAll(page.getContent());
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

        prevBtn.setOnAction(e -> { if (currentPage[0] > 0) { currentPage[0]--; loadBooks.run(); } });
        nextBtn.setOnAction(e -> { if (currentPage[0] < totalPages[0] - 1) { currentPage[0]++; loadBooks.run(); } });
        searchBtn.setOnAction(e -> { currentPage[0] = 0; loadBooks.run(); });
        resetBtn.setOnAction(e -> { searchField.clear(); searchType.getSelectionModel().select(0); currentPage[0] = 0; loadBooks.run(); });

        borrowBtn.setOnAction(e -> {
            BookDTO selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { statusLabel.setText("请先选择一本书"); return; }
            if (selected.getAvailable() <= 0) { statusLabel.setText("该书已无可借数量"); return; }
            TextInputDialog dialog = new TextInputDialog("30");
            dialog.setTitle("借阅图书");
            dialog.setHeaderText("借阅：" + selected.getTitle());
            dialog.setContentText("借阅天数（默认30天）：");
            dialog.showAndWait().ifPresent(daysStr -> {
                try {
                    int days = Integer.parseInt(daysStr.trim());
                    if (days <= 0 || days > 365) { statusLabel.setText("借阅天数必须在1-365之间"); return; }
                    statusLabel.setText("借阅中...");
                    borrowBtn.setDisable(true);
                    BorrowRequest req = new BorrowRequest();
                    req.setBookId(selected.getId());
                    req.setBorrowDays(days);
                    ctx.getExecutorService().submit(() -> {
                        try {
                            BorrowRecordDTO borrowResult = ctx.getBookService().borrowBook(ctx.getCurrentUser().getId(), req);
                            Platform.runLater(() -> {
                                borrowBtn.setDisable(false);
                                statusLabel.setTextFill(Color.GREEN);
                                statusLabel.setText("借阅成功！");
                                loadBooks.run();
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() -> {
                                borrowBtn.setDisable(false);
                                statusLabel.setTextFill(Color.RED);
                                statusLabel.setText("借阅失败：" + ex.getMessage());
                            });
                        }
                    });
                } catch (NumberFormatException ex) {
                    statusLabel.setText("请输入有效的天数");
                }
            });
        });

        loadBooks.run();
        tab.setContent(root);
        return tab;
    }

    @SuppressWarnings("unchecked")
    private static <T> TableColumn<BookDTO, T> col(String title, String prop, Class<T> type) {
        TableColumn<BookDTO, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        return c;
    }
}