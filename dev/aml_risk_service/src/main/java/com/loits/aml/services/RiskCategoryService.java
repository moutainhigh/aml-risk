package com.loits.aml.services;

import com.loits.aml.config.LoitServiceException;
import com.loits.aml.domain.RiskCategory;
import com.loits.aml.services.model.NewRiskCategory;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */

public interface RiskCategoryService {

    Page<?> getAll(Pageable pageable, Predicate predicate, String bookmarks, String projection);

    Object create(String projection, RiskCategory riskCategory, String user, Timestamp timestamp, String company, String module) throws LoitServiceException;

    Object update(String projection, NewRiskCategory newRiskCategory, String user, Timestamp timestamp) throws LoitServiceException;

    Object delete(String projection, Integer id) throws LoitServiceException;
}
