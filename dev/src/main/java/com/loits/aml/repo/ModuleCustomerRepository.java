package com.loits.aml.repo;

import com.loits.aml.domain.Customer;
import com.loits.aml.domain.Module;
import com.loits.aml.domain.ModuleCustomer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Date;
import java.util.List;

@RepositoryRestResource(exported = false)
public interface ModuleCustomerRepository extends PagingAndSortingRepository<ModuleCustomer,
        Long>, QuerydslPredicateExecutor<ModuleCustomer> {

  ModuleCustomer findOneByModuleAndModuleCustomerCodeAndRiskCalculatedOnBetween(Module module,
                                                                                String custCode,
                                                                                Date from, Date to);

  boolean existsByModuleAndModuleCustomerCodeAndRiskCalculatedOnBetween(Module moduleObj,
                                                                        String customerCode,
                                                                        Date from, Date to);

  boolean existsByModuleAndModuleCustomerCodeAndRiskCalculatedOnIsNull(Module moduleObj,
                                                                       String customerCode);

  @Query("select m from ModuleCustomer m join fetch m.customer where m.id = :id")
  ModuleCustomer getModuleCustomerWithCustoemrPopulated(@Param("id") Long moduleId);

  boolean existsByModuleAndModuleCustomerCode(Module module, String custCode);

  boolean existsByModule(Module moduleObj);

  List<ModuleCustomer> findAllByModuleAndRiskCalculatedOnBetween(Module module, Date from,
                                                                 Date to, Pageable pageable);

  List<ModuleCustomer> findAllByCustomer(Customer customer);

  @Query("SELECT COUNT(c) FROM ModuleCustomer c WHERE c.module=?1 AND c.riskCalculatedOn BETWEEN " +
          "?2 AND ?3")
  int findCountByModuleAndRiskCalculatedOnBetween(Module module, Date from, Date to);

  ModuleCustomer findByModuleAndModuleCustomerCodeAndRiskCalculatedOnIsNull(Module moduleObj,
                                                                            String customerCode);
}
