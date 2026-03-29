package com.example.demoapp.repository;

import com.example.demoapp.entity.User;
import com.example.demoapp.entity.UserMembership;
import com.example.demoapp.entity.UserMembershipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {

    Optional<UserMembership> findByUserAndStatus(User user, UserMembershipStatus status);

    List<UserMembership> findByUserOrderByCreatedAtDesc(User user);

    Page<UserMembership> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
