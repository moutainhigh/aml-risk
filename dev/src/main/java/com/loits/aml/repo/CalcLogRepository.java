package com.loits.aml.repo;

import com.loits.aml.domain.CalcLog;
import com.loits.aml.domain.CalcTasks;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface CalcLogRepository extends PagingAndSortingRepository<CalcLog, Long>,
        QuerydslPredicateExecutor<CalcLog> {
}
