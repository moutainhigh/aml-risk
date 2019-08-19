package com.loits.aml.repo;

import com.loits.aml.domain.Product;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface ProductRepository extends PagingAndSortingRepository<Product, Integer>, QuerydslPredicateExecutor<Product> {

}
