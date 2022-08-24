package com.petya136900.raccoonvpn.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.petya136900.raccoonvpn.entitys.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	List<User> findByLogin(String login);
	List<User> findByMail(String mail);
	//@Query("SELECT u FROM User u WHERE u.login = ?1")
	//List<User> findByLoginQ(String login);
	@Query("SELECT u FROM User u WHERE u.admin = true")
	List<User> findAllAdmins();
}