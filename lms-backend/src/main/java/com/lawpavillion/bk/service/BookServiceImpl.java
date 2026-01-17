package com.lawpavillion.bk.service;

import com.lawpavillion.bk.dto.BookDto;
import com.lawpavillion.bk.exception.BookNotFoundException;
import lombok.RequiredArgsConstructor;
import com.lawpavillion.bk.model.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.lawpavillion.bk.repository.BookRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepo;

    @Override
    public BookDto addBook(BookDto request) {
        log.info("Adding new book with title: {}", request.getTitle());

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publishedDate(request.getPublishedDate())
                .updatedAt(LocalDateTime.now())
                .build();

        Book savedBook = bookRepo.save(book);
        log.debug("Book saved successfully with ID: {}", savedBook.getId());
        return BookDto.builder()
                .id(savedBook.getId())
                .title(savedBook.getTitle())
                .author(savedBook.getAuthor())
                .isbn(savedBook.getIsbn())
                .publishedDate(savedBook.getPublishedDate())
                .build();
    }

    @Override
    public Page<BookDto> getAllBooks(Pageable pageable) {
        log.info("Fetching all books with pagination - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return bookRepo.findAll(pageable).map(bk -> BookDto.builder()
                .id(bk.getId())
                .title(bk.getTitle())
                .author(bk.getAuthor())
                .isbn(bk.getIsbn())
                .publishedDate(bk.getPublishedDate())
                .build());
    }

    @Override
    public BookDto updateBook(Long id, BookDto request) {
        log.info("Updating book with ID: {}", id);

        // Find existing book by path variable id (not request.getId())
        Book existingBook = bookRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Book not found with ID: {}", id);
                    return new BookNotFoundException("Book Not Found with ID: " + id);
                });

        // Update the existing book entity (preserves ID and createdAt)
        existingBook.setTitle(request.getTitle());
        existingBook.setAuthor(request.getAuthor());
        existingBook.setIsbn(request.getIsbn());
        existingBook.setPublishedDate(request.getPublishedDate());
        existingBook.setUpdatedAt(LocalDateTime.now());

        // Save the updated book
        Book updatedBook = bookRepo.save(existingBook);
        log.debug("Book updated successfully with ID: {}", updatedBook.getId());

        return BookDto.builder()
                .id(updatedBook.getId())
                .title(updatedBook.getTitle())
                .author(updatedBook.getAuthor())
                .isbn(updatedBook.getIsbn())
                .publishedDate(updatedBook.getPublishedDate())
                .build();
    }

    @Override
    public void deleteBook(Long id) {
        log.info("Deleting book with ID: {}", id);
        
        // Check if book exists before deleting
        if (!bookRepo.existsById(id)) {
            log.error("Cannot delete - Book not found with ID: {}", id);
            throw new BookNotFoundException("Book Not Found with ID: " + id);
        }
        
        bookRepo.deleteById(id);
        log.debug("Book deleted successfully with ID: {}", id);
    }
}
