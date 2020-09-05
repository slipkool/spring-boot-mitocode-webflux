package com.personal.webflux.app.repo;

import com.personal.webflux.app.model.Usuario;

import reactor.core.publisher.Mono;

public interface IUsuarioRepo extends IGenericRepo<Usuario, String> {

    Mono<Usuario> findOneByUsuario(String usuario);
}
