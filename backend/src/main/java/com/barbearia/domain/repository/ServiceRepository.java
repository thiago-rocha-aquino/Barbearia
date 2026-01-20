package com.barbearia.domain.repository;

import com.barbearia.domain.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

    List<Service> findByActiveTrueOrderByDisplayOrderAsc();

    boolean existsByNameAndIdNot(String name, UUID id);

    boolean existsByName(String name);
}
