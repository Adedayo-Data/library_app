package com.lawpavillion.lmsui.controller;

import com.lawpavillion.lmsui.model.Book;
import com.lawpavillion.lmsui.service.ApiService;
import com.lawpavillion.lmsui.util.DialogUtils;
import com.lawpavillion.lmsui.util.ValidationUtils;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Main controller for the Library Management System UI.
 * Handles all user interactions and backend communication.
 */
public class LibraryController {

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, Boolean> selectColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, String> publishedDateColumn;
    @FXML private TableColumn<Book, String> statusColumn;

    @FXML private Button searchButton;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Button deleteSelectedButton;
    @FXML private Label statusLabel;

    // Pagination controls
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private Label pageLabel;
    @FXML private ComboBox<Integer> pageSizeComboBox;

    private final ApiService apiService;
    private final ObservableList<Book> bookList;
    private final List<Book> selectedBooks;
    
    // Pagination state
    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPages = 1;
    private long totalElements = 0;

    public LibraryController() {
        this.apiService = new ApiService();
        this.bookList = FXCollections.observableArrayList();
        this.selectedBooks = new ArrayList<>();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupPagination();
        setupAnimations();
        loadBooks();
        
        // Set up double-click to edit
        bookTable.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEdit(row.getItem());
                }
            });
            return row;
        });
    }

    /**
     * Set up smooth animations for UI elements.
     */
    private void setupAnimations() {
        // Initial fade-in for table
        bookTable.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), bookTable);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);
        fadeIn.setDelay(Duration.millis(100));
        fadeIn.play();
    }

    /**
     * Set up table columns with proper cell value factories.
     */
    private void setupTableColumns() {
        // Checkbox column for multi-select
        selectColumn.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    if (checkBox.isSelected()) {
                        selectedBooks.add(book);
                    } else {
                        selectedBooks.remove(book);
                    }
                    updateDeleteButtonState();
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Book book = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(selectedBooks.contains(book));
                    setGraphic(checkBox);
                }
            }
        });

        // Title column
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        // Author column
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));

        // ISBN column
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        // Published Date column
        publishedDateColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getPublishedDate();
            String formattedDate = date != null ? date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "";
            return new SimpleStringProperty(formattedDate);
        });

        // Status column with badge styling
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("status-badge");
                    if ("Available".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("status-available");
                    } else {
                        badge.getStyleClass().add("status-borrowed");
                    }
                    setGraphic(badge);
                }
            }
        });
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        bookTable.setItems(bookList);
    }

    /**
     * Set up pagination controls.
     */
    private void setupPagination() {
        // Populate page size options
        pageSizeComboBox.getItems().addAll(10, 25, 50, 100);
        pageSizeComboBox.setValue(pageSize);
        updatePaginationButtons();
    }

    /**
     * Load books from the backend with pagination.
     */
    private void loadBooks() {
        try {
            updateStatus("Loading books...");
            
            // Use pagination if backend supports it
            Page<Book> page = apiService.getBooks(currentPage, pageSize);
            
            bookList.clear();
            bookList.addAll(page.getContent());
            
            totalPages = page.getTotalPages();
            totalElements = page.getTotalElements();
            
            updatePaginationInfo();
            
            // Show appropriate status message
            if (bookList.isEmpty()) {
                updateStatus("No books found. Click + to add your first book!");
            } else {
                updateStatus("Loaded " + bookList.size() + " books");
            }
        } catch (Exception e) {
            // Log error but don't show popup for connection issues
            System.err.println("Error loading books: " + e.getMessage());
            e.printStackTrace();
            updateStatus("Unable to connect to server. Please check if backend is running.");
        }
    }

    /**
     * Update pagination information and button states.
     */
    private void updatePaginationInfo() {
        pageLabel.setText("Page " + (currentPage + 1) + " of " + Math.max(1, totalPages));
        updatePaginationButtons();
    }

    /**
     * Update pagination button enabled/disabled states.
     */
    private void updatePaginationButtons() {
        firstPageButton.setDisable(currentPage == 0);
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
        lastPageButton.setDisable(currentPage >= totalPages - 1);
    }

    /**
     * Update status label.
     */
    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    /**
     * Update delete button state based on selection.
     */
    private void updateDeleteButtonState() {
        deleteSelectedButton.setDisable(selectedBooks.isEmpty());
    }

    // ===== EVENT HANDLERS =====

    @FXML
    private void handleAdd() {
        showBookDialog(null);
    }

    @FXML
    private void handleSearch() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Search Books");
        dialog.setHeaderText("Search by title or author");
        dialog.setContentText("Enter search term:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(query -> {
            if (query.trim().isEmpty()) {
                loadBooks();
            } else {
                try {
                    List<Book> searchResults = apiService.searchBooks(query);
                    bookList.clear();
                    bookList.addAll(searchResults);
                    updateStatus("Found " + searchResults.size() + " books");
                } catch (Exception e) {
                    DialogUtils.showError("Error", "Search failed: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        selectedBooks.clear();
        updateDeleteButtonState();
        loadBooks();
    }

    @FXML
    private void handleDeleteSelected() {
        if (selectedBooks.isEmpty()) {
            return;
        }

        String message = "Are you sure you want to delete " + selectedBooks.size() + " book(s)?";
        if (DialogUtils.showConfirmation("Confirm Delete", message)) {
            int successCount = 0;
            int failCount = 0;

            for (Book book : new ArrayList<>(selectedBooks)) {
                try {
                    apiService.deleteBook(book.getId());
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    System.err.println("Failed to delete book: " + book.getTitle());
                }
            }

            selectedBooks.clear();
            updateDeleteButtonState();
            loadBooks();

            if (failCount == 0) {
                DialogUtils.showSuccess("Success", "Deleted " + successCount + " book(s)");
            } else {
                DialogUtils.showWarning("Partial Success", 
                    "Deleted " + successCount + " book(s), failed to delete " + failCount);
            }
        }
    }

    private void handleEdit(Book book) {
        showBookDialog(book);
    }

    // ===== PAGINATION HANDLERS =====

    @FXML
    private void handleFirstPage() {
        currentPage = 0;
        loadBooks();
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadBooks();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadBooks();
        }
    }

    @FXML
    private void handleLastPage() {
        currentPage = Math.max(0, totalPages - 1);
        loadBooks();
    }

    @FXML
    private void handlePageSizeChange() {
        Integer newSize = pageSizeComboBox.getValue();
        if (newSize != null && newSize != pageSize) {
            pageSize = newSize;
            currentPage = 0; // Reset to first page
            loadBooks();
        }
    }

    // ===== BOOK DIALOG =====

    /**
     * Show dialog for adding or editing a book.
     */
    private void showBookDialog(Book existingBook) {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle(existingBook == null ? "Add Book" : "Edit Book");
        dialog.setHeaderText(null);

        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));
        grid.getStyleClass().add("form-container");

        TextField titleField = new TextField();
        titleField.setPromptText("Book Title");
        TextField authorField = new TextField();
        authorField.setPromptText("Author Name");
        TextField isbnField = new TextField();
        isbnField.setPromptText("ISBN");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Published Date");
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Available", "Borrowed");
        statusCombo.setValue("Available");

        // Populate if editing
        if (existingBook != null) {
            titleField.setText(existingBook.getTitle());
            authorField.setText(existingBook.getAuthor());
            isbnField.setText(existingBook.getIsbn());
            datePicker.setValue(existingBook.getPublishedDate());
            statusCombo.setValue(existingBook.getStatus());
        }

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Author:"), 0, 1);
        grid.add(authorField, 1, 1);
        grid.add(new Label("ISBN:"), 0, 2);
        grid.add(isbnField, 1, 2);
        grid.add(new Label("Published Date:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Enable/disable save button based on validation
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateBookForm(titleField.getText(), authorField.getText(), 
                    isbnField.getText(), datePicker.getValue())) {
                event.consume();
            }
        });

        // Convert result to Book
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Book book = existingBook != null ? existingBook : new Book();
                book.setTitle(titleField.getText().trim());
                book.setAuthor(authorField.getText().trim());
                book.setIsbn(isbnField.getText().trim());
                book.setPublishedDate(datePicker.getValue());
                book.setStatus(statusCombo.getValue());
                return book;
            }
            return null;
        });

        Optional<Book> result = dialog.showAndWait();
        result.ifPresent(book -> {
            try {
                if (existingBook == null) {
                    // Add new book
                    apiService.addBook(book);
                    DialogUtils.showSuccess("Success", "Book added successfully");
                } else {
                    // Update existing book
                    apiService.updateBook(book.getId(), book);
                    DialogUtils.showSuccess("Success", "Book updated successfully");
                }
                loadBooks();
            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to save book: " + e.getMessage());
            }
        });
    }

    /**
     * Validate book form inputs.
     */
    private boolean validateBookForm(String title, String author, String isbn, LocalDate publishedDate) {
        if (!ValidationUtils.isValidTitle(title)) {
            DialogUtils.showError("Validation Error", "Please enter a valid title (1-200 characters)");
            return false;
        }
        if (!ValidationUtils.isValidAuthor(author)) {
            DialogUtils.showError("Validation Error", "Please enter a valid author name (2-100 characters)");
            return false;
        }
        if (!ValidationUtils.isValidISBN(isbn)) {
            DialogUtils.showError("Validation Error", "Please enter a valid ISBN (10 or 13 digits)");
            return false;
        }
        if (!ValidationUtils.isValidPublishedDate(publishedDate)) {
            DialogUtils.showError("Validation Error", "Please enter a valid published date (not in the future)");
            return false;
        }
        return true;
    }
}
