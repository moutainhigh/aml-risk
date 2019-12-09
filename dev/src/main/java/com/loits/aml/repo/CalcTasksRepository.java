package com.loits.aml.repo;

import com.loits.aml.domain.CalcStatus;
import com.loits.aml.domain.CalcTasks;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface CalcTasksRepository extends PagingAndSortingRepository<CalcTasks, Long>,
        QuerydslPredicateExecutor<CalcTasks> {

}
