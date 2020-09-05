package com.personal.webflux.app.controller;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static reactor.function.TupleUtils.function;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

import org.cloudinary.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.personal.webflux.app.model.Cliente;
import com.personal.webflux.app.service.IClienteService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/clientes")
public class ClientesController {

    @Autowired
    private IClienteService clienteService;

    @Value("${ruta.subida}")
    private String RUTA_SUBIDA;

    @GetMapping
    public Mono<ResponseEntity<Flux<Cliente>>> listar() {
        Flux<Cliente> fxClientes = clienteService.listar();

        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fxClientes));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Cliente>> listarPorId(@PathVariable("id") String id) {
        return clienteService.listarPorId(id)
                .map(p -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Cliente>> registrar(@RequestBody Cliente cliente, final ServerHttpRequest request) {
        return clienteService.registrar(cliente)
                .map(p -> ResponseEntity.created(URI.create(request.getURI().toString().concat("/").concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p));
    }

    @PutMapping
    public Mono<ResponseEntity<Cliente>> modificar(@RequestBody Cliente cliente) {
        return clienteService.modificar(cliente)
                .map(p -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable("id") String id) {
        return clienteService.listarPorId(id)
                .flatMap(p -> {
                    return clienteService.eliminar(p.getId())
                            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
                })
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }
    //--------------------------
    //private Cliente clienteHateoas;

    @GetMapping("/hateoas/{id}")
    public Mono<EntityModel<Cliente>> listarHateoasPorId(@PathVariable("id") String id) {
        Mono<Link> link1 = linkTo(methodOn(ClientesController.class).listarPorId(id)).withSelfRel().toMono();
        Mono<Link> link2 = linkTo(methodOn(ClientesController.class).listarPorId(id)).withSelfRel().toMono();

        //PRACTIVA NO RECOMENDADA
//        return clienteService.listarPorId(id)
//                .flatMap(p -> {
//                    this.clienteHateoas = p;
//                    return link1;
//                }).map(links -> {
//                    return EntityModel.of(this.clienteHateoas, links);
//                });

        //PRACTICA INTERMEDIA
//        return clienteService.listarPorId(id)
//                .flatMap(p -> {
//                    return link1.map(links -> EntityModel.of(p, links));
//                });

        //PRACTICA IDEAL
//        return clienteService.listarPorId(id)
//                .zipWith(link1, (p, links) -> EntityModel.of(p, links));

        //MAS DE UN LINK
        return link1.zipWith(link2)
                .map(function((left, right) -> Links.of(left, right)))
                .zipWith(clienteService.listarPorId(id), (links, p) -> EntityModel.of(p, links));
    }

    /*
     * subir imagen al servidor local
     */
//    @PostMapping("subir/{id}")
//    public Mono<ResponseEntity<Cliente>> subir(@PathVariable String id, @RequestPart FilePart file) {
//        return clienteService.listarPorId(id)
//                .flatMap(c -> {
//                    c.setUrlFoto(UUID.randomUUID() + "-" + file.filename());
//                    return file.transferTo(new File(RUTA_SUBIDA + c.getUrlFoto())).then(clienteService.registrar(c));
//                })
//                .map(c -> ResponseEntity.ok(c))
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }
    /*
     * subir a imagen a un servidor externo Cloudinary
     */
    @PostMapping("subir/{id}")
    public Mono<ResponseEntity<Cliente>> subir(@PathVariable String id, @RequestPart FilePart file) {
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", "TU_CLOUD_NAME"
                                                                , "api_key", "TU_API_KEY"
                                                                , "api_secret", "TU_API_SECRET"));

        return clienteService.listarPorId(id)
                .map(c -> {
                    try {
                        File f = Files.createTempFile("temp", file.filename()).toFile();
                        file.transferTo(f);
                        Map response = cloudinary.uploader().upload(f, ObjectUtils.asMap("resource_type", "auto"));

                        JSONObject json = new JSONObject(response);
                        String url  = json.getString("url");

                        c.setUrlFoto(url);
                        clienteService.modificar(c).then(Mono.just(ResponseEntity.ok().body(c)));
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    return ResponseEntity.ok().body(c);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
