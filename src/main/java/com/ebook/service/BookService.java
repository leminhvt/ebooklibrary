package com.ebook.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ebook.common.ShelfLoansResponse;
import com.ebook.dao.BookRepository;
import com.ebook.dao.HistoryRepository;
import com.ebook.dao.OrderRepository;
import com.ebook.dao.PaymentRepository;
import com.ebook.entity.Book;
import com.ebook.entity.Order;
import com.ebook.entity.Payment;
import com.ebook.entity.History;

@Service
@Transactional
public class BookService {
	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Autowired
	PaymentRepository paymentRepository;

	public Book checkoutBook(String email, Long bookId) throws Exception {
		Optional<Book> book = bookRepository.findById(bookId);

		Order order = orderRepository.findByEmailAndBookId(email, bookId);

		if (!book.isPresent() || order != null || book.get().getCopiesAvailable() <= 0) {
			throw new Exception("Book already checked out by user");
		}

		List<Order> currentBooksCheckout = orderRepository.findBookByEmail(email);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		boolean bookNeedsReturned = false;

		for (Order newOrder : currentBooksCheckout) {
			Date date1 = dateFormat.parse(newOrder.getReturnDate());
			Date date2 = dateFormat.parse(LocalDate.now().toString());

			TimeUnit timeUnit = TimeUnit.DAYS;
			double differenceInTime = timeUnit.convert(date1.getTime() - date2.getTime(), TimeUnit.MILLISECONDS);
			if (differenceInTime < 0) {
				bookNeedsReturned = true;
				break;
			}
		}

		Payment userPayment = paymentRepository.findByUserEmail(email);

		if (userPayment != null && userPayment.getAmount() > 0 || (userPayment != null && bookNeedsReturned)) {
			throw new Exception("Outstanding fees");
		}

		if (userPayment == null) {
			Payment payment = new Payment();
			payment.setAmount(00.00);
			payment.setUserEmail(email);

			paymentRepository.save(payment);
		}

		book.get().setCopiesAvailable(book.get().getCopiesAvailable() - 1);
		bookRepository.save(book.get());

		Order newOrder = new Order(email, LocalDate.now().toString(), LocalDate.now().plusDays(7).toString(),
				book.get().getId());

		orderRepository.save(newOrder);
		return book.get();

	}

	public Boolean checkoutBookByEmail(String email, Long bookId) {
		Order order = orderRepository.findByEmailAndBookId(email, bookId);
		if (order != null) {
			return true;
		} else {
			return false;
		}
	}

	public int currentLoansCount(String email) {
		return orderRepository.findBookByEmail(email).size();
	}

	public List<ShelfLoansResponse> loansResponses(String email) throws Exception {
		List<ShelfLoansResponse> shelfLoansResponses = new ArrayList<>();

		List<Order> ordersList = orderRepository.findBookByEmail(email);
		List<Long> bookIdList = new ArrayList<>();

		for (Order order : ordersList) {
			bookIdList.add(order.getBookId());
		}

		List<Book> books = bookRepository.findBooksByBookIds(bookIdList);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (Book book : books) {
			Optional<Order> optional = ordersList.stream().filter(x -> x.getBookId() == book.getId()).findFirst();

			if (optional.isPresent()) {
				Date date1 = dateFormat.parse(optional.get().getReturnDate());
				Date date2 = dateFormat.parse(LocalDate.now().toString());

				TimeUnit timeUnit = TimeUnit.DAYS;
				long inTime = timeUnit.convert(date1.getTime() - date2.getTime(), TimeUnit.MILLISECONDS);
				shelfLoansResponses.add(new ShelfLoansResponse(book, (int) inTime));
			}
		}
		return shelfLoansResponses;
	}

	public void returnBook(String userEmail, long bookId) throws Exception {
		Optional<Book> book = bookRepository.findById(bookId);
		Order order = orderRepository.findByEmailAndBookId(userEmail, bookId);

		if (!book.isPresent() || order == null) {
			throw new Exception("Book does not exits");
		}

		book.get().setCopiesAvailable(book.get().getCopiesAvailable() + 1);
		bookRepository.save(book.get());

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date1 = dateFormat.parse(order.getReturnDate());
		Date date2 = dateFormat.parse(LocalDate.now().toString());
		TimeUnit timeUnit = TimeUnit.DAYS;
		double differenceInTime = timeUnit.convert(date1.getTime() - date2.getTime(), TimeUnit.MILLISECONDS);
		if (differenceInTime < 0) {
			Payment payment = paymentRepository.findByUserEmail(userEmail);
			payment.setAmount(payment.getAmount() + (differenceInTime * -1));

			paymentRepository.save(payment);
		}

		orderRepository.deleteById(order.getId());

		History orderDetails = new History(userEmail, order.getCheckoutDate(), LocalDate.now().toString(),
				book.get().getTitle(), book.get().getAuthor(), book.get().getDescription(), book.get().getImage());

		historyRepository.save(orderDetails);
	}

	public void renewLoan(String userEmail, Long bookId) throws Exception {
		Order order = orderRepository.findByEmailAndBookId(userEmail, bookId);

		if (order == null) {
			throw new Exception("Book does not exits");
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date1 = dateFormat.parse(order.getReturnDate());
		Date date2 = dateFormat.parse(LocalDate.now().toString());

		if (date1.compareTo(date2) > 0 || date1.compareTo(date2) == 0) {
			order.setReturnDate(LocalDate.now().plusDays(7).toString());
			orderRepository.save(order);
		}
	}
}
