package com.rentify.applicationService.repository;

import com.rentify.applicationService.model.RegistroArriendo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroArriendoRepository extends JpaRepository<RegistroArriendo, Long> {
    List<RegistroArriendo> findBySolicitudId(Long solicitudId);
}
