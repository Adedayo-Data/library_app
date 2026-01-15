package com.lawpavillion.bk.service;

import com.lawpavillion.bk.dto.BookDto;
import com.lawpavillion.bk.exception.BookNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.lawpavillion.bk.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.lawpavillion.bk.repository.BookRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepo;

    @Override
    public BookDto addBook(BookDto request) {

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publishedDate(request.getPublishedDate())
                .updatedAt(LocalDateTime.now())
                .build();

        // save
        Book savedBook = bookRepo.save(book);

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

        // find Book by id
        Book book = bookRepo.findById(request.getId())
                .orElseThrow(() -> new BookNotFoundException("Book Not Found!"));

        // map request to book
        book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publishedDate(request.getPublishedDate())
                .updatedAt(LocalDateTime.now())
                .build();

        // save the book
        Book updatedBook = bookRepo.save(book);

        return BookDto.builder()
                .id(updatedBook.getId())
                .title(updatedBook.getTitle())
                .author(updatedBook.getAuthor())
                .isbn(updatedBook.getIsbn())
                .publishedDate(updatedBook.getPublishedDate())
                .build();
    }

    @NonNull
    private Page<BookDto> getBookDtos(Pageable pageable, Book book) {
        Book updatedBook = bookRepo.save(book);

        if (updatedBook == null){
            return null;
        }

        return bookRepo.findAll(pageable).map(bk -> BookDto.builder()
                .id(bk.getId())
                .title(bk.getTitle())
                .author(bk.getAuthor())
                .isbn(bk.getIsbn())
                .publishedDate(bk.getPublishedDate())
                .build());
    }

    @Override
    public void deleteBook(Long id) {
        bookRepo.deleteById(id);
    }
}
