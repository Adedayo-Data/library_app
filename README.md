# Library Management System - JavaFX Frontend

A modern, clean JavaFX user interface for the Library Management System. This application provides a professional interface for managing book records with full CRUD operations, search functionality, and pagination support.

## Features

✅ **Complete CRUD Operations**
- Add new books with validation
- Edit existing book details
- Delete single or multiple books
- Refresh data from backend

✅ **Advanced Functionality**
- Search books by title or author
- Pagination with configurable page size
- Multi-select for bulk delete operations
- Form validation for all inputs

✅ **Modern UI Design**
- Clean light theme with professional aesthetics
- Responsive table layout
- Smooth hover effects and transitions
- Icon-based navigation

## Technology Stack

- **JavaFX 21** - UI framework
- **Spring RestTemplate** - HTTP client for backend communication
- **Gson** - JSON serialization/deserialization
- **Spring Data Commons** - Pagination support
- **Ikonli** - Modern Material Design icons
- **Maven** - Build and dependency management

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Spring Boot backend running on `http://localhost:8080`

## Project Structure

```
src/main/
├── java/com/lawpavillion/lmsui/
│   ├── LibraryApplication.java          # Main application entry point
│   ├── model/
│   │   └── Book.java                    # Book entity model
│   ├── service/
│   │   └── ApiService.java              # REST API communication layer
│   ├── controller/
│   │   └── LibraryController.java       # Main UI controller
│   └── util/
│       ├── DialogUtils.java             # Alert dialog utilities
│       └── ValidationUtils.java         # Input validation utilities
└── resources/com/lawpavillion/lmsui/
    ├── library-view.fxml                # Main UI layout
    └── styles.css                       # Application stylesheet
```

## Setup Instructions

### 1. Clone the Repository

```bash
git clone git@github.com:Adedayo-Data/library_app.git
cd library_app
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Ensure Backend is Running

Make sure your Spring Boot backend is running on `http://localhost:8080` with the following endpoints:

- `GET /api/books?page={page}&size={size}` - Get paginated books
- `POST /api/books` - Add a new book
- `PUT /api/books/{id}` - Update a book
- `DELETE /api/books/{id}` - Delete a book

### 4. Run the Application

```bash
mvn javafx:run
```

Or using the launcher:

```bash
java -jar target/LMS-UI-1.0-SNAPSHOT.jar
```

## Usage Guide

### Adding a Book

1. Click the **+** (plus) icon in the top navigation bar
2. Fill in the book details:
   - Title (required, 1-200 characters)
   - Author (required, 2-100 characters)
   - ISBN (required, 10 or 13 digits)
   - Published Date (required, not in future)
3. Click **Save**

### Editing a Book

1. **Double-click** any row in the table
2. Modify the book details in the dialog
3. Click **Save**

### Deleting Books

**Single Delete:**
- Select a book and press Delete key, or
- Double-click to edit and use delete option

**Bulk Delete:**
1. Check the boxes next to books you want to delete
2. Click **Delete Selected** button
3. Confirm the deletion

### Searching Books

1. Click the **🔍** (search) icon in the top navigation bar
2. Enter search term (searches both title and author)
3. Press Enter or click OK
4. Clear search to see all books again

### Pagination

- Use **First**, **Previous**, **Next**, **Last** buttons to navigate pages
- Current page and total pages displayed in the center

## API Integration

The application communicates with the Spring Boot backend using RestTemplate. The `ApiService` class handles:

- JSON serialization/deserialization with Gson
- LocalDate formatting for API compatibility
- Error handling and exception management
- Pagination metadata parsing from Spring Data Page responses

### Expected Backend Response Format

**Paginated Response:**
```json
{
  "success": true,
  "message": "All books delivered successfully!",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Book Title",
        "author": "Author Name",
        "isbn": "9780123456789",
        "publishedDate": "2020-01-15"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 25,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalPages": 4,
    "totalElements": 100,
    "last": false,
    "size": 25,
    "number": 0,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "numberOfElements": 25,
    "first": true,
    "empty": false
  },
  "timestamp": "2024-01-17T10:30:00"
}
```

## Validation Rules

- **Title**: 1-200 characters, required
- **Author**: 2-100 characters, required
- **ISBN**: 10 or 13 digits (hyphens and spaces allowed), required
- **Published Date**: Cannot be in the future, required
- **Status**: Must be "Available" or "Borrowed"

## Troubleshooting

### Backend Connection Issues

If you see "Failed to fetch books" error:
1. Verify backend is running on `http://localhost:8080`
2. Check backend endpoints are accessible
3. Review backend logs for errors

### Build Issues

If Maven build fails:
```bash
mvn clean install -U
```

### JavaFX Runtime Issues

Ensure you have Java 21 with JavaFX support:
```bash
java --version
```

## Design Philosophy

The UI follows modern design principles:
- **Clean & Minimal**: Light theme with cream/beige background
- **Professional Typography**: Inter/SF Pro fonts for readability
- **Intuitive Navigation**: Icon-based actions with clear labels
- **Responsive Feedback**: Hover effects and visual state changes
- **Accessible**: High contrast text and clear visual hierarchy

## Future Enhancements

- [ ] Advanced filtering options
- [ ] Export to CSV/PDF
- [ ] Dark mode toggle
- [ ] Keyboard shortcuts
- [ ] Book cover image support
- [ ] Borrowing history tracking

## License

This project is part of a recruitment exercise.

## Developer

Adedayo Theophilus Adedeji
[adedejitheophilus2018@gmail.com](mailto:adedejitheophilus2018@gmail.com)
