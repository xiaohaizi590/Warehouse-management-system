package net.togogo.service;

import net.togogo.dto.BookDTO;
import net.togogo.dto.BorrowRecordDTO;
import net.togogo.dto.CreateBookRequest;
import net.togogo.dto.BorrowRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    BookDTO createBook(CreateBookRequest request);
    BookDTO getBookById(Long id);
    Page<BookDTO> getAllBooks(Pageable pageable);
    Page<BookDTO> searchByTitle(String title, Pageable pageable);
    Page<BookDTO> searchByAuthor(String author, Pageable pageable);
    Page<BookDTO> searchByCategory(String category, Pageable pageable);
    BookDTO updateBook(Long id, CreateBookRequest request);
    void deleteBook(Long id);

    BorrowRecordDTO borrowBook(Long userId, BorrowRequest request);
    BorrowRecordDTO returnBook(Long recordId);
    BorrowRecordDTO renewBook(Long recordId);
    Page<BorrowRecordDTO> getBorrowRecordsByUser(Long userId, Pageable pageable);
    List<BorrowRecordDTO> getBorrowRecordsByBook(Long bookId);
    Page<BorrowRecordDTO> getOverdueRecords(Pageable pageable);
    Page<BorrowRecordDTO> getAllBorrowRecords(Pageable pageable);
}