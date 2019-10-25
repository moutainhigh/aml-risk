package com.loits.aml.repo;

import com.loits.aml.domain.ServiceMetadata;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface ServiceMetadataRepository extends PagingAndSortingRepository<ServiceMetadata, String>, QuerydslPredicateExecutor<ServiceMetadata> {

  ServiceMetadata findOneByMetaKey(String metaKey);
}
