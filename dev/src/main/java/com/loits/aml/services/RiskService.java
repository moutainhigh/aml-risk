package com.loits.aml.services;

import com.redhat.aml.Customer;
import com.redhat.aml.OverallRisk;

import java.sql.Timestamp;

public interface RiskService {
    Object calcRisk(String projection, Customer customer, String user, Timestamp timestamp);
}
