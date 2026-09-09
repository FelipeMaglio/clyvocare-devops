package com.fiap.clyvocaresc.repository;

import com.fiap.clyvocaresc.entity.City;
import com.fiap.clyvocaresc.entity.Species;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeciesRepository extends JpaRepository<Species, Long> {
}