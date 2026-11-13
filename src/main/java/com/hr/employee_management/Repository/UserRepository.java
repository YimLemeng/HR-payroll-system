package com.hr.employee_management.Repository;

import com.hr.employee_management.Entity.User;
import com.hr.employee_management.enums.Role;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("select u from User u where u.username = :input OR u.email = :input")
    Optional<User> findByUsernameOrEmail(@Param("input") String input);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
    long countByRoleAndActiveTrue(Role role);

    List<User> id(Long id);
}
