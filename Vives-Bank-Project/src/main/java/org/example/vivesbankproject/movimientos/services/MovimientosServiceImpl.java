package org.example.vivesbankproject.movimientos.services;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.vivesbankproject.cliente.exceptions.ClienteNotFound;
import org.example.vivesbankproject.cliente.models.Cliente;
import org.example.vivesbankproject.cliente.repositories.ClienteRepository;
import org.example.vivesbankproject.movimientos.exceptions.ClienteHasNoMovements;
import org.example.vivesbankproject.movimientos.exceptions.MovimientoNotFound;
import org.example.vivesbankproject.movimientos.models.Movimientos;
import org.example.vivesbankproject.movimientos.repositories.MovimientosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@CacheConfig(cacheNames = {"Movimientos"})
public class MovimientosServiceImpl implements MovimientosService {

   // private final ClienteService clienteService;
    private final ClienteRepository clienteRepository;
    private final MovimientosRepository movimientosRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MovimientosServiceImpl( MovimientosRepository movimientosRepository, MongoTemplate mongoTemplate, ClienteRepository clienteRepository) {
        //this.clienteService = clienteService;
        this.clienteRepository = clienteRepository;
        this.movimientosRepository = movimientosRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Movimientos> getAll(Optional<Cliente> cliente, Pageable pageable) {
        log.info("Encontrando todos los Movimientos");

        // Construir el filtro dinámico para MongoDB
        Query query = new Query();

        cliente.ifPresent(c -> {
            // Usar Criteria para filtrar por ID del cliente
            query.addCriteria(Criteria.where("cliente.id").is(c.getId()));
        });

        // Configurar paginación
        query.with(pageable);

        // Ejecutar la consulta
        List<Movimientos> movimientos = mongoTemplate.find(query, Movimientos.class);

        // Contar el total de elementos solo si es necesario
        long total = cliente.isPresent() || pageable.getOffset() > 0 || movimientos.size() == pageable.getPageSize() ?
                mongoTemplate.count(query, Movimientos.class) :
                movimientos.size();

        // Devolver como Page
        return PageableExecutionUtils.getPage(movimientos, pageable, () -> total);
    }


    @Override
    @Cacheable(key = "#idMovimiento")
    public Movimientos getById(ObjectId idMovimiento) {
        log.info("Encontrando Movimiento por id: {}", idMovimiento);
        return movimientosRepository.findById(idMovimiento).orElseThrow(
                () -> new MovimientoNotFound(idMovimiento)
        );
    }

    @Override
    @Cacheable(key = "#idCliente")
    public Movimientos getByIdCliente(UUID idCliente) {
        log.info("Encontrando Movimientos por idCliente: {}", idCliente);
        return movimientosRepository.findMovimientosByClienteId(idCliente)
                .orElseThrow(() -> new ClienteHasNoMovements(idCliente));
    }

    @Override
    @CachePut(key = "#result.idMovimiento")
    public Movimientos save(Movimientos movimiento) {
        log.info("Guardando Movimiento: {}", movimiento);
        var cliente = clienteRepository.findById(movimiento.getCliente().getId()).orElseThrow(() -> new ClienteNotFound(movimiento.getCliente().getId()));
        if (cliente.getIdMovimientos() == null) {
            cliente.setIdMovimientos(movimiento.getId());
            movimiento.setCliente(cliente);
            return movimientosRepository.save(movimiento);
        } else {
            var savedMovimiento = getById(cliente.getIdMovimientos());
            movimiento.getTransacciones().forEach(newTransaccion -> {
                    savedMovimiento.getTransacciones().add(newTransaccion);
            });
            return movimientosRepository.save(savedMovimiento);
        }
    }

    /**
     * MIRAR SI LO BORRAMOS YA QUE UN MOVIMIENTO NUNCA SE DERIA DE ACTUALIZAR
     * */
    @Override
    public Movimientos update(ObjectId idMovimiento, Movimientos Movimiento) {
        log.info("Actualizando Movimiento: {} con {}", idMovimiento, Movimiento);
        var savedMovimient = movimientosRepository.findById(idMovimiento);
        if (savedMovimient.isPresent()) {
            return movimientosRepository.save(Movimiento);
        }
        throw new MovimientoNotFound(idMovimiento);
    }

    /**
     * SE PUEDE MIRAR SI EN VED DE ELIMINAR UN MOVIMIENTO ELIMINAMOS EL CLIENTE DEL MOVIMIENTO
     * */
    @Override
    public void delete(ObjectId idMovimiento) {

    }

    /**
     * SE PUEDE IMPLEMENTAR POR SI EN ALGUN CASO NOS INTERESARA "BORRAR" UN MOVIMIENTO
     * */
    @Override
    public void softDelete(ObjectId idMovimiento) {

    }
}