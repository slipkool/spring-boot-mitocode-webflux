package com.personal.webflux.app.controller;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static reactor.function.TupleUtils.function;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personal.webflux.app.model.Factura;
import com.personal.webflux.app.service.IFacturaService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/facturas")
public class FacturaController {

    @Autowired
    private IFacturaService Facturaservice;

    @GetMapping
    public Mono<ResponseEntity<Flux<Factura>>> listar() {
        Flux<Factura> fxFacturas = Facturaservice.listar();

        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fxFacturas));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Factura>> listarPorId(@PathVariable("id") String id) {
        return Facturaservice.listarPorId(id)
                .map(p -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Factura>> registrar(@RequestBody Factura Factura, final ServerHttpRequest request) {
        return Facturaservice.registrar(Factura)
                .map(p -> ResponseEntity.created(URI.create(request.getURI().toString().concat("/").concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p));
    }

    @PutMapping
    public Mono<ResponseEntity<Factura>> modificar(@RequestBody Factura Factura) {
        return Facturaservice.modificar(Factura)
                .map(p -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable("id") String id) {
        return Facturaservice.listarPorId(id)
                .flatMap(p -> {
                    return Facturaservice.eliminar(p.getId())
                            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
                })
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }
    //--------------------------
    //private Factura FacturaHateoas;

    @GetMapping("/hateoas/{id}")
    public Mono<EntityModel<Factura>> listarHateoasPorId(@PathVariable("id") String id) {
        Mono<Link> link1 = linkTo(methodOn(FacturaController.class).listarPorId(id)).withSelfRel().toMono();
        Mono<Link> link2 = linkTo(methodOn(FacturaController.class).listarPorId(id)).withSelfRel().toMono();

        //PRACTIVA NO RECOMENDADA
//        return Facturaservice.listarPorId(id)
//                .flatMap(p -> {
//                    this.FacturaHateoas = p;
//                    return link1;
//                }).map(links -> {
//                    return EntityModel.of(this.FacturaHateoas, links);
//                });

        //PRACTICA INTERMEDIA
//        return Facturaservice.listarPorId(id)
//                .flatMap(p -> {
//                    return link1.map(links -> EntityModel.of(p, links));
//                });

        //PRACTICA IDEAL
//        return Facturaservice.listarPorId(id)
//                .zipWith(link1, (p, links) -> EntityModel.of(p, links));

        //MAS DE UN LINK
        return link1.zipWith(link2)
                .map(function((left, right) -> Links.of(left, right)))
                .zipWith(Facturaservice.listarPorId(id), (links, p) -> EntityModel.of(p, links));
    }
}
