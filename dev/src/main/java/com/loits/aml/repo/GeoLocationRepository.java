package com.loits.aml.repo;

import com.loits.aml.domain.AmlRisk;
import com.loits.aml.domain.GeoLocation;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */

@RepositoryRestResource(exported = false)
public interface GeoLocationRepository extends PagingAndSortingRepository<GeoLocation, Long>, QuerydslPredicateExecutor<GeoLocation> {
    Optional<GeoLocation> findByLocationName(String district);
}
