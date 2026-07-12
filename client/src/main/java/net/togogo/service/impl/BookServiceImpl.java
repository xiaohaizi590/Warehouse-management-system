package net.togogo.service.impl;

import lombok.RequiredArgsConstructor;
import net.togogo.common.BusinessException;
import net.togogo.common.ResultCode;
import net.togogo.dto.BookDTO;
import net.togogo.dto.BorrowRecordDTO;
import net.togogo.dto.BorrowRequest;
import net.togogo.dto.CreateBookRequest;
import net.togogo.entity.Book;
import net.togogo.entity.BorrowRecord;
import net.togogo.entity.User;
import net.togogo.repository.BookRepository;
import net.togogo.repository.BorrowRecordRepository;
import net.togogo.repository.UserRepository;
import net.togogo.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookDTO createBook(CreateBookRequest request) {
        if (request.getIsbn() != null && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException(ResultCode.BOOK_ISBN_EXIST);
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publisher(request.getPublisher())
                .publishDate(request.getPublishDate())
                .category(request.getCategory())
                .description(request.getDescription())
                .stock(request.getStock())
                .available(request.getStock())
                .build();

        Book saved = bookRepository.save(book);
        return convertToBookDTO(saved);
    }

    @Override
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        return convertToBookDTO(book);
    }

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(this::convertToBookDTO);
    }

    @Override
    public Page<BookDTO> searchByTitle(String title, Pageable pageable) {
        return bookRepository.findByTitleContaining(title, pageable).map(this::convertToBookDTO);
    }

    @Override
    public Page<BookDTO> searchByAuthor(String author, Pageable pageable) {
        return bookRepository.findByAuthorContaining(author, pageable).map(this::convertToBookDTO);
    }

    @Override
    public Page<BookDTO> searchByCategory(String category, Pageable pageable) {
        return bookRepository.findByCategory(category, pageable).map(this::convertToBookDTO);
    }

    @Override
    @Transactional
    public BookDTO updateBook(Long id, CreateBookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublisher(request.getPublisher());
        book.setPublishDate(request.getPublishDate());
        book.setCategory(request.getCategory());
        book.setDescription(request.getDescription());
        book.setStock(request.getStock());

        Book updated = bookRepository.save(book);
        return convertToBookDTO(updated);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        Long borrowedCount = borrowRecordRepository.countByBookIdAndStatus(id, BorrowRecord.Borrowstatus.BORROWED);
        if (borrowedCount > 0) {
            throw new BusinessException(ResultCode.BOOK_BORROWED_CANNOT_DELETE);
        }

        bookRepository.delete(book);
    }

    @Override
    @Transactional
    public BorrowRecordDTO borrowBook(Long userId, BorrowRequest request) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        if (book.getAvailable() <= 0) {
            throw new BusinessException(ResultCode.BOOK_NOT_AVAILABLE);
        }

        boolean alreadyBorrowed = borrowRecordRepository.existsByBookIdAndUserIdAndStatus(
                request.getBookId(), userId, BorrowRecord.Borrowstatus.BORROWED);
        if (alreadyBorrowed) {
            throw new BusinessException(ResultCode.BOOK_ALREADY_BORROWED);
        }

        book.setAvailable(book.getAvailable() - 1);
        bookRepository.save(book);

        int days = request.getBorrowDays() != null ? request.getBorrowDays() : 30;
        LocalDateTime borrowTime = LocalDateTime.now();
        LocalDateTime dueTime = borrowTime.plusDays(days);

        BorrowRecord record = BorrowRecord.builder()
                .bookId(request.getBookId())
                .userId(userId)
                .borrowTime(borrowTime)
                .dueTime(dueTime)
                .status(BorrowRecord.Borrowstatus.BORROWED)
                .build();

        BorrowRecord saved = borrowRecordRepository.save(record);
        return convertToBorrowRecordDTO(saved, book.getTitle(), book.getAuthor());
    }

    @Override
    @Transactional
    public BorrowRecordDTO returnBook(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        if (record.getStatus() != BorrowRecord.Borrowstatus.BORROWED) {
            throw new BusinessException(ResultCode.RECORD_NOT_BORROWED);
        }

        Book book = bookRepository.findById(record.getBookId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        book.setAvailable(book.getAvailable() + 1);
        bookRepository.save(book);

        record.setReturnTime(LocalDateTime.now());
        record.setStatus(BorrowRecord.Borrowstatus.RETURNED);

        BorrowRecord saved = borrowRecordRepository.save(record);
        return convertToBorrowRecordDTO(saved, book.getTitle(), book.getAuthor());
    }

    @Override
    @Transactional
    public BorrowRecordDTO renewBook(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        if (record.getStatus() != BorrowRecord.Borrowstatus.BORROWED) {
            throw new BusinessException(ResultCode.RECORD_NOT_BORROWED);
        }

        if (record.getRenewCount() >= 2) {
            throw new BusinessException(ResultCode.MAX_RENEW_COUNT_EXCEEDED);
        }

        record.setDueTime(record.getDueTime().plusDays(30));
        record.setRenewCount(record.getRenewCount() + 1);

        BorrowRecord saved = borrowRecordRepository.save(record);

        Book book = bookRepository.findById(record.getBookId()).orElse(null);
        String bookTitle = book != null ? book.getTitle() : "未知";
        String bookAuthor = book != null ? book.getAuthor() : "未知";

        return convertToBorrowRecordDTO(saved, bookTitle, bookAuthor);
    }

    @Override
    public Page<BorrowRecordDTO> getBorrowRecordsByUser(Long userId, Pageable pageable) {
        return borrowRecordRepository.findByUserId(userId, pageable)
                .map(record -> {
                    Book book = bookRepository.findById(record.getBookId()).orElse(null);
                    String title = book != null ? book.getTitle() : "未知";
                    String author = book != null ? book.getAuthor() : "未知";
                    return convertToBorrowRecordDTO(record, title, author);
                });
    }

    @Override
    public List<BorrowRecordDTO> getBorrowRecordsByBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        return borrowRecordRepository.findByBookIdOrderByBorrowTimeDesc(bookId).stream()
                .map(record -> {
                    User user = userRepository.findById(record.getUserId()).orElse(null);
                    String username = user != null ? user.getUsername() : "未知";
                    return convertToBorrowRecordDTO(record, book.getTitle(), book.getAuthor(), username);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<BorrowRecordDTO> getOverdueRecords(Pageable pageable) {
        return borrowRecordRepository.findByStatusAndDueTimeBefore(
                BorrowRecord.Borrowstatus.BORROWED, LocalDateTime.now(), pageable)
                .map(record -> {
                    Book book = bookRepository.findById(record.getBookId()).orElse(null);
                    String title = book != null ? book.getTitle() : "未知";
                    String author = book != null ? book.getAuthor() : "未知";
                    User user = userRepository.findById(record.getUserId()).orElse(null);
                    String username = user != null ? user.getUsername() : "未知";
                    return convertToBorrowRecordDTO(record, title, author, username);
                });
    }

    @Override
    public Page<BorrowRecordDTO> getAllBorrowRecords(Pageable pageable) {
        return borrowRecordRepository.findAll(pageable)
                .map(record -> {
                    Book book = bookRepository.findById(record.getBookId()).orElse(null);
                    String title = book != null ? book.getTitle() : "未知";
                    String author = book != null ? book.getAuthor() : "未知";
                    User user = userRepository.findById(record.getUserId()).orElse(null);
                    String username = user != null ? user.getUsername() : "未知";
                    return convertToBorrowRecordDTO(record, title, author, username);
                });
    }

    private BookDTO convertToBookDTO(Book book) {
        return BookDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publisher(book.getPublisher())
                .publishDate(book.getPublishDate())
                .category(book.getCategory())
                .description(book.getDescription())
                .stock(book.getStock())
                .available(book.getAvailable())
                .build();
    }

    private BorrowRecordDTO convertToBorrowRecordDTO(BorrowRecord record, String bookTitle, String bookAuthor) {
        User user = userRepository.findById(record.getUserId()).orElse(null);
        String username = user != null ? user.getUsername() : "未知";
        return convertToBorrowRecordDTO(record, bookTitle, bookAuthor, username);
    }

    private BorrowRecordDTO convertToBorrowRecordDTO(BorrowRecord record, String bookTitle,
                                                     String bookAuthor, String username) {
        Long overdueDays = null;
        if (record.getStatus() == BorrowRecord.Borrowstatus.BORROWED
                && record.getDueTime().isBefore(LocalDateTime.now())) {
            overdueDays = ChronoUnit.DAYS.between(record.getDueTime(), LocalDateTime.now());
        }

        return BorrowRecordDTO.builder()
                .id(record.getId())
                .bookId(record.getBookId())
                .bookTitle(bookTitle)
                .bookAuthor(bookAuthor)
                .userId(record.getUserId())
                .userName(username)
                .borrowTime(record.getBorrowTime())
                .dueTime(record.getDueTime())
                .returnTime(record.getReturnTime())
                .renewCount(record.getRenewCount())
                .status(record.getStatus())
                .overdueDays(overdueDays)
                .build();
    }
}