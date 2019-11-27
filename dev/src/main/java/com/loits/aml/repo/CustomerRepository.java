package com.loits.aml.repo;

import com.loits.aml.domain.Customer;
import com.loits.aml.domain.QCustomer;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.domain.Page;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */

@RepositoryRestResource(exported = false)
public interface CustomerRepository extends PagingAndSortingRepository<Customer, Long>,
        QuerydslPredicateExecutor<Customer>, QuerydslBinderCustomizer<QCustomer> {

  @Override
  default public void customize(QuerydslBindings bindings, QCustomer root) {

    bindings.bind(String.class).first((StringPath path, String value) -> path.containsIgnoreCase(value));
  }
}
