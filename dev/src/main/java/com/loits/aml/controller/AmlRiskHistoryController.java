package com.loits.aml.controller;

import com.loits.aml.domain.AmlRisk;
import com.loits.aml.services.AmlRiskService;
import com.querydsl.core.types.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
@RequestMapping("/history/v1")
@SuppressWarnings("unchecked")
public class AmlRiskHistoryController {

    Logger logger = LogManager.getLogger(AmlRiskHistoryController.class);

    @Autowired
    AmlRiskService channelRiskService;

    @GetMapping(path = "/{tenent}", produces = "application/json")
    public @ResponseBody
    Page<?> getChannelRisks(@PathVariable(value = "tenent") String tenent,
                            @PageableDefault(size = 10) Pageable pageable,
                            @QuerydslPredicate(root = AmlRisk.class) Predicate predicate,
                            @RequestParam(value = "bookmarks", required = false) String bookmarks,
                            @RequestParam(name = "projection", defaultValue = "ChannelRiskLOV") String projection) {

        logger.debug(String.format("Loading Channel details.(Projection: %s )",
                projection));

        return channelRiskService.getAll(pageable, predicate, bookmarks, projection);
    }
}
