package com.loits.aml.repo;

import com.loits.aml.domain.RiskCategoryHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface RiskCategoryHistoryRepository extends CrudRepository<RiskCategoryHistory, Integer> {

}
