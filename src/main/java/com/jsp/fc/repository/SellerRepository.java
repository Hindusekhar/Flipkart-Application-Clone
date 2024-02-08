package com.jsp.fc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jsp.fc.entity.Seller;
@Repository
public interface SellerRepository extends JpaRepository<Seller, Integer> {

}
