package com.lab.passwordmanager.repository;

import com.lab.passwordmanager.model.Password;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordRepository extends JpaRepository<Password, Long> {

    Optional<Password> findByService(String service);

    List<Password> findByUsernameContainingIgnoreCase(String username);

    boolean existsByServiceAndUsername(String service, String username);

    @Query("SELECT DISTINCT p FROM Password p JOIN p.tags t WHERE t.name = :tagName")
    List<Password> findByTagName(@Param("tagName") String tagName);
}
