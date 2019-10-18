package com.loits.aml.repo;

import com.loits.aml.domain.KafkaErrorLog;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface KafkaErrorLogRepository extends PagingAndSortingRepository<KafkaErrorLog, Long>, QuerydslPredicateExecutor<KafkaErrorLog> {

}
