/*
 * Copyright (c) 2018. LOLC Technology Services Ltd.
 * Author: Ranjith Kodikara
 * Date: 12/12/18 10:45
 */

package com.loits.aml.core;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * 
 * Inherit this class when you define entities... common_seq is a common
 * sequence and the numbers will be shared with different entities ensuring the
 * uniqueness <br>
 * Have id and version columns
 * 
 * @author ranjithk
 * @since 2018-12-20
 * @version 1.0
 * 
 */
@MappedSuperclass
public abstract class BaseEntity {

	protected Long id;

	protected BaseEntity() {
		id = null;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator="native")
	@GenericGenerator(name = "native", strategy = "native")
	@Column(name = "id", nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
