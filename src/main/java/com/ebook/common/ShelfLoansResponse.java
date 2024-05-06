package com.ebook.common;

import com.ebook.entity.Book;

import lombok.Data;

@Data
public class ShelfLoansResponse {

	private Book book;
	private int daysLeft;

	public ShelfLoansResponse(Book book, int daysLeft) {
		this.book = book;
		this.daysLeft = daysLeft;
	}
}
