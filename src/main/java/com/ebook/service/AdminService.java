package com.ebook.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ebook.common.AddBookRequest;
import com.ebook.dao.BookRepository;
import com.ebook.dao.OrderRepository;
import com.ebook.dao.ReviewRepository;
import com.ebook.entity.Book;

@Service
@Transactional
public class AdminService {
	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private ReviewRepository reviewRepository;

	public void addNewBook(AddBookRequest addBookRequest) {
		Book book = new Book();
		book.setTitle(addBookRequest.getTitle());
		book.setDescription(addBookRequest.getDescription());
		book.setAuthor(addBookRequest.getAuthor());
		book.setCategory(addBookRequest.getCategory());
		book.setCopies(addBookRequest.getCopies());
		book.setCopiesAvailable(addBookRequest.getCopies());
		book.setImage(addBookRequest.getImage());

		bookRepository.save(book);
	}

	public void increaseBook(Long bookId) throws Exception {
		Optional<Book> book = bookRepository.findById(bookId);
		if (!book.isPresent()) {
			throw new Exception("Book not found");
		}

		book.get().setCopiesAvailable(book.get().getCopiesAvailable() + 1);
		book.get().setCopies(book.get().getCopies() + 1);

		bookRepository.save(book.get());
	}

	public void decreaseBook(Long bookId) throws Exception {
		Optional<Book> book = bookRepository.findById(bookId);
		if (!book.isPresent() || book.get().getCopiesAvailable() <= 0 || book.get().getCopies() <= 0) {
			throw new Exception("Book not found");
		}

		book.get().setCopiesAvailable(book.get().getCopiesAvailable() - 1);
		book.get().setCopies(book.get().getCopies() - 1);

		bookRepository.save(book.get());
	}
	
	public void deleteBook(Long bookId) throws Exception {
		Optional<Book> book = bookRepository.findById(bookId);
		if (!book.isPresent()) {
			throw new Exception("Book not found");
		}
		
		bookRepository.delete(book.get());
		orderRepository.deleteAllByBookId(bookId);
		reviewRepository.deleteAllByBookId(bookId);
	}
}
