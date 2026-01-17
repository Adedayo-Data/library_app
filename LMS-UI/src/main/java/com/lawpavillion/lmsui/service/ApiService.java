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
     * Expects backend to return ApiResponse<Page<Book>> format.
     */
    public Page<Book> getBooks(int page, int size) {
        try {
            String url = BASE_URL + "?page=" + page + "&size=" + size;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Parse the ApiResponse wrapper
                Map<String, Object> apiResponse = gson.fromJson(response.getBody(), Map.class);
                
                // Check if the response was successful
                Boolean success = (Boolean) apiResponse.get("success");
                if (success == null || !success) {
                    System.err.println("API returned unsuccessful response");
                    return Page.empty();
                }
                
                // Extract the data (which contains the Page)
                Map<String, Object> pageData = (Map<String, Object>) apiResponse.get("data");
                if (pageData == null) {
                    // No data means empty result, not an error
                    return Page.empty();
                }
                
                // Extract content array
                List<Map<String, Object>> content = (List<Map<String, Object>>) pageData.get("content");
                if (content == null || content.isEmpty()) {
                    // Empty content is valid, just return empty page
                    return Page.empty();
                }
                
                // Convert content to Book objects
                Book[] books = new Book[content.size()];
                for (int i = 0; i < content.size(); i++) {
                    books[i] = gson.fromJson(gson.toJson(content.get(i)), Book.class);
                }
                
                // Extract pagination metadata
                int totalElements = ((Double) pageData.get("totalElements")).intValue();
                int totalPages = ((Double) pageData.get("totalPages")).intValue();
                
                return new PageImpl<>(Arrays.asList(books), PageRequest.of(page, size), totalElements);
            }
            return Page.empty();
        } catch (Exception e) {
            System.err.println("Error fetching paginated books: " + e.getMessage());
            e.printStackTrace();
            // Return empty page instead of throwing exception
            return Page.empty();
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
            
            ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL, request, String.class);
            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                // Parse ApiResponse wrapper
                Map<String, Object> apiResponse = gson.fromJson(response.getBody(), Map.class);
                Map<String, Object> bookData = (Map<String, Object>) apiResponse.get("data");
                return gson.fromJson(gson.toJson(bookData), Book.class);
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
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                // Parse ApiResponse wrapper
                Map<String, Object> apiResponse = gson.fromJson(response.getBody(), Map.class);
                Map<String, Object> bookData = (Map<String, Object>) apiResponse.get("data");
                return gson.fromJson(gson.toJson(bookData), Book.class);
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
     * Delete multiple books by ID.
     */
    public void deleteBooks(List<Long> ids) {
        for (Long id : ids) {
            deleteBook(id);
        }
    }

    /**
     * Search books by title or author using backend endpoint.
     */
    public Page<Book> searchBooks(String query, int page, int size) {
        try {
            String url = BASE_URL + "/search?query=" + query + "&page=" + page + "&size=" + size;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> apiResponse = gson.fromJson(response.getBody(), Map.class);
                Map<String, Object> pageData = (Map<String, Object>) apiResponse.get("data");
                
                if (pageData == null) return Page.empty();
                
                List<Map<String, Object>> content = (List<Map<String, Object>>) pageData.get("content");
                if (content == null || content.isEmpty()) return Page.empty();
                
                Book[] books = new Book[content.size()];
                for (int i = 0; i < content.size(); i++) {
                    books[i] = gson.fromJson(gson.toJson(content.get(i)), Book.class);
                }
                
                int totalElements = ((Double) pageData.get("totalElements")).intValue();
                return new PageImpl<>(Arrays.asList(books), PageRequest.of(page, size), totalElements);
            }
            return Page.empty();
        } catch (Exception e) {
            System.err.println("Error searching books: " + e.getMessage());
            return Page.empty();
        }
    }
}
