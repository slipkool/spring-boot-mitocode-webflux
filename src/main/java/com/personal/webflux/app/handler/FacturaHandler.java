package com.personal.webflux.app.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.personal.webflux.app.model.Factura;
import com.personal.webflux.app.service.IFacturaService;
import com.personal.webflux.app.validator.RequestValidator;

import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.net.URI;

@Component
public class FacturaHandler {

    @Autowired
    private IFacturaService service;

    @Autowired
    private RequestValidator validadorGeneral;

    public Mono<ServerResponse> listar(ServerRequest req) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.listar(), Factura.class);
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
        Mono<Factura> monoFactura = req.bodyToMono(Factura.class);
        return monoFactura
                .flatMap(validadorGeneral::validate)
                .flatMap(service::registrar)
                .flatMap(p -> ServerResponse.created(URI.create(req.uri().toString().concat("/").concat(p.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(p)));
    }

    public Mono<ServerResponse> modificar(ServerRequest req) {
        Mono<Factura> monoFactura = req.bodyToMono(Factura.class);
        Mono<Factura> monoBD = service.listarPorId(req.pathVariable("id"));

        return monoBD.zipWith(monoFactura, (bd, f) -> {
                    bd.setId(f.getId());
                    bd.setCliente(f.getCliente());
                    bd.setDescripcion(f.getDescripcion());
                    bd.setItems(f.getItems());
                    bd.setObservacion(f.getObservacion());
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
