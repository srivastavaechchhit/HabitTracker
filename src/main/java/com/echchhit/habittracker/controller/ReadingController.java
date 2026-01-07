package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.model.Book;
import com.echchhit.habittracker.service.BookService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class ReadingController {
    @FXML private Label booksFinishedLabel;
    @FXML private TextField titleField, authorField, totalPagesField;
    @FXML private ListView<Book> bookListView;

    @FXML
    public void initialize() {
        setupListView();
        refreshUI();
    }

    private void setupListView() {
        bookListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                if (empty || book == null) {
                    setGraphic(null);
                } else {
                    Label title = new Label(book.getTitle() + " by " + book.getAuthor());
                    Label progress = new Label(book.getProgressText());
                    progress.setStyle("-fx-text-fill: #6C63FF; -fx-font-weight: bold;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button updateBtn = new Button("+ Pages");
                    updateBtn.setOnAction(e -> showUpdateDialog(book));

                    HBox container = new HBox(10, title, spacer, progress, updateBtn);
                    container.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");
                    setGraphic(container);
                }
            }
        });
    }

    private void showUpdateDialog(Book book) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Update Progress");
        dialog.setHeaderText("Pages read today for: " + book.getTitle());
        dialog.setContentText("Enter number of pages:");
        dialog.showAndWait().ifPresent(val -> {
            try {
                BookService.updateReadingProgress(book.getId(), Integer.parseInt(val));
                refreshUI();
                MainController.refreshStats();
            } catch (Exception e) { /* Handle error */ }
        });
    }

    @FXML
    private void handleAddBook() {
        try {
            String title = titleField.getText();
            String author = authorField.getText();
            int pages = Integer.parseInt(totalPagesField.getText());
            BookService.addBook(title, author, pages);
            titleField.clear(); authorField.clear(); totalPagesField.clear();
            refreshUI();
        } catch (Exception e) { /* Handle error */ }
    }

    private void refreshUI() {
        booksFinishedLabel.setText(String.valueOf(BookService.getFinishedBooksCount()));
        bookListView.getItems().setAll(BookService.getAllBooks());
    }
}