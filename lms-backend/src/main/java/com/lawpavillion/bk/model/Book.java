package com.lawpavillion.bk.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @NotEmpty(message = "title cannot be null")
    @Size(min = 3, message = "Enter a valid title")
    private String title;

    @NotEmpty(message = "author cannot be null")
    @Size(min = 3, message = "Enter a valid title")
    private String author;


    private String isbn;
    private LocalDate publishedDate;

    @CreationTimestamp
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
