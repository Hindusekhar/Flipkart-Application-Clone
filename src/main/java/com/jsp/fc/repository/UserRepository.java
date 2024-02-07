package com.jsp.fc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jsp.fc.entity.User;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

	boolean existsByUserEmail(String userEmail);
	Optional<User> findByUserName(String userName);
	
	//List<User> findByEmailVerified(boolean emailVerified);
}
