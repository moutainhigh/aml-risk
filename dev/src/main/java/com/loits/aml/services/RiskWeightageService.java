package com.loits.aml.services;

import com.loits.aml.config.LoitServiceException;
import com.loits.aml.domain.RiskWeightage;
import com.loits.aml.services.model.NewRiskWeightage;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */

public interface RiskWeightageService {

    Page<?> getAll(Pageable pageable, Predicate predicate, String bookmarks, String projection);

    Object create(String projection, RiskWeightage riskWeightage, String user, Timestamp timestamp, String company, String module) throws LoitServiceException;

    Object update(String projection, NewRiskWeightage newRiskWeightage, String user, Timestamp timestamp) throws LoitServiceException;

    Object delete(String projection, Integer id) throws LoitServiceException;
}
