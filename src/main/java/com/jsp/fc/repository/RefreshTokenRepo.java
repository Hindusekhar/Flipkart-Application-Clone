package com.jsp.fc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.RefereshToken;

public interface RefreshTokenRepo extends JpaRepository<RefereshToken, Long>{

}
