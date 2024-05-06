package com.ebook.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ebook.common.PaymentRequest;
import com.ebook.dao.PaymentRepository;
import com.ebook.entity.Payment;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

@Service
@Transactional
public class PaymentService {

	private PaymentRepository paymentRepository;

	@Autowired
	public PaymentService(PaymentRepository paymentRepository, @Value("${stripe.key.secret}") String secretKey) {
		super();
		this.paymentRepository = paymentRepository;
		Stripe.apiKey = secretKey;
	}

	public PaymentIntent createPaymentIntent(PaymentRequest paymentRequest) throws StripeException {
		List<String> paymentMethod = new ArrayList<>();
		paymentMethod.add("card");

		Map<String, Object> params = new HashMap<>();
		params.put("amount", paymentRequest.getAmount());
		params.put("currency", paymentRequest.getCurrency());
		params.put("payment_method_types", paymentMethod);

		return PaymentIntent.create(params);

	}

	public ResponseEntity<String> stripePayment(String userEmail) throws Exception {
		Payment payment = paymentRepository.findByUserEmail(userEmail);

		if (payment == null) {
			throw new Exception("Payment information is missing");
		}

		payment.setAmount(00.00);
		paymentRepository.save(payment);

		return new ResponseEntity<>(HttpStatus.OK);
	}

}
