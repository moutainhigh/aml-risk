package com.loits.aml.repo;

import com.loits.aml.domain.RiskWeightage;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */

@RepositoryRestResource(exported = false)
public interface RiskWeightageRepository extends PagingAndSortingRepository<RiskWeightage, Integer>, QuerydslPredicateExecutor<RiskWeightage>{

}
