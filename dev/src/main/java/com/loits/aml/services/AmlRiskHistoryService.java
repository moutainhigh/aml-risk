package com.loits.aml.services;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */

public interface AmlRiskHistoryService {

    Page<?> getAll(Pageable pageable, Predicate predicate, String bookmarks, String projection);

}
