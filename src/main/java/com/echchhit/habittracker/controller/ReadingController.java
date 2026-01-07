package com.echchhit.habittracker.controller;

import com.echchhit.habittracker.model.Book;
import com.echchhit.habittracker.service.BookService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

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
                    setText(null);
                    setContextMenu(null); // Clear context menu for empty cells
                } else {
                    VBox details = new VBox(2);
                    Label title = new Label(book.getTitle());
                    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");
                    Label author = new Label("by " + book.getAuthor());
                    author.setStyle("-fx-text-fill: #b2bec3; -fx-font-size: 12px;");
                    details.getChildren().addAll(title, author);

                    VBox progressSection = new VBox(5);
                    progressSection.setAlignment(Pos.CENTER_RIGHT);
                    double progressValue = (double) book.getPagesRead() / book.getTotalPages();
                    ProgressBar bar = new ProgressBar(progressValue);
                    bar.setPrefWidth(150);
                    bar.setStyle("-fx-accent: " + (progressValue >= 1.0 ? "#2ecc71;" : "#6C63FF;"));

                    Label progressText = new Label(book.getPagesRead() + " / " + book.getTotalPages() + " pages");
                    progressText.getStyleClass().add("reading-progress-text");
                    progressSection.getChildren().addAll(bar, progressText);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button addBtn = new Button(progressValue >= 1.0 ? "✓" : "+");
                    addBtn.setDisable(progressValue >= 1.0);
                    addBtn.setStyle("-fx-background-color: #f1f3f5; -fx-background-radius: 50; -fx-min-width: 35; -fx-min-height: 35; -fx-cursor: hand;");
                    addBtn.setOnAction(e -> showUpdateDialog(book));

                    HBox container = new HBox(15, details, spacer, progressSection, addBtn);
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.getStyleClass().add("book-item-container");

                    setGraphic(container);

                    // --- NEW: ADD DELETE CONTEXT MENU ---
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem deleteItem = new MenuItem("Remove Book & History");
                    deleteItem.setStyle("-fx-text-fill: #e74c3c;"); // Red color for danger action
                    deleteItem.setOnAction(event -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirm Deletion");
                        alert.setHeaderText("Delete '" + book.getTitle() + "'?");
                        alert.setContentText("This will permanently remove the book and all reading logs associated with it.");

                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                BookService.deleteBook(book.getId());
                                refreshUI();
                            }
                        });
                    });
                    contextMenu.getItems().add(deleteItem);
                    setContextMenu(contextMenu);
                    // ------------------------------------
                }
            }
        });
    }

    private void showUpdateDialog(Book book) {
        int remaining = book.getTotalPages() - book.getPagesRead();

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Update Progress");
        dialog.setHeaderText(book.getTitle() + " (" + remaining + " pages left)");
        dialog.setContentText("Enter pages read today:");

        dialog.showAndWait().ifPresent(val -> {
            try {
                int input = Integer.parseInt(val.trim());
                if (input <= 0) {
                    showAlert("Invalid Input", "Please enter a positive number of pages.");
                } else if (input > remaining) {
                    showAlert("Limit Exceeded", "You only have " + remaining + " pages left in this book!");
                } else {
                    BookService.updateReadingProgress(book.getId(), input);
                    refreshUI();
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid numeric value.");
            }
        });
    }

    @FXML
    private void handleAddBook() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            int pages = Integer.parseInt(totalPagesField.getText().trim());

            if (title.isEmpty() || pages <= 0) {
                showAlert("Missing Info", "Please provide a title and a valid number of pages.");
                return;
            }

            BookService.addBook(title, author, pages);
            titleField.clear(); authorField.clear(); totalPagesField.clear();
            refreshUI();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Total pages must be a number.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void refreshUI() {
        booksFinishedLabel.setText(String.format("%02d", BookService.getFinishedBooksCount()));
        bookListView.getItems().setAll(BookService.getAllBooks());
    }
}