package com.alphabike.backend.pedido;

import com.alphabike.backend.pedido.dto.*;
import com.alphabike.backend.producto.Producto;
import com.alphabike.backend.producto.ProductoRepository;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import com.alphabike.backend.shared.exception.UnauthorizedException;
import com.alphabike.backend.shared.validation.EnumUtils;
import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public List<PedidoResponse> listar() {
        return pedidoRepository.findAll()
                .stream()
                .map(PedidoResponse::from)
                .toList();
    }

    public List<PedidoResponse> listarPorCliente(String email) {
        Usuario cliente = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        return pedidoRepository.findByClienteId(cliente.getId())
                .stream()
                .map(PedidoResponse::from)
                .toList();
    }

    public PedidoResponse obtener(String id, String userEmail) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getRol() == Usuario.Rol.CLIENTE && !pedido.getCliente().getEmail().equalsIgnoreCase(userEmail)) {
            throw new UnauthorizedException("No tiene permiso para ver este pedido");
        }

        return PedidoResponse.from(pedido);
    }

    @Transactional
    public PedidoResponse crear(PedidoRequest request, String email) {
        Usuario cliente = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        Pedido.TipoEntrega tipoEntrega = EnumUtils.parse(
                Pedido.TipoEntrega.class,
                request.getTipoEntrega(),
                "tipoEntrega"
        );
        BigDecimal costoEnvio = calcularCostoEnvio(tipoEntrega);
        Map<String, Integer> cantidadesPorProducto = agruparCantidades(request.getDetalles());
        Map<String, Producto> productos = cargarYValidarProductos(cantidadesPorProducto);

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .estado(Pedido.Estado.PENDIENTE)
                .tipoEntrega(tipoEntrega)
                .direccionEntrega(request.getDireccionEntrega())
                .costoEnvio(costoEnvio)
                .total(BigDecimal.ZERO)
                .build();

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        BigDecimal total = BigDecimal.ZERO;
        List<DetallePedido> detallesGuardados = new ArrayList<>();

        for (DetallePedidoRequest detalleReq : request.getDetalles()) {
            Producto producto = productos.get(detalleReq.getProductoId());

            // Para pedidos de clientes, se usa siempre el precio de lista del producto
            BigDecimal precioAcordado = producto.getPrecio();

            BigDecimal subtotal = precioAcordado.multiply(
                    BigDecimal.valueOf(detalleReq.getCantidad()));

            DetallePedido detalle = DetallePedido.builder()
                    .pedido(pedidoGuardado)
                    .producto(producto)
                    .cantidad(detalleReq.getCantidad())
                    .precioLista(producto.getPrecio())
                    .precioAcordado(precioAcordado)
                    .subtotal(subtotal)
                    .build();

            detallesGuardados.add(detallePedidoRepository.save(detalle));
            total = total.add(subtotal);

            producto.setStock(producto.getStock() - detalleReq.getCantidad());
            productoRepository.save(producto);
        }

        pedidoGuardado.setTotal(total.add(costoEnvio));
        pedidoGuardado.setDetalles(detallesGuardados);
        return PedidoResponse.from(pedidoRepository.save(pedidoGuardado));
    }

    public PedidoResponse cambiarEstado(String id, String estado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        pedido.setEstado(EnumUtils.parse(Pedido.Estado.class, estado, "estado"));
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    public PedidoResponse registrarSeguimiento(String id, String numeroSeguimiento) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        pedido.setNumeroSeguimiento(numeroSeguimiento);
        pedido.setEstado(Pedido.Estado.ENVIADO);
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    public PedidoResponse actualizarPrecioAcordado(String pedidoId, String detalleId,
                                                   BigDecimal precioAcordado) {
        if (precioAcordado == null || precioAcordado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El precio acordado debe ser mayor a cero");
        }

        DetallePedido detalle = detallePedidoRepository.findById(detalleId)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado"));
        if (!detalle.getPedido().getId().equals(pedidoId)) {
            throw new UnauthorizedException("El detalle no pertenece a este pedido");
        }
        detalle.setPrecioAcordado(precioAcordado);
        detalle.setSubtotal(precioAcordado.multiply(BigDecimal.valueOf(detalle.getCantidad())));
        detallePedidoRepository.save(detalle);

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        BigDecimal nuevoTotal = detallePedidoRepository.findByPedidoId(pedidoId)
                .stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(pedido.getCostoEnvio() != null ? pedido.getCostoEnvio() : BigDecimal.ZERO);
        pedido.setTotal(nuevoTotal);
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    private Map<String, Integer> agruparCantidades(List<DetallePedidoRequest> detalles) {
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (DetallePedidoRequest detalle : detalles) {
            cantidades.merge(detalle.getProductoId(), detalle.getCantidad(), Integer::sum);
        }
        return cantidades;
    }

    private Map<String, Producto> cargarYValidarProductos(Map<String, Integer> cantidadesPorProducto) {
        Map<String, Producto> productos = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : cantidadesPorProducto.entrySet()) {
            Producto producto = productoRepository.findByIdForUpdate(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            if (producto.getEstado() != Producto.Estado.ACTIVO) {
                throw new BadRequestException("El producto " + producto.getNombre() + " no esta disponible");
            }

            if (producto.getStock() < entry.getValue()) {
                throw new BadRequestException(
                        "Stock insuficiente para " + producto.getNombre()
                                + ". Disponible: " + producto.getStock()
                                + ", solicitado: " + entry.getValue()
                );
            }

            productos.put(entry.getKey(), producto);
        }

        return productos;
    }

    private BigDecimal calcularCostoEnvio(Pedido.TipoEntrega tipoEntrega) {
        return switch (tipoEntrega) {
            case DELIVERY_LIMA -> new BigDecimal("10.00");
            case ENVIO_PROVINCIA -> new BigDecimal("25.00");
            default -> BigDecimal.ZERO;
        };
    }
}
