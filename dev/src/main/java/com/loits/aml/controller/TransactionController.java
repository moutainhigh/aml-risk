package com.loits.aml.controller;

import com.loits.aml.config.LoitServiceException;
import com.loits.aml.domain.Transaction;
import com.loits.aml.services.TransactionService;
import com.querydsl.core.types.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;

/**
 * Managing Transaction related operations
 *
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/risk/product/transaction/v1") //check
@SuppressWarnings("unchecked")
public class TransactionController {

    Logger logger = LogManager.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    /**
     * Fetching all Transaction Records
     *
     * @param pageable
     * @param predicate
     * @param bookmarks  comma seperated record IDs
     * @param projection for data shaping
     * @return
     */

    @GetMapping(produces = "application/json")
    public @ResponseBody
    Page<?> getProductChannel(@RequestParam(value = "tenent", defaultValue = "1") String tenent,
                              @PageableDefault(size = 10) Pageable pageable,
                              @QuerydslPredicate(root = Transaction.class) Predicate predicate,
                              @RequestParam(value = "bookmarks", required = false) String bookmarks,
                              @RequestParam(name = "projection", defaultValue = "TransactionLOV") String projection) {

        logger.debug(String.format("Loading Transaction details.(Projection: %s )",
                projection));
        return transactionService.getAll(pageable, predicate, bookmarks, projection);

    }

    /**
     * Add new Transaction
     *
     * @param projection for data shaping
     * @param transaction object to be created
     * @param user user creating record
     * @param timestamp record creation timestamp
     * @param company
     * @param module
     * @return
     * @throws LoitServiceException
     */
    @PostMapping(produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> addProductChannel(@RequestParam(value = "tenent", defaultValue = "1") String tenent,
                                                @RequestParam(value = "projection") String projection,
                                                @RequestBody @Valid Transaction transaction,
                                                @RequestHeader("user") String user,
                                                @RequestParam("timestamp") Timestamp timestamp,
                                                @RequestParam("module") String module
    ) throws LoitServiceException {

        logger.debug(String.format("Creating Transaction data.(Projection: %s |" +
                " | ProductChannel : %s | User : %s " +
                " | Timestamp : %s" +
                " | Module : %s )", projection, transaction, user, timestamp, module));

        Resource resource = new Resource(transactionService.create(projection, transaction, user, timestamp, module));
        return ResponseEntity.ok(resource);
    }

}
