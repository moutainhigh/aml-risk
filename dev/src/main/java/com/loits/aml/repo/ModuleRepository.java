package com.loits.aml.repo;

import com.loits.aml.domain.Module;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface ModuleRepository extends PagingAndSortingRepository<Module, String>, QuerydslPredicateExecutor<Module> {

}
