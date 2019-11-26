package com.loits.aml.repo.custom;

import com.loits.aml.domain.AmlRisk;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface AmlRiskRepositoryCustom {

    public final static String GET_CUSTOMER_RISK= "SELECT * FROM aml_risk " +
            "where customer = :id AND created_on>= :from " +
            "AND created_on<= :to " +
            "ORDER BY created_on desc\n" +
            "LIMIT 1;";

    public final static String GET_CUSTOMER_RISKS= "SELECT x.* FROM aml_risk x " +
            "INNER JOIN (SELECT customer, MAX(created_on) as co FROM aml_risk as x " +
            "where created_on >= :from" +
            " AND created_on<= :to"+"" +
            //"AND module= :module" +
            " GROUP BY customer) y ON x.customer = y.customer AND x.created_on = y.co";

    @Query(value =GET_CUSTOMER_RISKS, nativeQuery = true)
    List<AmlRisk> findTopForEachCustomerBetween(@Param("from") Timestamp from, @Param("to")Timestamp to);


    @Query(value =GET_CUSTOMER_RISK, nativeQuery = true)
    List<AmlRisk> findTopForCustomerOrderByCreatedOnDescBetween(@Param("id")Long customerId, @Param("from") Timestamp from, @Param("to")Timestamp to);

}
