package com.loits.aml.services.impl;

import com.loits.aml.config.Translator;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.domain.QTransaction;
import com.loits.aml.domain.Transaction;
import com.loits.aml.repo.TransactionRepository;
import com.loits.aml.services.TransactionService;
import com.loits.aml.services.projections.LovTransactions;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

@Service
public class TransactionServiceImpl implements TransactionService {

    Logger logger = LogManager.getLogger(TransactionServiceImpl.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ProjectionFactory projectionFactory;

    public Page<?> getAll(Pageable pageable,
                          Predicate predicate,
                          String bookmarks,
                          String projection) {
        logger.debug("Transaction find CRUD operation started");

        BooleanBuilder bb = new BooleanBuilder(predicate);
        QTransaction transaction = QTransaction.transaction;

        //split and separate ids sent as a string
        if (!StringUtils.isEmpty(bookmarks)) {
            ArrayList<Integer> ids = new ArrayList<>();
            for (String id : bookmarks.split(",")) {
                ids.add(Integer.parseInt(id));
            }
            bb.and(transaction.txnId.in(ids));
        }
        return transactionRepository.findAll(bb.getValue(), pageable).map(
                transaction1 -> projectionFactory.createProjection(LovTransactions.class, transaction1)
        );
    }

    @Override
    public Object create(String projection,
                         Transaction transaction,
                         String user,
                         Timestamp timestamp,
                         String module)
            throws FXDefaultException {

        //Check null (**Check if custom error messages are required for each)
        if (user == null || timestamp == null || module == null) {
            throw new FXDefaultException("3000","NULL" ,Translator.toLocale("REQUIRED"), new Date(), HttpStatus.BAD_REQUEST,false);

        }

        //overriding channelId to 0
        transaction.setTxnId(0);

        //overriding the values sent by user with the header variable values
        transaction.setCreatedBy(user);
        transaction.setCreatedOn(timestamp);
        transaction.setModule(module);

        return projectionFactory.createProjection(LovTransactions.class,
                transactionRepository.save(transaction)
        );
    }

}
