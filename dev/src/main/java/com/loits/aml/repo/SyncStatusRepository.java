package com.loits.aml.repo;

import com.loits.aml.domain.SyncStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface SyncStatusRepository extends PagingAndSortingRepository<SyncStatus, Integer>,
        QuerydslPredicateExecutor<SyncStatus> {

  SyncStatus findFirstByType(String type, Sort sort);

//  SyncStatus findFirstByTypeAndModuleAndCronStatus(String type, String module, String
// syncCompleted, Sort sort);

}
