package com.personal.webflux.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.personal.webflux.app.model.Factura;
import com.personal.webflux.app.repo.IFacturaRepo;
import com.personal.webflux.app.repo.IGenericRepo;
import com.personal.webflux.app.service.IFacturaService;

@Service
public class FacturaServiceImpl extends CRUDImpl<Factura, String> implements IFacturaService {

    @Autowired
    private IFacturaRepo FacturaRepo;

    @Override
    protected IGenericRepo<Factura, String> getRepo() {
        return FacturaRepo;
    }
}
