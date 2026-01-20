package com.barbearia.domain.repository;

import com.barbearia.domain.entity.User;
import com.barbearia.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleAndActiveTrue(UserRole role);

    @Query("SELECT u FROM User u WHERE u.active = true AND (u.role = 'ADMIN' OR u.role = 'BARBER')")
    List<User> findAllActiveBarbers();

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.workingHours WHERE u.id = :id")
    Optional<User> findByIdWithWorkingHours(UUID id);
}
