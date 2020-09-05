package com.personal.webflux.app.service.impl;

import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;

import com.personal.webflux.app.pagination.PageSupport;
import com.personal.webflux.app.repo.IGenericRepo;
import com.personal.webflux.app.service.ICRUD;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class CRUDImpl<T, ID> implements ICRUD<T, ID> {

    protected abstract IGenericRepo<T, ID> getRepo();

    @Override
    public Mono<T> registrar(T t) {
        return getRepo().save(t);
    }

    @Override
    public Mono<T> modificar(T t) {
        return getRepo().save(t);
    }

    @Override
    public Flux<T> listar() {
        return getRepo().findAll();
    }

    @Override
    public Mono<T> listarPorId(ID id) {
        return getRepo().findById(id);
    }

    @Override
    public Mono<Void> eliminar(ID id) {
        return getRepo().deleteById(id);
    }

    @Override
    public Mono<PageSupport<T>> listarPage(Pageable page) {
        //directamente en la bd para no hacer un full a la bd
        //db.platos.find().skip(5).limit(5) //mongo

        //hace un full a la bd pra poder paginar
        return getRepo().findAll()
                .collectList()
                .map(list -> new PageSupport<>(
                        list
                        .stream()
                        .skip(page.getPageNumber() * page.getPageSize())
                        .limit(page.getPageSize())
                        .collect(Collectors.toList()),
                    page.getPageNumber(), page.getPageSize(), list.size()
                    ));
    }
}
