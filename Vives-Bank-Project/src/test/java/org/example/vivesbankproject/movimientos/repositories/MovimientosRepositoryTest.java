package org.example.vivesbankproject.movimientos.repositories;

import org.example.vivesbankproject.cliente.models.Cliente;
import org.example.vivesbankproject.movimientos.models.Movimientos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class MovimientosRepositoryTest {

    @Autowired
    private MovimientosRepository movimientosRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Environment env;

    private UUID clienteId;
    private Movimientos movimientos;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);

        movimientos = new Movimientos();
        movimientos.setCliente(cliente);
        // Aquí puedes agregar más configuraciones para el objeto Movimientos si es necesario

        movimientosRepository.save(movimientos);
    }

    @AfterEach
    void tearDown() {
        movimientosRepository.deleteAll();
    }

    @Test
    void findMovimientosByClienteId_shouldReturnMovimientos_whenClienteExists() {
        Optional<Movimientos> result = movimientosRepository.findMovimientosByClienteId(clienteId);

        assertAll(
                () -> assertTrue(result.isPresent(), "El resultado debería estar presente"),
                () -> assertEquals(clienteId, result.get().getCliente().getId(), "El ID del cliente debería coincidir")
        );
    }

    @Test
    void findMovimientosByClienteId_shouldReturnEmpty_whenClienteDoesNotExist() {
        UUID nonExistentClienteId = UUID.randomUUID();

        Optional<Movimientos> result = movimientosRepository.findMovimientosByClienteId(nonExistentClienteId);

        assertTrue(result.isEmpty(), "El resultado debería estar vacío para un cliente inexistente");
    }

    @Test
    void verifyEmbeddedMongoDB() {
        assertAll(
                () -> assertTrue(mongoTemplate.getDb().getName().startsWith("banco-dev"), "El nombre de la base de datos debería empezar con 'test'"),
                () -> assertNull(env.getProperty("spring.data.mongodb.uri"), "La URI de MongoDB debería ser null para la base de datos embebida")
        );
    }
}