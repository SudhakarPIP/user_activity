package com.useractivity.repository;

import com.useractivity.entity.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    @Query("SELECT ua FROM UserActivity ua WHERE ua.userId = :userId AND ua.isDeleted = false ORDER BY ua.createdAt DESC")
    Page<UserActivity> findByUserIdAndNotDeletedOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    boolean existsByIdAndIsDeletedFalse(Long id);

}
