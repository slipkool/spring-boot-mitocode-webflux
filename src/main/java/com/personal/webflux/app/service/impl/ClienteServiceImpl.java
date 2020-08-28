package com.personal.webflux.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.personal.webflux.app.model.Cliente;
import com.personal.webflux.app.repo.IClienteRepo;
import com.personal.webflux.app.repo.IGenericRepo;
import com.personal.webflux.app.service.IClienteService;

@Service
public class ClienteServiceImpl extends CRUDImpl<Cliente, String> implements IClienteService {

    @Autowired
    private IClienteRepo ClienteRepo;

    @Override
    protected IGenericRepo<Cliente, String> getRepo() {
        return ClienteRepo;
    }
}
