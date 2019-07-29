package com.loits.aml.repo;

import com.loits.aml.domain.RiskCategory;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */

@RepositoryRestResource(exported = false)
public interface RiskCategoryRepository extends PagingAndSortingRepository<RiskCategory, Integer>, QuerydslPredicateExecutor<RiskCategory>{

}
