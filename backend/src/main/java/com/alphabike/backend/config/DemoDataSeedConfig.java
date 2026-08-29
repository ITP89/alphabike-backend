package com.alphabike.backend.config;

import com.alphabike.backend.categoria.Categoria;
import com.alphabike.backend.categoria.CategoriaRepository;
import com.alphabike.backend.producto.Producto;
import com.alphabike.backend.producto.ProductoRepository;
import com.alphabike.backend.servicio.Servicio;
import com.alphabike.backend.servicio.ServicioRepository;
import com.alphabike.backend.trabajo.TrabajoRealizado;
import com.alphabike.backend.trabajo.TrabajoRealizadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DemoDataSeedConfig {

    @Value("${app.seed.demo.enabled:false}")
    private boolean demoSeedEnabled;

    @Bean
    CommandLineRunner seedDemoData(
            CategoriaRepository categoriaRepository,
            ServicioRepository servicioRepository,
            ProductoRepository productoRepository,
            TrabajoRealizadoRepository trabajoRepository
    ) {
        return args -> {
            if (!demoSeedEnabled) {
                return;
            }

            Categoria accesorios = ensureCategoria(categoriaRepository, "Accesorios", "Cascos, luces, candados y articulos de seguridad");
            Categoria repuestos = ensureCategoria(categoriaRepository, "Repuestos", "Componentes para reparar y mejorar bicicletas");
            Categoria herramientas = ensureCategoria(categoriaRepository, "Herramientas", "Herramientas utiles para mantenimiento basico");

            ensureServicio(servicioRepository, "Mantenimiento general", "Revision, ajuste de frenos, cambios y limpieza completa", "70.00", 90);
            ensureServicio(servicioRepository, "Ajuste de frenos", "Calibracion y revision de pastillas o zapatas", "35.00", 45);
            ensureServicio(servicioRepository, "Diagnostico tecnico", "Revision inicial para detectar fallas y preparar cotizacion", "25.00", 30);

            ensureProducto(productoRepository, accesorios, "Casco urbano Alpha", "Casco ventilado para uso diario", "AlphaBike", "89.90", 12);
            ensureProducto(productoRepository, accesorios, "Luz LED recargable", "Luz delantera USB con tres modos", "Rider", "45.00", 18);
            ensureProducto(productoRepository, repuestos, "Cadena 8 velocidades", "Cadena compatible para bicicletas urbanas y MTB", "KMC", "58.50", 10);
            ensureProducto(productoRepository, herramientas, "Multiherramienta compacta", "Llaves Allen y destornilladores para ruta", "BikePro", "39.90", 15);

            ensureTrabajo(trabajoRepository, "Mantenimiento urbano completo", "Limpieza, ajuste de frenos y calibracion de cambios", true);
            ensureTrabajo(trabajoRepository, "Cambio de transmision", "Instalacion de cadena nueva y regulacion de cambios", true);
            ensureTrabajo(trabajoRepository, "Revision de frenos", "Ajuste de tension y prueba de frenado", false);
        };
    }

    private Categoria ensureCategoria(CategoriaRepository repository, String nombre, String descripcion) {
        return repository.findAll().stream()
                .filter(categoria -> categoria.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseGet(() -> repository.save(Categoria.builder()
                        .nombre(nombre)
                        .descripcion(descripcion)
                        .build()));
    }

    private void ensureServicio(ServicioRepository repository, String nombre, String descripcion, String precio, int duracionMin) {
        if (repository.existsByNombre(nombre)) {
            return;
        }

        repository.save(Servicio.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .precioBase(new BigDecimal(precio))
                .duracionMin(duracionMin)
                .build());
    }

    private void ensureProducto(ProductoRepository repository, Categoria categoria, String nombre, String descripcion, String marca, String precio, int stock) {
        boolean exists = repository.findAll().stream()
                .anyMatch(producto -> producto.getNombre().equalsIgnoreCase(nombre));

        if (exists) {
            return;
        }

        repository.save(Producto.builder()
                .categoria(categoria)
                .nombre(nombre)
                .descripcion(descripcion)
                .marca(marca)
                .precio(new BigDecimal(precio))
                .stock(stock)
                .estado(Producto.Estado.ACTIVO)
                .build());
    }

    private void ensureTrabajo(TrabajoRealizadoRepository repository, String titulo, String descripcion, boolean destacado) {
        boolean exists = repository.findAll().stream()
                .anyMatch(trabajo -> trabajo.getTitulo().equalsIgnoreCase(titulo));

        if (exists) {
            return;
        }

        repository.save(TrabajoRealizado.builder()
                .titulo(titulo)
                .descripcion(descripcion)
                .fecha(LocalDate.now())
                .destacado(destacado)
                .build());
    }
}
