package com.alphabike.backend.reporte;

import com.alphabike.backend.reporte.dto.*;
import com.alphabike.backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/ventas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VentasReporteResponse>> ventas() {
        return ResponseEntity.ok(ApiResponse.ok("Reporte de ventas", reporteService.reporteVentas()));
    }

    @GetMapping("/descuentos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DescuentosReporteResponse>>> descuentos() {
        return ResponseEntity.ok(ApiResponse.ok("Reporte de descuentos",
                reporteService.reporteDescuentosPorEncargado()));
    }

    @GetMapping("/productos-populares")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductoPopularResponse>>> productosPopulares() {
        return ResponseEntity.ok(ApiResponse.ok("Productos populares",
                reporteService.productosPopulares()));
    }

    @GetMapping("/servicios-populares")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ServicioPopularResponse>>> serviciosPopulares() {
        return ResponseEntity.ok(ApiResponse.ok("Servicios populares",
                reporteService.serviciosPopulares()));
    }
}