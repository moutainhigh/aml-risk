package com.loits.aml.services.projections;

import com.loits.aml.domain.Transaction;
import org.springframework.data.rest.core.config.Projection;

import java.sql.Timestamp;

@Projection(types = {Transaction.class}, name = "lovTransactions")
public interface LovTransactions {

    public Integer getCustomer();

    public Integer getTxnId();

    public Timestamp getTxnDate();

    public Integer getProduct();

    public String getTxnType();

    public String getTxnReference();

    public String getRemarks();

    public String getCreatedBy();

    public Timestamp getCreatedOn();

    public String getModule();

    public Long getAmount();

}
