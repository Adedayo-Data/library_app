package com.lawpavillion.bk.controller;

import com.lawpavillion.bk.dto.ApiResponse;
import com.lawpavillion.bk.dto.BookDto;
import com.lawpavillion.bk.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
@Tag(name = "Book Management", description = "APIs for managing library books")
@Slf4j
public class BookController {

    private final BookService bookService;

    @PostMapping
    @Operation(summary = "Add a new book", description = "Creates a new book record in the library")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Book created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookDto>> addBooks(
            @Valid @RequestBody @Parameter(description = "Book details to add") BookDto request){

        BookDto bookDto = bookService.addBook(request);

        ApiResponse<BookDto> response = ApiResponse.<BookDto>builder()
                .success(true)
                .message("Book Added Successfully")
                .data(bookDto)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Book added successfully with ID: {}", bookDto.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieves a paginated list of all books in the library")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Books retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<Page<BookDto>>> getAllBooks(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "20") int size){

        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());

        Page<BookDto> bookDtoPage = bookService.getAllBooks(pageable);

        ApiResponse<Page<BookDto>> response = ApiResponse.<Page<BookDto>>builder()
                .success(true)
                .message("All books delivered successfully!")
                .data(bookDtoPage)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Retrieved {} books, page {} of size {}", bookDtoPage.getTotalElements(), page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a book", description = "Updates an existing book's details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Book updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Book not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookDto>> updateBook(
            @Parameter(description = "ID of the book to update") @PathVariable Long id,
            @Valid @RequestBody @Parameter(description = "Updated book details") BookDto request){

        BookDto updatedBook = bookService.updateBook(id, request);

        ApiResponse<BookDto> response = ApiResponse.<BookDto>builder()
                .success(true)
                .message("Book Updated Successfully")
                .data(updatedBook)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Book updated successfully with ID: {}", id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book", description = "Removes a book from the library by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Book deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Book not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @Parameter(description = "ID of the book to delete") @PathVariable Long id){

        bookService.deleteBook(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Book Deleted Successfully")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Book deleted successfully with ID: {}", id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
