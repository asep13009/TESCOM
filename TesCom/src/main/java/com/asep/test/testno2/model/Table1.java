package com.asep.test.testno2.model;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class Table1 {
	@Id
	@Column(length = 20)
	private Integer id;
	@Column(length = 50)
	private String name;
	@Column(length = 30)
	private String phone;
}
