package com.loits.aml.controller;


import com.loits.aml.config.LoitServiceException;
import com.loits.aml.domain.RiskWeightage;
import com.loits.aml.services.model.NewRiskWeightage;
import com.loits.aml.services.RiskWeightageService;
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
 * Managing Risk-Weightage related operations
 *
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/risk-weightage/v1")
@SuppressWarnings("unchecked")
public class RiskWeightageController {

    Logger logger = LogManager.getLogger(RiskWeightageController.class);

    @Autowired
    private RiskWeightageService riskWeightageService;

    /**
     * Fetching all RiskWeightage Records
     *
     * @param pageable
     * @param predicate
     * @param bookmarks  comma seperated record IDs
     * @param projection for data shaping
     * @return
     */

    @GetMapping(produces = "application/json")
    public @ResponseBody
    Page<?> getRiskWeightage(@RequestParam(value = "tenent", defaultValue = "1") String tenent,
                             @PageableDefault(size = 10) Pageable pageable,
                             @QuerydslPredicate(root = RiskWeightage.class) Predicate predicate,
                             @RequestParam(value = "bookmarks", required = false) String bookmarks,
                             @RequestParam(name = "projection", defaultValue = "RiskWeightageLov") String projection) {

        logger.debug(String.format("Loading RiskWeightage details.(Projection: %s )",
                projection));
        return riskWeightageService.getAll(pageable, predicate, bookmarks, projection);

    }

    /**
     * Add new RiskWeightage
     *
     * @param projection for data shaping
     * @param riskWeightage object to be created
     * @param user user creating record
     * @param timestamp record creation timestamp
     * @param module
     * @return
     * @throws LoitServiceException
     */
    @PostMapping(produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> addRiskWeightage(@RequestParam(value = "tenent", defaultValue = "1") String tenent,
                                                @RequestParam(value = "projection") String projection,
                                                @RequestBody @Valid RiskWeightage riskWeightage,
                                                @RequestHeader("user") String user,
                                                @RequestParam("timestamp")Timestamp timestamp,
                                                @RequestParam("module") String module
            ) throws LoitServiceException{

        logger.debug(String.format("Creating RiskWeightage data.(Projection: %s |" +
                " | RiskWeightage : %s | User : %s " +
                " | Timestamp : %s " +
                " | Module : %s )", projection, riskWeightage, user, timestamp, module));

        Resource resource = new Resource(riskWeightageService.create(projection, riskWeightage, user, timestamp, module));
        return ResponseEntity.ok(resource);
    }

    /**
     * Update existing RiskWeightage
     *
     * @param id id of the RiskWeightage to be altered
     * @param projection for data shaping
     * @param newRiskWeightage object with values to be edited
     * @param user user modifying record
     * @param timestamp modified timestamp
     * @return
     * @throws LoitServiceException
     */
    @PutMapping(path = "/{id}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> updateRiskWeightage(@RequestParam(value = "tenent", defaultValue = "1") String tenent,
                                                 @PathVariable(value = "id") Integer id,
                                                 @RequestParam(value = "projection") String projection,
                                                 @RequestBody NewRiskWeightage newRiskWeightage,
                                                 @RequestHeader("user") String user,
                                                 @RequestParam("timestamp")Timestamp timestamp
    ) throws LoitServiceException {


        newRiskWeightage.setId(id);

        logger.debug(String.format("Updating RiskWeightage data.(Projection: %s "+
                " | New RiskWeightage : %s | User : %s" +
                " | Timestamp : %s | )", projection, newRiskWeightage, user, timestamp));

        Resource resource = new Resource(riskWeightageService.update(projection, newRiskWeightage, user, timestamp));
        return ResponseEntity.ok(resource);
    }

    /**
     * Delete existing RiskWeightage
     *
     * @param id id of the RiskWeightage to be deleted
     * @param projection for data shaping
     * @return
     * @throws LoitServiceException
     */
    @DeleteMapping(path = "/{id}")
    public @ResponseBody
    ResponseEntity<?> deleteProductChannel(@RequestParam(value = "tenent", defaultValue = "1") String tenent,
                                           @PathVariable(value = "id") Integer id,
                                           @RequestParam(value = "projection") String projection)
            throws LoitServiceException {


        logger.debug(String.format("Deleting RiskWeightage data.(Projection: %s |"  +
                " RiskWeightage Id : %s)", projection, id));

        Resource resource = new Resource(riskWeightageService.delete(projection, id));
        return ResponseEntity.ok(resource);
    }
}
