package com.loits.aml.services.impl;

import com.loits.aml.config.LoitServiceException;
import com.loits.aml.config.Translator;
import com.loits.aml.domain.*;
import com.loits.aml.domain.QRiskWeightage;
import com.loits.aml.repo.RiskWeightageHistoryRepository;
import com.loits.aml.repo.RiskWeightageRepository;
import com.loits.aml.services.model.NewRiskWeightage;
import com.loits.aml.services.projections.LovRiskWeightages;
import com.loits.aml.services.RiskWeightageService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */

@Service
public class RiskWeightageServiceImpl implements RiskWeightageService {

    Logger logger = LogManager.getLogger(RiskWeightageServiceImpl.class);

    @Autowired
    private RiskWeightageRepository riskWeightageRepository;

    @Autowired
    private RiskWeightageHistoryRepository riskWeightageHistoryRepository;

    @Autowired
    private ProjectionFactory projectionFactory;

    public Page<?> getAll(Pageable pageable,
                          Predicate predicate,
                          String bookmarks,
                          String projection) {
        logger.debug("RiskWeightage find CRUD operation started");

        BooleanBuilder bb = new BooleanBuilder(predicate);
        QRiskWeightage riskWeightage = QRiskWeightage.riskWeightage;

        //split and separate ids sent as a string
        if (!StringUtils.isEmpty(bookmarks)) {
            ArrayList<Integer> ids = new ArrayList<>();
            for (String id : bookmarks.split(",")) {
                ids.add(Integer.parseInt(id));
            }
            bb.and(riskWeightage.id.in(ids));
        }
        return riskWeightageRepository.findAll(bb.getValue(), pageable).map(
                riskWeightage1 -> projectionFactory.createProjection(LovRiskWeightages.class, riskWeightage1)
        );
    }

    @Override
    public Object create(String projection,
                         RiskWeightage riskWeightage,
                         String user,
                         Timestamp timestamp,
                         String module)
            throws LoitServiceException {

        //Check null (**Check if custom error messages are required for each)
        if (user == null || timestamp == null || module == null) {
            throw new LoitServiceException(Translator.toLocale("REQUIRED"), "NULL");
        }

        //overriding id to 0
        riskWeightage.setId(0);

        //overriding the values sent by user with the header variable values
        riskWeightage.setCreatedBy(user);
        riskWeightage.setCreatedOn(timestamp);
        riskWeightage.setModule(module);
        riskWeightage.setVersion(Long.valueOf(1));


        return projectionFactory.createProjection(LovRiskWeightages.class,
                riskWeightageRepository.save(riskWeightage)
        );
    }

    @Override
    public Object update(String projection,
                         NewRiskWeightage newRiskWeightage,
                         String user,
                         Timestamp timestamp)
            throws LoitServiceException {

        Integer id = newRiskWeightage.getId();

        //Check null (**Check if custom error messages are required for each)
        if (id == null || user == null || timestamp == null ) {
            throw new LoitServiceException(Translator.toLocale("REQUIRED"),
                    "NULL");
        }

        //Check for availability of RiskWeightage by id
        if (!riskWeightageRepository.existsById(id)) {
            //RiskWeightage not found
            throw new LoitServiceException(Translator.toLocale("NO_DATA_FOUND_RW"),
                    "NO_DATA_FOUND");
        }

        //Get RiskWeightage object
        RiskWeightage riskWeightage = riskWeightageRepository.findById(id).get();

        //Check if record version has changed
        long currentVersion = riskWeightage.getVersion();
        if (newRiskWeightage.getVersion() == null || newRiskWeightage.getVersion().compareTo(currentVersion) != 0) {
            throw new LoitServiceException(Translator.toLocale("VERSION_MISMATCH"),
                    "VERSION_MISMATCH");
        }


        saveHistoryRecord(riskWeightage);

        //Editable fields being updated //editable fields not given in doc (assumed)
        riskWeightage.setCategory(newRiskWeightage.getCategory());
        riskWeightage.setName(newRiskWeightage.getName());
        riskWeightage.setWeightage(newRiskWeightage.getWeightage());
        riskWeightage.setStatus(newRiskWeightage.getStatus());

        //user and time being updated
        riskWeightage.setCreatedBy(user);
        riskWeightage.setCreatedOn(timestamp);

        //increment version
        riskWeightage.setVersion(newRiskWeightage.getVersion() + 1);

        return projectionFactory.createProjection(LovRiskWeightages.class,
                riskWeightageRepository.save(riskWeightage));
    }

    @Override
    public Object delete(String projection,
                         Integer id)
            throws LoitServiceException {

        //Check for availability of RiskWeightage by id
        if (!riskWeightageRepository.existsById(id)) {
            //RiskWeightage not found
            throw new LoitServiceException(Translator.toLocale("NO_DATA_FOUND_RW"),
                    "NO_DATA_FOUND");
        }

        RiskWeightage riskWeightage = riskWeightageRepository.findById(id).get();

        //save to history before deleting record
        saveHistoryRecord(riskWeightage);

        riskWeightageRepository.delete(riskWeightage);
        return projectionFactory.createProjection(LovRiskWeightages.class, riskWeightage);
    }


    /**
     * Save riskWeightage history record
     *
     * @param riskWeightage
     */
    public void saveHistoryRecord(RiskWeightage riskWeightage) {
        RiskWeightageHistory riskWeightageHistory = new RiskWeightageHistory();
        BeanUtils.copyProperties(riskWeightage, riskWeightageHistory);
        riskWeightageHistory.setId(0);
        riskWeightageHistory.setRiskWeightageId(riskWeightage.getId());
        riskWeightageHistoryRepository.save(riskWeightageHistory);
    }
}
