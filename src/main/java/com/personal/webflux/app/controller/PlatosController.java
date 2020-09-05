package com.personal.webflux.app.controller;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static reactor.function.TupleUtils.function;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.personal.webflux.app.model.Plato;
import com.personal.webflux.app.pagination.PageSupport;
import com.personal.webflux.app.service.IPlatoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/platos")
public class PlatosController {

    @Autowired
    private IPlatoService platoService;

    @GetMapping
    public Mono<ResponseEntity<Flux<Plato>>> listar() {
        Flux<Plato> fxPlatos = platoService.listar();

        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fxPlatos));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Plato>> listarPorId(@PathVariable("id") String id) {
        return platoService.listarPorId(id)
                .map(p -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Plato>> registrar(@RequestBody Plato plato, final ServerHttpRequest request) {
        return platoService.registrar(plato)
                .map(p -> ResponseEntity.created(URI.create(request.getURI().toString().concat("/").concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Plato>> modificar(@PathVariable("id") String id, @RequestBody Plato plato) {
        Mono<Plato> monoBD = platoService.listarPorId(id);
        Mono<Plato> monoPlato = Mono.just(plato);

        return monoBD.zipWith(monoPlato, (bd, p) -> {
                    bd.setEstado(p.getEstado());
                    bd.setNombre(p.getNombre());
                    return bd;
                })
                .flatMap(platoService::modificar)
                .map(p -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable("id") String id) {
        return platoService.listarPorId(id)
                .flatMap(p -> {
                    return platoService.eliminar(p.getId())
                            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
                })
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }
    //--------------------------
    //private Plato platoHateoas;

    @GetMapping("/hateoas/{id}")
    public Mono<EntityModel<Plato>> listarHateoasPorId(@PathVariable("id") String id) {
        Mono<Link> link1 = linkTo(methodOn(PlatosController.class).listarPorId(id)).withSelfRel().toMono();
        Mono<Link> link2 = linkTo(methodOn(PlatosController.class).listarPorId(id)).withSelfRel().toMono();

        //PRACTIVA NO RECOMENDADA
//        return platoService.listarPorId(id)
//                .flatMap(p -> {
//                    this.platoHateoas = p;
//                    return link1;
//                }).map(links -> {
//                    return EntityModel.of(this.platoHateoas, links);
//                });

        //PRACTICA INTERMEDIA
//        return platoService.listarPorId(id)
//                .flatMap(p -> {
//                    return link1.map(links -> EntityModel.of(p, links));
//                });

        //PRACTICA IDEAL
//        return platoService.listarPorId(id)
//                .zipWith(link1, (p, links) -> EntityModel.of(p, links));

        //MAS DE UN LINK
        return link1.zipWith(link2)
                .map(function((left, right) -> Links.of(left, right)))
                .zipWith(platoService.listarPorId(id), (links, p) -> EntityModel.of(p, links));
    }

    @GetMapping("/pageable")
    public Mono<ResponseEntity<PageSupport<Plato>>> listarPagebale(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
            ){

        Pageable pageRequest = PageRequest.of(page, size);

        return platoService.listarPage(pageRequest)
                .map(p -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p)
                        )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /*
     * Consumo de servicio externo
     */
    @GetMapping("/client1")
    public Flux<Plato> listarClient1(){
        Flux<Plato> fx = WebClient.create("http://localhost:8080/platos")
                            .get()
                            .retrieve() //recuperar
                            .bodyToFlux(Plato.class);
        return fx;
    }
}
