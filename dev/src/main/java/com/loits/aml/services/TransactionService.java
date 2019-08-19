package com.loits.aml.services;

import com.loits.aml.config.LoitServiceException;
import com.loits.aml.domain.Transaction;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */
public interface TransactionService {
    Page<?> getAll(Pageable pageable, Predicate predicate, String bookmarks, String projection);

    Object create(String projection, Transaction transaction, String user, Timestamp timestamp, String module) throws LoitServiceException;
}
