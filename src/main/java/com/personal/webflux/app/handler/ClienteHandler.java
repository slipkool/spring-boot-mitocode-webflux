package com.personal.webflux.app.handler;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.personal.webflux.app.model.Cliente;
import com.personal.webflux.app.service.IClienteService;
import com.personal.webflux.app.validator.RequestValidator;

import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ClienteHandler {

    @Autowired
    private IClienteService service;

    @Autowired
    private RequestValidator validadorGeneral;

    public Mono<ServerResponse> listar(ServerRequest req) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.listar(), Cliente.class);
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
        Mono<Cliente> monoCliente = req.bodyToMono(Cliente.class);
        return monoCliente
                .flatMap(validadorGeneral::validate)
                .flatMap(service::registrar)
                .flatMap(p -> ServerResponse.created(URI.create(req.uri().toString().concat("/").concat(p.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(p)));
    }

    public Mono<ServerResponse> modificar(ServerRequest req) {
        Mono<Cliente> monoCliente = req.bodyToMono(Cliente.class);
        Mono<Cliente> monoBD = service.listarPorId(req.pathVariable("id"));

        return monoBD.zipWith(monoCliente, (bd, c) -> {
                    bd.setId(c.getId());
                    bd.setNombres(c.getNombres());
                    bd.setApellidos(c.getApellidos());
                    bd.setFechaNac(c.getFechaNac());
                    return bd;
                })
                .flatMap(validadorGeneral::validate)
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
