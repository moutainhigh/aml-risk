package com.loits.aml.repo;

import com.loits.aml.domain.Transaction;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TransactionRepository extends PagingAndSortingRepository<Transaction, Integer>, QuerydslPredicateExecutor<Transaction> {
}
