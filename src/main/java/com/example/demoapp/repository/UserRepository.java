package com.example.demoapp.repository;

import com.example.demoapp.entity.Role;
import com.example.demoapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    long countByRole(Role role);

    @Query("SELECT u FROM User u WHERE " +
            "(COALESCE(:search, '') = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:blocked IS NULL OR u.blocked = :blocked)")
    Page<User> adminSearch(
            @Param("search") String search,
            @Param("role") Role role,
            @Param("blocked") Boolean blocked,
            Pageable pageable);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByRoleAndBlocked(Role role, boolean blocked, Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u JOIN u.serviceCategories c WHERE u.role = :role AND c.id = :categoryId")
    Page<User> findByRoleAndServiceCategoriesId(@Param("role") Role role, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u JOIN u.serviceCategories c WHERE u.role = :role AND u.blocked = false AND c.id = :categoryId")
    Page<User> findActiveMahirsByCategory(@Param("role") Role role, @Param("categoryId") Long categoryId, Pageable pageable);

    Page<User> findByRoleAndBlockedFalse(Role role, Pageable pageable);

    Page<User> findByBlockedTrue(Pageable pageable);

    Page<User> findByBlockedFalse(Pageable pageable);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime endExclusive);
}
