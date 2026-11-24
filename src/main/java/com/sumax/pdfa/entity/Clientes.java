package com.sumax.pdfa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.io.Serial;
import java.io.Serializable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Cliente del Converter; una cuota nula representa consumo sin límite. */
@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clientes")
public class Clientes implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "codigo", length = 64, nullable = false, unique = true, updatable = false)
    private String codigo;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    /** Hash usado para autenticar X-API-Key; nunca se devuelve al consumidor. */
    @ToString.Exclude
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "api_key_hash", length = 64, unique = true)
    private String apiKeyHash;

    @Column(name = "estado", nullable = false)
    private boolean estado;

    /** Saldo descontado después de convertir y validar. Null indica cuota ilimitada. */
    @Column(name = "conversiones_restantes")
    private Integer conversionesRestantes;
}
