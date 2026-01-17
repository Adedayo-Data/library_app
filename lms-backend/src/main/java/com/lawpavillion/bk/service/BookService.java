package com.lawpavillion.bk.service;

import com.lawpavillion.bk.dto.BookDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    // Add Book
    // after the insertion of a book, it would return all the books with the book just added at the top
    // JavaFX would handle this with a Prepend Logic
    BookDto addBook(BookDto request);

    // Get Book
    Page<BookDto> getAllBooks(Pageable pageable);

    // Update books
    // after update it would return all the books with the updated book at the top
    BookDto updateBook(Long id, BookDto request);

    // Delete books
    // would use a pop up to pass the message (Success or failure)
    void deleteBook(Long id);

}
