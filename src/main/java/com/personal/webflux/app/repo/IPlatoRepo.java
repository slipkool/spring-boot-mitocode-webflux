package com.personal.webflux.app.repo;

import com.personal.webflux.app.model.Plato;

public interface IPlatoRepo extends IGenericRepo<Plato, String> {

    /*@Query("find().skip(:page).limit(:size)")
    Flux<Plato> getPagina(@Param("page") int page, @Param("size") int size);*/
}
