package com.jsp.fc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jsp.fc.entity.Customer;

@Repository
public interface customerRepository extends JpaRepository<Customer, Integer>{

}
