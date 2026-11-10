package com.alphabike.backend.historial;

import com.alphabike.backend.cita.CitaService;
import com.alphabike.backend.shared.response.ApiResponse;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataAccessException;
import com.mongodb.MongoException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/citas/{id}/historial")
@RequiredArgsConstructor
@Validated
public class HistorialController {
    private final CitaService citas;
    private final ObjectProvider<HistorialMongoService> historial;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO', 'CLIENTE')")
    public ResponseEntity<ApiResponse<List<EventoCita>>> listar(@PathVariable String id,
            @AuthenticationPrincipal String email,
            @RequestParam(defaultValue = "0") @Min(0) @Max(10000) int pagina) {
        // Authorize against the relational source before reading any documents.
        citas.obtener(id, email);
        HistorialMongoService servicio = historial.getIfAvailable();
        if (servicio == null) return noDisponible();
        try {
            return ResponseEntity.ok(ApiResponse.ok("Historial obtenido", servicio.listar(id, pagina)));
        } catch (DataAccessException | MongoException ex) {
            return noDisponible();
        }
    }

    private ResponseEntity<ApiResponse<List<EventoCita>>> noDisponible() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("El historial no esta disponible temporalmente. Intenta nuevamente."));
    }
}
