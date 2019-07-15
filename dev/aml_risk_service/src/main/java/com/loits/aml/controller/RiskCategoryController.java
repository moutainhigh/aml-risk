package com.loits.aml.controller;


import com.loits.aml.config.LoitServiceException;
import com.loits.aml.domain.RiskCategory;
import com.loits.aml.services.model.NewRiskCategory;
import com.loits.aml.services.RiskCategoryService;
import com.querydsl.core.types.Predicate;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
 * Managing RiskCategory related operations
 *
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/aml/v1/risk-category")
@SuppressWarnings("unchecked")
public class RiskCategoryController {

    Logger logger = LogManager.getLogger(RiskCategoryController.class);

    @Autowired
    private RiskCategoryService riskCategoryService;

    /**
     * Fetching all RiskCategory Records
     *
     * @param pageable
     * @param predicate
     * @param bookmarks  comma seperated record IDs
     * @param projection for data shaping
     * @return
     */

    @GetMapping(produces = "application/json")
    public @ResponseBody
    Page<?> getRiskCategory(@PageableDefault(size = 10) Pageable pageable,
                            @QuerydslPredicate(root = RiskCategory.class) Predicate predicate,
                            @RequestParam(value = "bookmarks", required = false) String bookmarks,
                            @RequestParam(name = "projection", defaultValue = "RiskCategoryLov") String projection) {

        logger.debug(String.format("Loading RiskCategory details.(Projection: %s )",
                projection));
        return riskCategoryService.getAll(pageable, predicate, bookmarks, projection);

    }

    /**
     * Add new RiskCategory
     *
     * @param projection for data shaping
     * @param riskCategory object to be created
     * @param user user creating record
     * @param timestamp record creation timestamp
     * @param company
     * @param module
     * @return
     * @throws LoitServiceException
     */
    @PostMapping(produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> addRiskCategory(
            @RequestParam(value = "projection") String projection,
            @RequestBody @Valid RiskCategory riskCategory,
            @RequestHeader("user") String user,
            @RequestParam("timestamp")Timestamp timestamp,
            @RequestParam("company")String company,
            @RequestParam("module") String module
            ) throws LoitServiceException {

        logger.debug(String.format("Creating RiskCategory data.(Projection: %s |" +
                " | RiskCategory : %s | User : %s " +
                " | Timestamp : %s | Company : %s" +
                " | Module : %s )", projection, riskCategory, user, timestamp, company, module));

        Resource resource = new Resource(riskCategoryService.create(projection, riskCategory, user, timestamp, company, module));
        return ResponseEntity.ok(resource);
    }

    /**
     * Update existing RiskCategory
     *
     * @param id of the riskcategory to be altered
     * @param projection for data shaping
     * @param newRiskCategory object with values to be edited
     * @param user user modifying record
     * @param timestamp modified timestamp
     * @return
     * @throws LoitServiceException
     */
    @PutMapping(path = "/{id}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> updateRiskCategory(@PathVariable(value = "id") Integer id,
                                                @RequestParam(value = "projection") String projection,
                                                @RequestBody NewRiskCategory newRiskCategory,
                                                @RequestHeader("user") String user,
                                                @RequestParam("timestamp")Timestamp timestamp
    ) throws LoitServiceException {

        newRiskCategory.setId(id);

        logger.debug(String.format("Updating RiskCategory data.(Projection: %s "+
                " | New RiskCategory : %s | User : %s" +
                " | Timestamp : %s | )", projection, newRiskCategory, user, timestamp));

        Resource resource = new Resource(riskCategoryService.update(projection, newRiskCategory, user, timestamp));
        return ResponseEntity.ok(resource);
    }

    /**
     * Delete existing RiskCategory
     *
     * @param id of the riskcategory to be deleted
     * @param projection for data shaping
     * @return
     * @throws LoitServiceException
     */
    @DeleteMapping(path = "/{id}")
    public @ResponseBody
    ResponseEntity<?> deleteRiskCategory(@PathVariable(value = "id") Integer id,
                                         @RequestParam(value = "projection") String projection)
            throws LoitServiceException {


        logger.debug(String.format("Deleting RiskCategory data.(Projection: %s |"  +
                " RiskCategory id : %s)", projection, id));

        Resource resource = new Resource(riskCategoryService.delete(projection, id));
        return ResponseEntity.ok(resource);
    }
}
