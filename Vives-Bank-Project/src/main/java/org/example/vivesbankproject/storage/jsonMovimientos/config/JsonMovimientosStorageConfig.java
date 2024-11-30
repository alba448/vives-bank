package org.example.vivesbankproject.storage.jsonMovimientos.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.vivesbankproject.storage.jsonMovimientos.services.JsonMovimientosStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
@Slf4j
public class JsonMovimientosStorageConfig {
    private final JsonMovimientosStorageService jsonMovimientosStorageService;

    @Value("${upload.delete}")
    private String deleteAll;

    @Autowired
    public JsonMovimientosStorageConfig(JsonMovimientosStorageService jsonMovimientosStorageService) {
        this.jsonMovimientosStorageService = jsonMovimientosStorageService;
    }

    @PostConstruct
    public void init() {
        if (deleteAll.equals("true")) {
            log.info("Borrando ficheros de almacenamiento...");
            jsonMovimientosStorageService.delete("movimientos_clientes_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".json");
        }

        jsonMovimientosStorageService.init();
    }
}