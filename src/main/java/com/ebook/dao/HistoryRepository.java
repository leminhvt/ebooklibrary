package com.ebook.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestParam;

import com.ebook.entity.History;

public interface HistoryRepository extends JpaRepository<History, Long> {
	Page<History> findBooksByEmail(@RequestParam("email") String email, Pageable pageable);
}
