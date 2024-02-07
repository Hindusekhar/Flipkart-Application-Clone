package com.jsp.fc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.User;

public interface UserRepository extends JpaRepository<User, Integer>{
	boolean existsByUserEmail(String userEmail);

}
