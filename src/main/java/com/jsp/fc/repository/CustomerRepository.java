package com.jsp.fc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

}
