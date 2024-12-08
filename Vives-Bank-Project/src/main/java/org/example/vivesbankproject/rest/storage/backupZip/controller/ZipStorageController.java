package org.example.vivesbankproject.rest.storage.backupZip.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.vivesbankproject.rest.storage.exceptions.StorageInternal;
import org.example.vivesbankproject.rest.storage.backupZip.services.ZipStorageService;
import org.example.vivesbankproject.rest.storage.exceptions.StorageNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@Slf4j
@RequestMapping("/storage/zip")
public class ZipStorageController {

    private final ZipStorageService zipStorageService;

    @Autowired
    public ZipStorageController(ZipStorageService zipStorageService) {
        this.zipStorageService = zipStorageService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateZip() {
        try {
            String storedFilename = zipStorageService.export();
            return ResponseEntity.ok("Archivo ZIP generado con éxito: " + storedFilename);
        } catch (StorageInternal e) {
            log.error("Error al generar el archivo ZIP: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar el archivo ZIP.");
        }
    }

    @GetMapping("/import/{filename:.+}")
    public ResponseEntity<Void> importFromZip(@PathVariable String filename) {
        try {
            zipStorageService.loadFromZip(new File(filename));

            return ResponseEntity.noContent().build();
        } catch (StorageNotFound e) {
            log.error("Archivo no encontrado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (Exception e) {
            log.error("Error desconocido al procesar el archivo JSON: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


    @GetMapping(value = "/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, HttpServletRequest request) {
        try {
            Resource file = zipStorageService.loadAsResource(filename);

            String contentType = request.getServletContext().getMimeType(file.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(file);
        } catch (Exception e) {
            log.error("Error al cargar el archivo ZIP", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        try {
            zipStorageService.delete(filename);
            return ResponseEntity.ok("Archivo ZIP eliminado: " + filename);
        } catch (Exception e) {
            log.error("Error al eliminar el archivo ZIP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el archivo ZIP: " + e.getMessage());
        }
    }
}