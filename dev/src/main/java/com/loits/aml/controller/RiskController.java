package com.loits.aml.controller;

import com.querydsl.core.types.Predicate;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * Managing Riskrelated operations
 *
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/aml/v1/risk")
@SuppressWarnings("unchecked")
public class RiskController {
    Logger logger = LogManager.getLogger(RiskCategoryController.class);


    /**
     * Risk calculation
     *
     */

    @GetMapping(produces = "application/json")
    public @ResponseBody
    void  getRisk() {

    }
}
