package com.example.demoapp.repository;

import com.example.demoapp.entity.Role;
import com.example.demoapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u JOIN u.serviceCategories c WHERE u.role = :role AND c.id = :categoryId")
    Page<User> findByRoleAndServiceCategoriesId(@Param("role") Role role, @Param("categoryId") Long categoryId, Pageable pageable);
}
