package com.ebook.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ebook.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	Payment findByUserEmail(String userEmail);
}
