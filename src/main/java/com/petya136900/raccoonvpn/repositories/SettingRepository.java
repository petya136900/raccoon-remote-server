package com.petya136900.raccoonvpn.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.petya136900.raccoonvpn.entitys.Setting;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {
	List<Setting> findByName(String name);
}