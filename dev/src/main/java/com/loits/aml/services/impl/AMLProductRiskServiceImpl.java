package com.loits.aml.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loits.aml.core.FXDefaultException;
import com.loits.aml.dto.CustomerProduct;
import com.loits.aml.dto.Transaction;
import com.loits.aml.services.AMLProductRiskService;
import com.loits.aml.services.HTTPService;
import com.loits.aml.services.KieService;
import com.loits.fx.aml.Module;
import com.loits.fx.aml.Product;
import com.loits.fx.aml.ProductRates;
import com.loits.fx.aml.ProductRisk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class AMLProductRiskServiceImpl implements AMLProductRiskService {

  Logger logger = LogManager.getLogger(AMLProductRiskServiceImpl.class);

  @Autowired
  KieService kieService;

  @Autowired
  Environment env;

  @Autowired
  HTTPService httpService;

  @Value("${global.date.format}")
  private String dateFormat;

  @Value("${aml.transaction.default.back-months}")
  private String DEFAULT_BACK_MONTHS_TRANSACTION;

  SimpleDateFormat sdf;

  @PostConstruct
  public void init() {
    this.sdf = new SimpleDateFormat(dateFormat);
  }

  @Override
  public ProductRisk calculateProductRisk(Long customerId, Module ruleModule, String user,
                                          String tenent) throws
          FXDefaultException {
    logger.debug("Product Risk calculation started...");
    List<CustomerProduct> customerProductList = null;
    ObjectMapper objectMapper = new ObjectMapper();
    ProductRisk productRisk = new ProductRisk();        //Get the products for customer
    //Request parameters to AML Service
    String amlServiceProductsUrl = String.format(env.getProperty("aml.api.aml-customer-products")
            , tenent);
    HashMap<String, String> productsParameters = new HashMap<>();
    productsParameters.put("customer.id", customerId.toString());
    try {
      customerProductList = httpService.getDataFromList("AML", amlServiceProductsUrl,
              productsParameters, new TypeReference<List<CustomerProduct>>() {
              });
    } catch (Exception e) {
      e.printStackTrace();
      throw new FXDefaultException("-1", "ERROR", "Exception occured in retrieving Customer " +
              "Products", new Date(), HttpStatus.BAD_REQUEST, false);
    }
    if (customerProductList.size() > 0) {
      logger.debug("Customer Products available. Starting calculation...");
      productRisk.setCustomerCode(customerId);
      productRisk.setModule(ruleModule);
      productRisk.setToday(new Date());
      List<Product> productList = new ArrayList<>();
      for (CustomerProduct cp : customerProductList) {
        Product product = new Product();
        product.setId(cp.getId());
        product.setCommencedDate(cp.getCommenceDate());
        product.setTerminatedDate(cp.getTerminateDate());
        product.setInterestRate(cp.getRate());
        product.setValue(cp.getValue());
        product.setCpmeta1(cp.getMeta1());
        product.setCpmeta2(cp.getMeta2());
        if (cp.getProduct() != null) {
          product.setCode(cp.getProduct().getCode());
          product.setDefaultRate(cp.getProduct().getDefaultRate());
          product.setPmeta1(cp.getProduct().getMeta1());
          product.setPmeta2(cp.getProduct().getMeta2());
          if (cp.getPeriod() != null) {
            product.setPeriod(cp.getPeriod().doubleValue());
          }
          List<ProductRates> productRatesList = new ArrayList<>();
          if (cp.getProduct().getRates() != null) {
            for (com.loits.aml.dto.ProductRates amlProductRate : cp.getProduct().getRates()) {
              ProductRates productRate = new ProductRates();
              if (amlProductRate.getRate() != null) {
                productRate.setRate(amlProductRate.getRate().doubleValue());
              }
              if (amlProductRate.getAccumulatedRate() != null) {
                productRate.setAccumulatedRate(amlProductRate.getAccumulatedRate().doubleValue());
              }
              if (amlProductRate.getCompanyRatio() != null) {
                productRate.setCompanyRatio(amlProductRate.getCompanyRatio().doubleValue());
              }
              if (amlProductRate.getInvestorRatio() != null) {
                productRate.setInvestorRatio(amlProductRate.getInvestorRatio().doubleValue());
              }
              if (amlProductRate.getProfitRate() != null) {
                productRate.setProfitRate(amlProductRate.getProfitRate().doubleValue());
              }
              if (amlProductRate.getProfitFeeRate() != null) {
                productRate.setProfitFeeRate(amlProductRate.getProfitFeeRate().doubleValue());
              }
              if (amlProductRate.getPeriod() != null) {
                productRate.setPeriod(amlProductRate.getPeriod().doubleValue());
              }
              productRate.setPayMode(amlProductRate.getPayMode());
              productRate.setStatus(amlProductRate.getStatus());
              productRate.setDate(amlProductRate.getDate());
              if (amlProductRate.getFromAmt() != null) {
                productRate.setFromAmt(amlProductRate.getFromAmt().doubleValue());
              }
              if (amlProductRate.getToAmt() != null) {
                productRate.setToAmt(amlProductRate.getToAmt().doubleValue());
              }
              productRatesList.add(productRate);
            }
          }
          product.setRates(productRatesList);
        }
        List<com.loits.fx.aml.Transaction> ruleTransactionsList = new ArrayList<>();
        for (Transaction tr : cp.getTransactions()) {
          com.loits.fx.aml.Transaction transaction = new com.loits.fx.aml.Transaction();
          transaction.setType(tr.getTxnMode());
          if (tr.getTxnAmount() != null) {
            transaction.setAmount(tr.getTxnAmount().doubleValue());
          }
          transaction.setDate(tr.getTxnDate());
          ruleTransactionsList.add(transaction);
        }
        product.setModule(cp.getProduct().getModule().getCode());
        product.setTransactions(ruleTransactionsList);
        productList.add(product);
      }
      productRisk.setProducts(productList);//            //TODO add to HTTPSERVice
//            HttpResponse httpResponse = sendPostRequest(productRisk, String.format(env
// .getProperty("aml.api.product-risk"), tenent), "ProductRisk", null);
//            try {
//                String jsonString = EntityUtils.toString(httpResponse.getEntity());
//                productRisk = objectMapper.readValue(jsonString, ProductRisk.class);
//            } catch (IOException e) {
//                logger.debug("Error deserializing productRisk object");
//                e.printStackTrace();
//            }            //Headers for CategoryRisk POST Req
      HashMap<String, String> headers = new HashMap<>();
      headers.put("user", user);            //Calculate product risk by sending request to
      // Product Risk Service
      try {
        productRisk = (ProductRisk) httpService.sendData("Product-risk",
                String.format(env.getProperty("aml.api.product-risk"), tenent),
                null, headers, ProductRisk.class, productRisk);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      logger.debug("No CustomerProducts available to calculate Product Risk. Aborting...");
      productRisk.setCalculatedRisk(0.0);
    }
    logger.debug("ProductRisk obj " + productRisk);
    return productRisk;
  }
}
