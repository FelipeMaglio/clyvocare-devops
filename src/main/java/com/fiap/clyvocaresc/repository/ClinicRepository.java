package com.fiap.clyvocaresc.repository;

import com.fiap.clyvocaresc.entity.CatalogItem;
import com.fiap.clyvocaresc.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {
}