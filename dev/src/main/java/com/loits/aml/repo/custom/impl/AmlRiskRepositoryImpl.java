package com.loits.aml.repo.custom.impl;
//
//import com.loits.aml.domain.AmlRisk;
//import com.loits.aml.domain.QAmlRisk;
//import com.loits.aml.repo.custom.AmlRiskRepositoryCustom;
//import com.querydsl.core.BooleanBuilder;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.codehaus.plexus.logging.LoggerManager;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import javax.persistence.EntityManager;
//import java.sql.Timestamp;
//import java.util.List;
//
//public class AmlRiskRepositoryImpl implements AmlRiskRepositoryCustom {
//
////    Logger logger = LogManager.getLogger(AmlRiskRepositoryImpl.class);
////
//    @Autowired
//    EntityManager em;
//
//    @Override
//    public List<AmlRisk> findTopForEachCustomerBetween(Long customerId, Timestamp from, Timestamp to) {
//        logger.debug(String.format("AmlRisk Between dates query : Received Params - " +
//                "customerId : %s "+
//                "From : %s , To : %s",customerId, from, to));
//
//        QAmlRisk amlRisk = QAmlRisk.amlRisk;
//        BooleanBuilder bb = new BooleanBuilder();
//        JPAQueryFactory qf = new JPAQueryFactory(em);
//
//        if(customerId!=null){
//            amlRisk.customer.eq(customerId)
//                    .and(amlRisk.createdOn.goe(from))
//                    .and(amlRisk.createdOn.loe(to));
//        }
//
//        List<AmlRisk> amlRisks = qf.select(amlRisk).from(QAmlRisk.amlRisk).where(bb).orderBy().limit(1).fetch();
//
//        if (!amlRisks.isEmpty())
//            logger.debug("Loaded AmlRisks size : " + amlRisks.size());
//
//        return amlRisks;
//    }
//}
