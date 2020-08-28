package com.personal.webflux.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.personal.webflux.app.model.Plato;
import com.personal.webflux.app.repo.IGenericRepo;
import com.personal.webflux.app.repo.IPlatoRepo;
import com.personal.webflux.app.service.IPlatoService;

@Service
public class PlatoServiceImpl extends CRUDImpl<Plato, String> implements IPlatoService {

    @Autowired
    private IPlatoRepo platoRepo;

    @Override
    protected IGenericRepo<Plato, String> getRepo() {
        return platoRepo;
    }
}
