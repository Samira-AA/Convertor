package com.sumax.pdfa.service;

import com.sumax.pdfa.dto.ClientDto;
import java.util.List;

public interface ClientService {
    ClientDto create(ClientDto request);

    List<ClientDto> list();

    ClientDto consumption(long id);

    ClientDto rotateKey(long id);

    ClientDto setStatus(long id, ClientDto request);
}
