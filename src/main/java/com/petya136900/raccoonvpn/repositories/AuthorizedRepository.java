package com.petya136900.raccoonvpn.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.petya136900.raccoonvpn.entitys.Authorized;

@Repository
public interface AuthorizedRepository extends JpaRepository<Authorized, Long> {
	@Query("SELECT a FROM Authorized a WHERE a.user.id = ?1")
	List<Authorized> findByUserId(Long userId);
	@Query("SELECT a FROM Authorized a WHERE a.token = ?1")
	List<Authorized> findByToken(String token);
	@Query("SELECT a FROM Authorized a WHERE a.user.id = ?1 AND a.ip = ?2 AND a.agent = true")
	List<Authorized>  getByUserIdAndIpAgent(Long userId, String ip);
	@Query("SELECT a FROM Authorized a WHERE a.token = ?1")
	List<Authorized>  getByToken(String token);
	@Query("SELECT a FROM Authorized a WHERE a.ip = ?1")
	List<Authorized> getByIp(String ip);
	@Query("SELECT a FROM Authorized a WHERE a.ip = ?1 AND a.user.id = ?2")
	List<Authorized> getByIpAndUserId(String ip, Long userId);
}