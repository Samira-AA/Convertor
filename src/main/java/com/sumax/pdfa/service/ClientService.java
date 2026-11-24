package com.sumax.pdfa.service;

import com.sumax.pdfa.dto.ClientesDto;
import com.sumax.pdfa.dto.ConversionesRestantesDto;
import java.util.List;

public interface ClientService {
    ClientesDto create(ClientesDto request);

    List<ClientesDto> list();

    ClientesDto findById(long id);

    ConversionesRestantesDto findConversionesRestantes(long id);

    ClientesDto update(long id, ClientesDto request);

    ClientesDto rotateKey(long id);

}
