package com.personal.webflux.app.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.personal.webflux.app.model.Plato;
import com.personal.webflux.app.service.IPlatoService;

import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.net.URI;

@Component
public class PlatoHandler {

    @Autowired
    private IPlatoService service;

    public Mono<ServerResponse> listar(ServerRequest req) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.listar(), Plato.class);
    }

    public Mono<ServerResponse> listarPorId(ServerRequest req) {
        String id = req.pathVariable("id");
        return service.listarPorId(id)
                .flatMap(p -> ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(p)))
                .switchIfEmpty(ServerResponse
                                .notFound()
                                .build());
    }

    public Mono<ServerResponse> registrar(ServerRequest req) {
        Mono<Plato> monoPlato = req.bodyToMono(Plato.class);
        return monoPlato.flatMap(service::registrar)
                .flatMap(p -> ServerResponse.created(URI.create(req.uri().toString().concat("/").concat(p.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(p)));
    }

    public Mono<ServerResponse> modificar(ServerRequest req) {
        Mono<Plato> monoPlato = req.bodyToMono(Plato.class);
        Mono<Plato> monoBD = service.listarPorId(req.pathVariable("id"));

        return monoBD.zipWith(monoPlato, (bd, p) -> {
                    bd.setEstado(p.getEstado());
                    bd.setNombre(p.getNombre());
                    return bd;
                })
                .flatMap(service::modificar)
                .flatMap(p -> ServerResponse.created(URI.create(req.uri().toString().concat("/").concat(p.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> eliminar(ServerRequest req) {
        String id = req.pathVariable("id");
        return service.listarPorId(id)
                .flatMap(p -> service.eliminar(p.getId())
                                .then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse
                        .notFound()
                        .build());
    }
}
