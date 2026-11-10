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

        List<DetallePedido> detalles = detallesPagados();
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
        List<DetallePedido> detalles = detallesPagados();

        List<Usuario> encargados = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == Usuario.Rol.ENCARGADO)
                .toList();

        return encargados.stream().map(encargado -> {
            List<DetallePedido> detallesEncargado = detalles.stream()
                    .filter(d -> d.getPedido() != null
                            && d.getPedido().getCliente() != null
                            && encargado.getId().equals(d.getPedido().getCliente().getId()))
                    .toList();

            long totalVentas = detallesEncargado.stream()
                    .map(d -> d.getPedido().getId())
                    .distinct()
                    .count();

            BigDecimal totalDesc = detallesEncargado.stream()
                    .map(d -> {
                        BigDecimal lista = d.getPrecioLista() != null ? d.getPrecioLista() : BigDecimal.ZERO;
                        BigDecimal acordado = d.getPrecioAcordado() != null ? d.getPrecioAcordado() : lista;
                        int cant = d.getCantidad() != null ? d.getCantidad() : 1;
                        return lista.subtract(acordado).multiply(BigDecimal.valueOf(cant));
                    })
                    .filter(d -> d.compareTo(BigDecimal.ZERO) > 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal promedio = totalVentas == 0 ? BigDecimal.ZERO :
                    totalDesc.divide(BigDecimal.valueOf(totalVentas),
                            2, RoundingMode.HALF_UP);

            return DescuentosReporteResponse.builder()
                    .encargadoId(encargado.getId())
                    .encargadoNombre(encargado.getNombre())
                    .totalVentas(totalVentas)
                    .totalDescuentos(totalDesc)
                    .promedioDescuento(promedio)
                    .build();
        }).toList();
    }

    private List<DetallePedido> detallesPagados() {
        Set<String> pagados = pagoRepository.findByEstado(Pago.Estado.PAGADO).stream()
                .filter(p -> p.getReferenciaTipo() == Pago.ReferenciaTipo.PEDIDO)
                .map(Pago::getReferenciaId).collect(Collectors.toSet());
        return detallePedidoRepository.findAll().stream()
                .filter(d -> d.getPedido() != null && d.getPedido().getEstado() != Pedido.Estado.CANCELADO
                        && pagados.contains(d.getPedido().getId())).toList();
    }

    public List<ProductoPopularResponse> productosPopulares() {
        return detallesPagados().stream().filter(d -> d.getProducto() != null)
                .collect(Collectors.groupingBy(d -> d.getProducto().getId())).entrySet().stream()
                .map(e -> ProductoPopularResponse.builder().productoId(e.getKey())
                        .productoNombre(e.getValue().get(0).getProducto().getNombre())
                        .totalVendido(e.getValue().stream().mapToLong(DetallePedido::getCantidad).sum()).build())
                .sorted(Comparator.comparing(ProductoPopularResponse::getTotalVendido).reversed()
                        .thenComparing(ProductoPopularResponse::getProductoId))
                .limit(10).toList();
    }

    public List<ServicioPopularResponse> serviciosPopulares() {
        return citaRepository.findAll().stream()
                .filter(c -> c.getServicio() != null && c.getEstado() != com.alphabike.backend.cita.Cita.Estado.CANCELADO)
                .collect(Collectors.groupingBy(c -> c.getServicio().getId())).entrySet().stream()
                .map(e -> ServicioPopularResponse.builder().servicioId(e.getKey())
                        .servicioNombre(e.getValue().get(0).getServicio().getNombre())
                        .totalCitas((long) e.getValue().size()).build())
                .sorted(Comparator.comparing(ServicioPopularResponse::getTotalCitas).reversed()
                        .thenComparing(ServicioPopularResponse::getServicioId))
                .limit(10).toList();
    }
}
