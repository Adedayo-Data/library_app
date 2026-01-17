package com.lawpavillion.lmsui.controller;

import com.lawpavillion.lmsui.model.Book;
import com.lawpavillion.lmsui.service.ApiService;
import com.lawpavillion.lmsui.util.DialogUtils;
import com.lawpavillion.lmsui.util.ValidationUtils;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @FXML private Button searchButton;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Button deleteSelectedButton;
    @FXML private Label statusLabel;
    @FXML private Label totalBooksLabel;

    // Pagination controls
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private Label pageLabel;
    @FXML private Label pageNumberLabel;

    // Modal & Toast Controls
    @FXML private StackPane modalOverlay;
    @FXML private StackPane deleteOverlay;
    @FXML private StackPane searchOverlay;
    @FXML private TextField searchField;
    @FXML private HBox activeSearchChip;
    @FXML private Label activeSearchLabel;
    @FXML private VBox toastOverlay;
    @FXML private Label toastLabel;
    
    // Modal Form Fields
    @FXML private Label modalTitle;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private DatePicker datePicker;

    private final ApiService apiService;
    private final ObservableList<Book> bookList;
    private final List<Book> selectedBooks;
    
    // Pagination state
    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPages = 1;
    private long totalElements = 0;

    // State for the book currently being edited (null if adding new)
    // State for the book currently being edited (null if adding new)
    private Book currentBookInModal = null;
    
    // State for search
    private String currentSearchQuery = "";

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

        bookTable.setItems(bookList);
    }

    /**
     * Set up pagination controls.
     */
    private void setupPagination() {
        updatePaginationButtons();
    }

    /**
     * Load books from the backend with pagination.
     */
    private void loadBooks() {
        Platform.runLater(() -> {
            try {
                updateStatus("Loading books...");
                Page<Book> page;
                if (currentSearchQuery == null || currentSearchQuery.isEmpty()) {
                    page = apiService.getBooks(currentPage, pageSize);
                } else {
                    page = apiService.searchBooks(currentSearchQuery, currentPage, pageSize);
                }
                
                bookList.setAll(page.getContent());
                // bookTable.setItems(bookList); // Already set in initialize, no need to set again unless list reference changes
                
                totalElements = page.getTotalElements();
                totalPages = page.getTotalPages();
                
                updatePaginationInfo();
                
                if (bookList.isEmpty()) {
                    updateStatus("No books found. Click + to add your first book!");
                } else {
                    updateStatus("Loaded " + bookList.size() + " books");
                }
            } catch (Exception e) {
                System.err.println("Error loading books: " + e.getMessage());
                e.printStackTrace();
                updateStatus("Unable to connect to server. Please check if backend is running.");
            }
        });
    }

    private void updatePaginationInfo() {
        pageLabel.setText("Page " + (currentPage + 1) + " of " + Math.max(1, totalPages));
        pageNumberLabel.setText(String.valueOf(currentPage + 1));
        totalBooksLabel.setText("Total Books: " + totalElements);
        updatePaginationButtons();
    }

    private void updatePaginationButtons() {
        firstPageButton.setDisable(currentPage == 0);
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
        lastPageButton.setDisable(currentPage >= totalPages - 1);
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    private void updateDeleteButtonState() {
        boolean hasSelection = !selectedBooks.isEmpty();
        deleteSelectedButton.setDisable(!hasSelection);
        deleteSelectedButton.setVisible(hasSelection);
        deleteSelectedButton.setManaged(hasSelection);
    }

    // ===== EVENT HANDLERS =====

    @FXML
    private void handleAdd() {
        currentBookInModal = null;
        openModal("Add New Book", null);
    }

    @FXML
    private void handleSearch() {
        // Open Search Modal
        searchField.clear();
        searchOverlay.setVisible(true);
        searchField.requestFocus();
    }
    
    @FXML
    private void handlePerformSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            showToast("Please enter a search term", "warning");
            return;
        }
        
        currentSearchQuery = query;
        currentPage = 0; // Reset to first page
        loadBooks(); // loadBooks will handle the search query
        
        searchOverlay.setVisible(false);
        showToast("Showing results for: " + query, "success");
        
        // Show Chip
        activeSearchLabel.setText("Results for: \"" + query + "\"");
        activeSearchChip.setVisible(true);
        activeSearchChip.setManaged(true);
    }
    
    @FXML
    private void handleClearSearch() {
        currentSearchQuery = "";
        searchField.clear();
        
        activeSearchChip.setVisible(false);
        activeSearchChip.setManaged(false);
        
        loadBooks();
        showToast("Search cleared", "info");
    }
    
    @FXML
    private void handleCancelSearch() {
        searchOverlay.setVisible(false);
    }

    @FXML
    private void handleRefresh() {
        selectedBooks.clear();
        updateDeleteButtonState();
        loadBooks();
        showToast("Refreshed list", "success");
    }

    @FXML
    private void handleDeleteSelected() {
        // Just show the modal
        deleteOverlay.setVisible(true);
    }

    @FXML
    private void handleConfirmDelete() {
        List<Book> selectedBooks = bookTable.getItems().stream()
                .filter(Book::isSelected)
                .collect(Collectors.toList());

        if (selectedBooks.isEmpty()) {
            showToast("No books selected", "error");
            deleteOverlay.setVisible(false);
            return;
        }

        List<Long> idsToDelete = selectedBooks.stream()
                .map(Book::getId)
                .collect(Collectors.toList());

        // Call backend
        apiService.deleteBooks(idsToDelete);

        // Success
        showToast("Book(s) deleted successfully", "success");
        loadBooks();
        deleteSelectedButton.setDisable(true);
        deleteSelectedButton.setVisible(false);
        deleteSelectedButton.setManaged(false);
        
        // Hide modal
        deleteOverlay.setVisible(false);
    }

    @FXML
    private void handleCancelDelete() {
        deleteOverlay.setVisible(false);
    }

    private void handleEdit(Book book) {
        currentBookInModal = book;
        openModal("Edit Book", book);
    }

    // ===== MODAL LOGIC =====

    private void openModal(String title, Book book) {
        modalTitle.setText(title);
        
        // Clear or Populate Fields
        if (book != null) {
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            isbnField.setText(book.getIsbn());
            datePicker.setValue(book.getPublishedDate());
        } else {
            titleField.clear();
            authorField.clear();
            isbnField.clear();
            datePicker.setValue(null);
        }
        
        // Show Modal with Fade In
        modalOverlay.setOpacity(0);
        modalOverlay.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(200), modalOverlay);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    @FXML
    private void handleCancelModal() {
        FadeTransition ft = new FadeTransition(Duration.millis(200), modalOverlay);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> modalOverlay.setVisible(false));
        ft.play();
    }

    @FXML
    private void handleSaveBook() {
        String title = titleField.getText();
        String author = authorField.getText();
        String isbn = isbnField.getText();
        LocalDate date = datePicker.getValue();
        
        if (!validateBookForm(title, author, isbn, date)) {
            return;
        }
        
        try {
            if (currentBookInModal == null) {
                // Add
                Book newBook = new Book();
                newBook.setTitle(title);
                newBook.setAuthor(author);
                newBook.setIsbn(isbn);
                newBook.setPublishedDate(date);
                newBook.setStatus("Available"); // Default
                apiService.addBook(newBook);
                showToast("Book added successfully", "success");
            } else {
                // Update
                currentBookInModal.setTitle(title);
                currentBookInModal.setAuthor(author);
                currentBookInModal.setIsbn(isbn);
                currentBookInModal.setPublishedDate(date);
                // Status remains unchanged during edit (or handled differently if needed)
                apiService.updateBook(currentBookInModal.getId(), currentBookInModal);
                showToast("Book updated successfully", "success");
            }
            
            handleCancelModal();
            loadBooks();
            
        } catch (Exception e) {
            showToast("Failed to save: " + e.getMessage(), "error");
        }
    }
    
    // ===== TOAST LOGIC =====

    private void showToast(String message, String type) {
        toastLabel.setText(message);
        toastOverlay.setVisible(true);
        toastOverlay.setOpacity(0);
        
        // Slight slide up and fade in
        toastOverlay.setTranslateY(20);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        javafx.animation.TranslateTransition slideUp = new javafx.animation.TranslateTransition(Duration.millis(300), toastOverlay);
        slideUp.setFromY(20);
        slideUp.setToY(0);
        
        javafx.animation.ParallelTransition show = new javafx.animation.ParallelTransition(fadeIn, slideUp);
        show.play();
        
        // Hide after delay
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastOverlay);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> toastOverlay.setVisible(false));
            fadeOut.play();
        });
        
        show.setOnFinished(e -> delay.play());
    }

    // ===== VALIDATION =====
    
    private boolean validateBookForm(String title, String author, String isbn, LocalDate publishedDate) {
        if (!ValidationUtils.isValidTitle(title)) {
            showToast("Invalid title (1-200 chars)", "error");
            return false;
        }
        if (!ValidationUtils.isValidAuthor(author)) {
            showToast("Invalid author name", "error");
            return false;
        }
        if (!ValidationUtils.isValidISBN(isbn)) {
            showToast("Invalid ISBN (10-13 digits)", "error");
            return false;
        }
        if (!ValidationUtils.isValidPublishedDate(publishedDate)) {
            showToast("Invalid date (cannot be future)", "error");
            return false;
        }
        return true;
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
}
