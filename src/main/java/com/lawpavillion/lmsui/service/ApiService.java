package com.lawpavillion.lmsui.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.lawpavillion.lmsui.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling REST API communication with the Spring Boot backend.
 * Uses RestTemplate for HTTP requests and Gson for JSON serialization.
 */
public class ApiService {
    private static final String BASE_URL = "http://localhost:8080/api/books";
    private final RestTemplate restTemplate;
    private final Gson gson;

    public ApiService() {
        this.restTemplate = new RestTemplate();
        
        // Configure Gson with LocalDate adapter
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                        LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
                .create();
    }

    /**
     * Fetch all books from the backend (without pagination).
     */
    public List<Book> getAllBooks() {
        try {
            ResponseEntity<Book[]> response = restTemplate.getForEntity(BASE_URL, Book[].class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error fetching books: " + e.getMessage());
            throw new RuntimeException("Failed to fetch books from server", e);
        }
    }

    /**
     * Fetch books with pagination support.
     * Expects backend to return Spring Data Page format.
     */
    public Page<Book> getBooks(int page, int size) {
        try {
            String url = BASE_URL + "?page=" + page + "&size=" + size;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Parse the Page response from Spring Data
                Map<String, Object> pageData = gson.fromJson(response.getBody(), Map.class);
                List<Map<String, Object>> content = (List<Map<String, Object>>) pageData.get("content");
                
                // Convert content to Book objects
                Book[] books = new Book[content.size()];
                for (int i = 0; i < content.size(); i++) {
                    books[i] = gson.fromJson(gson.toJson(content.get(i)), Book.class);
                }
                
                // Extract pagination metadata
                Map<String, Object> pageable = (Map<String, Object>) pageData.get("pageable");
                int totalElements = ((Double) pageData.get("totalElements")).intValue();
                int totalPages = ((Double) pageData.get("totalPages")).intValue();
                
                return new PageImpl<>(Arrays.asList(books), PageRequest.of(page, size), totalElements);
            }
            return Page.empty();
        } catch (Exception e) {
            System.err.println("Error fetching paginated books: " + e.getMessage());
            // Fallback to non-paginated if pagination not supported
            List<Book> allBooks = getAllBooks();
            return new PageImpl<>(allBooks, PageRequest.of(page, size), allBooks.size());
        }
    }

    /**
     * Add a new book to the library.
     */
    public Book addBook(Book book) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String bookJson = gson.toJson(book);
            HttpEntity<String> request = new HttpEntity<>(bookJson, headers);
            
            ResponseEntity<Book> response = restTemplate.postForEntity(BASE_URL, request, Book.class);
            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to add book");
        } catch (Exception e) {
            System.err.println("Error adding book: " + e.getMessage());
            throw new RuntimeException("Failed to add book to server", e);
        }
    }

    /**
     * Update an existing book.
     */
    public Book updateBook(Long id, Book book) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String bookJson = gson.toJson(book);
            HttpEntity<String> request = new HttpEntity<>(bookJson, headers);
            
            String url = BASE_URL + "/" + id;
            ResponseEntity<Book> response = restTemplate.exchange(url, HttpMethod.PUT, request, Book.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to update book");
        } catch (Exception e) {
            System.err.println("Error updating book: " + e.getMessage());
            throw new RuntimeException("Failed to update book on server", e);
        }
    }

    /**
     * Delete a book by ID.
     */
    public void deleteBook(Long id) {
        try {
            String url = BASE_URL + "/" + id;
            restTemplate.delete(url);
        } catch (Exception e) {
            System.err.println("Error deleting book: " + e.getMessage());
            throw new RuntimeException("Failed to delete book from server", e);
        }
    }

    /**
     * Search books by title or author.
     */
    public List<Book> searchBooks(String query) {
        try {
            List<Book> allBooks = getAllBooks();
            String lowerQuery = query.toLowerCase();
            
            return allBooks.stream()
                    .filter(book -> 
                        book.getTitle().toLowerCase().contains(lowerQuery) ||
                        book.getAuthor().toLowerCase().contains(lowerQuery))
                    .toList();
        } catch (Exception e) {
            System.err.println("Error searching books: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
