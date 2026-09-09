package com.fiap.clyvocaresc.repository;

import com.fiap.clyvocaresc.entity.CatalogItem;
import com.fiap.clyvocaresc.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface OwnerRepository extends JpaRepository<Owner, Long> {

    Optional<Owner> findByUserUsername(String username);
    Optional<Owner> findByDocument(String document);
}