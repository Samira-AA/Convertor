package com.sumax.pdfa.dao;

import com.sumax.pdfa.dto.ConversionesRestantesDto;
import com.sumax.pdfa.entity.Clientes;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.List;

public interface ClientDao extends JpaRepository<Clientes, Long> {
    List<Clientes> findAllByOrderByIdAsc();

    Optional<Clientes> findByApiKeyHash(String hash);

    Optional<ConversionesRestantesDto> findConversionesRestantesById(long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Clientes> findLockedById(long id);

}
