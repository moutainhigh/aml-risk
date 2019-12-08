package com.loits.aml.repo;

import com.loits.aml.domain.CalcStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface CalcStatusRepository extends PagingAndSortingRepository<CalcStatus, Long>,
        QuerydslPredicateExecutor<CalcStatus> {

  CalcStatus findFirstByType(String type, Sort sort);

}
