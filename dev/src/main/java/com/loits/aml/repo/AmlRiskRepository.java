package com.loits.aml.repo;

import com.loits.aml.domain.AmlRisk;
import com.loits.aml.repo.custom.AmlRiskRepositoryCustom;
import com.querydsl.core.types.Predicate;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */

@RepositoryRestResource(exported = false)
public interface AmlRiskRepository extends PagingAndSortingRepository<AmlRisk, Long>, QuerydslPredicateExecutor<AmlRisk>,
        AmlRiskRepositoryCustom {

    boolean existsByCustomer(Long id);

    Optional<AmlRisk> findTopByCustomerOrderByCreatedOnDesc(Long id);

}
