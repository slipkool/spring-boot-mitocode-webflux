package com.personal.webflux.app.service;

import com.personal.webflux.app.model.Usuario;
import com.personal.webflux.app.security.User;

import reactor.core.publisher.Mono;

public interface IUsuarioService extends ICRUD<Usuario, String> {

    Mono<User> buscarPorUsuario(String usuario);
}
