package com.sumax.pdfa.dao;

import com.sumax.pdfa.entity.ApiClient;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface ClientDao extends JpaRepository<ApiClient, Long> {
    List<ApiClient> findAllByOrderByIdAsc();

    Optional<ApiClient> findByApiKeyHash(String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT client FROM ApiClient client WHERE client.id = :id")
    Optional<ApiClient> lockClient(@Param("id") long id);

}
