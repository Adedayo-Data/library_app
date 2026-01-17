package com.lawpavillion.bk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@Schema(description = "Book Data Transfer Object")
public class BookDto {

    @Schema(description = "Unique identifier of the book", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Schema(description = "Title of the book", example = "Clean Code", required = true)
    private String title;

    @NotBlank(message = "Author cannot be blank")
    @Size(min = 1, max = 255, message = "Author name must be between 1 and 255 characters")
    @Schema(description = "Author of the book", example = "Robert C. Martin", required = true)
    private String author;

    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
            message = "Invalid ISBN format")
    @Schema(description = "ISBN of the book", example = "978-0132350884")
    private String isbn;

    @PastOrPresent(message = "Published date cannot be in the future")
    @Schema(description = "Publication date of the book", example = "2008-08-01")
    private LocalDate publishedDate;
}
