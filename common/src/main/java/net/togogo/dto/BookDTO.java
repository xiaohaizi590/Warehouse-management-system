package net.togogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private String category;
    private String description;
    private Integer stock;
    private Integer available;
    private LocalDateTime publishDate;

}