package com.petya136900.raccoonvpn.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.petya136900.raccoonvpn.entitys.Condition;


@Repository
public interface ConditionRepository extends JpaRepository<Condition, Long>{
	@Query("SELECT c FROM Condition c WHERE c.device.id = ?1")
	List<Condition> findByDevId(Long devId);
}