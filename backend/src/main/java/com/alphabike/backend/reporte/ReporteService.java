package com.alphabike.backend.reporte;

import com.alphabike.backend.cita.CitaRepository;
import com.alphabike.backend.pedido.DetallePedido;
import com.alphabike.backend.pedido.DetallePedidoRepository;
import com.alphabike.backend.pedido.Pedido;
import com.alphabike.backend.pedido.PedidoRepository;
import com.alphabike.backend.pago.Pago;
import com.alphabike.backend.pago.PagoRepository;
import com.alphabike.backend.reporte.dto.*;
import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final CitaRepository citaRepository;
    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;

    public VentasReporteResponse reporteVentas() {
        List<Pago> pagos = pagoRepository.findByEstado(Pago.Estado.PAGADO);

        BigDecimal ingresosPedidos = pagos.stream()
                .filter(p -> p.getReferenciaTipo() == Pago.ReferenciaTipo.PEDIDO)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ingresosServicios = pagos.stream()
                .filter(p -> p.getReferenciaTipo() == Pago.ReferenciaTipo.COTIZACION)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DetallePedido> detalles = detallePedidoRepository.findAll();
        BigDecimal totalDescuentos = detalles.stream()
                .map(d -> {
                    BigDecimal lista = d.getPrecioLista() != null ? d.getPrecioLista() : BigDecimal.ZERO;
                    BigDecimal acordado = d.getPrecioAcordado() != null ? d.getPrecioAcordado() : lista;
                    int cant = d.getCantidad() != null ? d.getCantidad() : 1;
                    return lista.subtract(acordado).multiply(BigDecimal.valueOf(cant));
                })
                .filter(d -> d.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return VentasReporteResponse.builder()
                .ingresosTotales(ingresosPedidos.add(ingresosServicios))
                .ingresosPedidos(ingresosPedidos)
                .ingresosServicios(ingresosServicios)
                .totalPedidos((long) pedidoRepository.findAll().size())
                .totalCitas((long) citaRepository.findAll().size())
                .totalDescuentos(totalDescuentos)
                .build();
    }

    public List<DescuentosReporteResponse> reporteDescuentosPorEncargado() {
        List<DetallePedido> detalles = detallePedidoRepository.findAll();

        List<Usuario> encargados = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == Usuario.Rol.ENCARGADO)
                .toList();

        return encargados.stream().map(encargado -> {
            List<DetallePedido> detallesEncargado = detalles.stream()
                    .filter(d -> d.getPedido() != null)
                    .toList();

            BigDecimal totalDesc = detallesEncargado.stream()
                    .map(d -> {
                        BigDecimal lista = d.getPrecioLista() != null ? d.getPrecioLista() : BigDecimal.ZERO;
                        BigDecimal acordado = d.getPrecioAcordado() != null ? d.getPrecioAcordado() : lista;
                        int cant = d.getCantidad() != null ? d.getCantidad() : 1;
                        return lista.subtract(acordado).multiply(BigDecimal.valueOf(cant));
                    })
                    .filter(d -> d.compareTo(BigDecimal.ZERO) > 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal promedio = detallesEncargado.isEmpty() ? BigDecimal.ZERO :
                    totalDesc.divide(BigDecimal.valueOf(detallesEncargado.size()),
                            2, RoundingMode.HALF_UP);

            return DescuentosReporteResponse.builder()
                    .encargadoId(encargado.getId())
                    .encargadoNombre(encargado.getNombre())
                    .totalVentas((long) detallesEncargado.size())
                    .totalDescuentos(totalDesc)
                    .promedioDescuento(promedio)
                    .build();
        }).toList();
    }

    public List<ProductoPopularResponse> productosPopulares() {
        return detallePedidoRepository.findAll().stream()
                .filter(d -> d.getProducto() != null)
                .collect(Collectors.groupingBy(
                        d -> d.getProducto().getId(),
                        Collectors.summingLong(DetallePedido::getCantidad)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    DetallePedido detalle = detallePedidoRepository.findAll().stream()
                            .filter(d -> d.getProducto() != null && d.getProducto().getId().equals(e.getKey()))
                            .findFirst().orElse(null);
                    String nombre = (detalle != null && detalle.getProducto() != null)
                            ? detalle.getProducto().getNombre()
                            : "Producto #" + e.getKey();
                    return ProductoPopularResponse.builder()
                            .productoId(e.getKey())
                            .productoNombre(nombre)
                            .totalVendido(e.getValue())
                            .build();
                })
                .toList();
    }

    public List<ServicioPopularResponse> serviciosPopulares() {
        return citaRepository.findAll().stream()
                .filter(c -> c.getServicio() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getServicio().getId(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    var cita = citaRepository.findAll().stream()
                            .filter(c -> c.getServicio() != null && c.getServicio().getId().equals(e.getKey()))
                            .findFirst().orElse(null);
                    String nombre = (cita != null && cita.getServicio() != null)
                            ? cita.getServicio().getNombre()
                            : "Servicio #" + e.getKey();
                    return ServicioPopularResponse.builder()
                            .servicioId(e.getKey())
                            .servicioNombre(nombre)
                            .totalCitas(e.getValue())
                            .build();
                })
                .toList();
    }
}