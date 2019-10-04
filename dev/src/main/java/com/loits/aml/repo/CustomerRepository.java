package com.loits.aml.repo;

import com.loits.aml.domain.Customer;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface CustomerRepository extends PagingAndSortingRepository<Customer, Integer>, QuerydslPredicateExecutor<Customer> {

    boolean existsByNic(String nic);

    boolean existsByOldNic(String nic);

//    boolean existsByNicAndModule(String customerNic, String nic);
//
//    boolean existsByOldNicAndModule(String customerNic, String nic);
}
