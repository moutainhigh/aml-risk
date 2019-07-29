package com.loits.aml.repo;

import com.loits.aml.domain.RiskWeightageHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface RiskWeightageHistoryRepository extends CrudRepository<RiskWeightageHistory, Integer> {

}
