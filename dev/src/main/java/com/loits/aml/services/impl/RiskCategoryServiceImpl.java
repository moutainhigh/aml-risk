package com.loits.aml.services.impl;

import com.loits.aml.config.LoitServiceException;
import com.loits.aml.config.Translator;
import com.loits.aml.domain.QRiskCategory;
import com.loits.aml.domain.RiskCategory;
import com.loits.aml.domain.RiskCategoryHistory;
import com.loits.aml.repo.RiskCategoryHistoryRepository;
import com.loits.aml.repo.RiskCategoryRepository;
import com.loits.aml.repo.RiskWeightageRepository;
import com.loits.aml.services.projections.LovRiskCategories;
import com.loits.aml.services.model.NewRiskCategory;
import com.loits.aml.services.RiskCategoryService;
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
public class RiskCategoryServiceImpl implements RiskCategoryService {

    Logger logger = LogManager.getLogger(RiskCategoryServiceImpl.class);

    @Autowired
    private RiskCategoryRepository riskCategoryRepository;

    @Autowired
    private RiskWeightageRepository riskWeightageRepository;

    @Autowired
    private RiskCategoryHistoryRepository riskCategoryHistoryRepository;

    @Autowired
    private ProjectionFactory projectionFactory;

    public Page<?> getAll(Pageable pageable,
                          Predicate predicate,
                          String bookmarks,
                          String projection) {
        logger.debug("RiskCategory find CRUD operation started");

        BooleanBuilder bb = new BooleanBuilder(predicate);
        QRiskCategory riskCategory = QRiskCategory.riskCategory;

        //split and separate ids sent as a string
        if (!StringUtils.isEmpty(bookmarks)) {
            ArrayList<Integer> ids = new ArrayList<>();
            for (String id : bookmarks.split(",")) {
                ids.add(Integer.parseInt(id));
            }
            bb.and(riskCategory.id.in(ids));
        }
        return riskCategoryRepository.findAll(bb.getValue(), pageable).map(
                riskCategory1 -> projectionFactory.createProjection(LovRiskCategories.class, riskCategory1)
        );
    }

    @Override
    public Object create(String projection,
                         RiskCategory riskCategory,
                         String user,
                         Timestamp timestamp,
                         String company,
                         String module)
            throws LoitServiceException {

        //Check null (**Check if custom error messages are required for each)
        if (user == null || timestamp == null || module == null || company == null) {
            throw new LoitServiceException(Translator.toLocale("REQUIRED"), "NULL");
        }

        //overriding channelId to 0
        riskCategory.setId(0);

        //overriding the values sent by user with the header variable values
        riskCategory.setCreatedBy(user);
        riskCategory.setCreatedOn(timestamp);
        riskCategory.setCompany(company);
        riskCategory.setModule(module);
        riskCategory.setVersion(Long.valueOf(1));


        return projectionFactory.createProjection(LovRiskCategories.class,
                riskCategoryRepository.save(riskCategory)
        );
    }

    @Override
    public Object update(String projection,
                         NewRiskCategory newRiskCategory,
                         String user,
                         Timestamp timestamp)
            throws LoitServiceException {

        Integer id = newRiskCategory.getId();

        //Check null (**Check if custom error messages are required for each)
        if (id == null || user == null || timestamp == null ) {
            throw new LoitServiceException(Translator.toLocale("REQUIRED"),
                    "NULL");
        }

        //Check for availability of RiskCategory by id
        if (!riskCategoryRepository.existsById(id)) {
            //RiskCategory not found
            throw new LoitServiceException(Translator.toLocale("NO_DATA_FOUND_RC"),
                    "NO_DATA_FOUND");
        }

        //Get riskCategory object
        RiskCategory riskCategory = riskCategoryRepository.findById(id).get();

        //Check if record version has changed
        long currentVersion = riskCategory.getVersion();
        if (newRiskCategory.getVersion() == null || newRiskCategory.getVersion().compareTo(currentVersion) != 0) {
            throw new LoitServiceException(Translator.toLocale("VERSION_MISMATCH"),
                    "VERSION_MISMATCH");
        }


        saveHistoryRecord(riskCategory);

        //Editable fields being updated
        riskCategory.setCode(newRiskCategory.getCode());
        riskCategory.setDescription(newRiskCategory.getDescription());
        riskCategory.setValueFrom(newRiskCategory.getValueFrom());
        riskCategory.setValueTo(newRiskCategory.getValueTo());
        riskCategory.setStatus(newRiskCategory.getStatus());

        //user and time being updated
        riskCategory.setCreatedBy(user);
        riskCategory.setCreatedOn(timestamp);

        //increment version
        riskCategory.setVersion(newRiskCategory.getVersion() + 1);

        return projectionFactory.createProjection(LovRiskCategories.class,
                riskCategoryRepository.save(riskCategory));
    }

    @Override
    public Object delete(String projection,
                         Integer id)
            throws LoitServiceException {

        //Check for availability of RiskCategory by id
        if (!riskCategoryRepository.existsById(id)) {
            //RiskCategory not found
            throw new LoitServiceException(Translator.toLocale("NO_DATA_FOUND_RC"),
                    "NO_DATA_FOUND");
        }

        RiskCategory riskCategory = riskCategoryRepository.findById(id).get();

        //check if deleting violates risk-weightage foreign key contraint
        if(riskWeightageRepository.existsByCategory(riskCategory)){
            throw new LoitServiceException(Translator.toLocale("FK_DELETE_PR"),
                    "INVALID_ATTEMPT");
        }else{
            //save to history before deleting record
            saveHistoryRecord(riskCategory);
            riskCategoryRepository.delete(riskCategory);
        }

        //save to history before deleting record
        saveHistoryRecord(riskCategory);

        riskCategoryRepository.delete(riskCategory);
        return projectionFactory.createProjection(LovRiskCategories.class, riskCategory);
    }


    /**
     * Save riskCategory history record
     *
     * @param riskCategory
     */
    public void saveHistoryRecord(RiskCategory riskCategory) {
        RiskCategoryHistory riskCategoryHistory = new RiskCategoryHistory();
        BeanUtils.copyProperties(riskCategory, riskCategoryHistory);
        riskCategoryHistory.setRiskCategoryId(riskCategory.getId());
        riskCategoryHistory.setId(0);
        riskCategoryHistoryRepository.save(riskCategoryHistory);
    }
}
