package com.loits.aml.services.impl;

import com.loits.aml.repo.AmlRiskRepository;
import com.loits.aml.services.AmlRiskHistoryService;
import com.loits.aml.services.projections.LovAmlRisk;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */

@Service
public class AmlRiskHistoryServiceImpl implements AmlRiskHistoryService {

    Logger logger = LogManager.getLogger(AmlRiskHistoryServiceImpl.class);

    @Autowired
    private AmlRiskRepository amlRiskRepository;

    @Autowired
    private ProjectionFactory projectionFactory;

    public Page<?> getAll(Pageable pageable,
                          Predicate predicate,
                          String bookmarks,
                          String projection) {
        logger.debug("AMLRisk find CRUD operation started");

        BooleanBuilder bb = new BooleanBuilder(predicate);
        //QAmlRisk amlRisk = QAmlRisk.amlRisk;

        //split and separate ids sent as a string
        if (!StringUtils.isEmpty(bookmarks)) {
            ArrayList<Long> ids = new ArrayList<>();
            for (String id : bookmarks.split(",")) {
                ids.add(Long.parseLong(id));
            }
            //bb.and(amlRisk.id.in(ids));
        }

        return amlRiskRepository.findAll(bb.getValue(), pageable).map(
                amlrisk1 -> projectionFactory.createProjection(LovAmlRisk.class, amlrisk1)
        );
    }



}
