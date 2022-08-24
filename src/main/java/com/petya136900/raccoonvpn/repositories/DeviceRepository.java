package com.petya136900.raccoonvpn.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.petya136900.raccoonvpn.entitys.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
	@Query("SELECT d FROM Device d WHERE d.user.id = ?1 and d.deviceId = ?2")
	List<Device> findByUserIdAndDeviceId(Long user_id, String deviceId);
	@Query("SELECT d FROM Device d WHERE d.user.id = ?1 and d.name = ?2")
	List<Device> findByUserIdAndName(Long userId, String name);
	@Query("SELECT d FROM Device d WHERE d.user.id = ?1 AND d.local = false")
	List<Device> findByUserId(Long userId);
	@Query("SELECT d FROM Device d WHERE d.local = true")
	List<Device> findAllLocals();
}