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
@Transactional
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
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        boolean ventaInterna = usuario.getRol() == Usuario.Rol.ADMIN || usuario.getRol() == Usuario.Rol.ENCARGADO;

        Pedido.TipoEntrega tipoEntrega = EnumUtils.parse(
                Pedido.TipoEntrega.class,
                request.getTipoEntrega(),
                "tipoEntrega"
        );
        if (tipoEntrega != Pedido.TipoEntrega.RECOJO_TIENDA
                && (request.getDireccionEntrega() == null || request.getDireccionEntrega().isBlank())) {
            throw new BadRequestException("La direccion es obligatoria para delivery o envio a provincia");
        }
        BigDecimal costoEnvio = calcularCostoEnvio(tipoEntrega);
        Map<String, Integer> cantidadesPorProducto = agruparCantidades(request.getDetalles());
        Map<String, Producto> productos = cargarYValidarProductos(cantidadesPorProducto);

        Pedido pedido = Pedido.builder()
                .cliente(usuario)
                .estado(Pedido.Estado.PENDIENTE)
                .tipoEntrega(tipoEntrega)
                .direccionEntrega(request.getDireccionEntrega() == null ? null : request.getDireccionEntrega().trim())
                .costoEnvio(costoEnvio)
                .total(BigDecimal.ZERO)
                .build();

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        BigDecimal total = BigDecimal.ZERO;
        List<DetallePedido> detallesGuardados = new ArrayList<>();

        for (DetallePedidoRequest detalleReq : request.getDetalles()) {
            Producto producto = productos.get(detalleReq.getProductoId());

            BigDecimal precioAcordado = ventaInterna && detalleReq.getPrecioAcordado() != null
                    ? detalleReq.getPrecioAcordado()
                    : producto.getPrecio();
            validarPrecioAcordado(producto, precioAcordado);

            BigDecimal subtotal = precioAcordado.multiply(
                    BigDecimal.valueOf(detalleReq.getCantidad()));

            validarImporte(subtotal);

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

        validarImporte(total.add(costoEnvio));
        pedidoGuardado.setTotal(total.add(costoEnvio));
        pedidoGuardado.setDetalles(detallesGuardados);
        return PedidoResponse.from(pedidoRepository.save(pedidoGuardado));
    }

    public PedidoResponse cambiarEstado(String id, String estado) {
        Pedido pedido = pedidoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        Pedido.Estado nuevoEstado = EnumUtils.parse(Pedido.Estado.class, estado, "estado");
        if (nuevoEstado == Pedido.Estado.CANCELADO) return cancelarReservado(pedido);
        if (nuevoEstado == pedido.getEstado()) return PedidoResponse.from(pedido);
        boolean permitido = switch (pedido.getEstado()) {
            case PAGADO -> nuevoEstado == Pedido.Estado.EN_PREPARACION;
            case EN_PREPARACION -> nuevoEstado == Pedido.Estado.LISTO_PARA_RECOJO
                    || nuevoEstado == Pedido.Estado.EN_CAMINO || nuevoEstado == Pedido.Estado.ENVIADO;
            case LISTO_PARA_RECOJO, EN_CAMINO, ENVIADO -> nuevoEstado == Pedido.Estado.ENTREGADO;
            default -> false;
        };
        if (!permitido) throw new BadRequestException("Transicion de pedido no permitida; registra el pago antes de preparar");
        pedido.setEstado(nuevoEstado);
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    public PedidoResponse registrarSeguimiento(String id, String numeroSeguimiento) {
        Pedido pedido = pedidoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        if (numeroSeguimiento == null || numeroSeguimiento.isBlank()) throw new BadRequestException("El seguimiento es obligatorio");
        if (pedido.getEstado() != Pedido.Estado.EN_PREPARACION && pedido.getEstado() != Pedido.Estado.ENVIADO) {
            throw new BadRequestException("Solo se puede enviar un pedido en preparacion");
        }
        pedido.setNumeroSeguimiento(numeroSeguimiento.trim());
        pedido.setEstado(Pedido.Estado.ENVIADO);
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse cancelarPendiente(String id, String emailUsuario) {
        Pedido pedido = pedidoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getRol() == Usuario.Rol.CLIENTE
                && !pedido.getCliente().getId().equals(usuario.getId())) {
            throw new UnauthorizedException("No tiene permiso para cancelar este pedido");
        }
        return cancelarReservado(pedido);
    }

    @Transactional
    public PedidoResponse cancelarMercadoPagoConfirmado(String id, String remoteId) {
        Pedido pedido = pedidoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        if (!remoteId.equals(pedido.getMercadoPagoOrderId())) {
            throw new BadRequestException("La operacion no corresponde al pedido");
        }
        return liberarReserva(pedido);
    }

    private PedidoResponse cancelarReservado(Pedido pedido) {
        if (pedido.getEstado() == Pedido.Estado.PENDIENTE && pedido.getMercadoPagoOrderId() != null) {
            throw new BadRequestException("Espera la confirmacion de cancelacion de Mercado Pago");
        }
        return liberarReserva(pedido);
    }

    private PedidoResponse liberarReserva(Pedido pedido) {
        if (pedido.getEstado() == Pedido.Estado.CANCELADO) return PedidoResponse.from(pedido);
        if (pedido.getEstado() != Pedido.Estado.PENDIENTE) {
            throw new BadRequestException("Solo se pueden cancelar pedidos pendientes; un pago requiere devolucion explicita");
        }
        String id = pedido.getId();
        for (DetallePedido detalle : detallePedidoRepository.findByPedidoId(id)) {
            Producto producto = productoRepository.findByIdForUpdate(detalle.getProducto().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }

        pedido.setEstado(Pedido.Estado.CANCELADO);
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse actualizarPrecioAcordado(String pedidoId, String detalleId,
                                                   BigDecimal precioAcordado) {
        if (precioAcordado == null || precioAcordado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El precio acordado debe ser mayor a cero");
        }

        validarImporte(precioAcordado);
        Pedido pedido = pedidoRepository.findByIdForUpdate(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        if (pedido.getEstado() != Pedido.Estado.PENDIENTE) {
            throw new BadRequestException("Solo se pueden modificar precios de pedidos pendientes");
        }
        if (pedido.getMercadoPagoOrderId() != null) {
            throw new BadRequestException("El total del pedido esta reservado para Mercado Pago");
        }
        DetallePedido detalle = detallePedidoRepository.findById(detalleId)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado"));
        if (!detalle.getPedido().getId().equals(pedidoId)) {
            throw new UnauthorizedException("El detalle no pertenece a este pedido");
        }
        validarPrecioAcordado(detalle.getProducto(), precioAcordado);
        detalle.setPrecioAcordado(precioAcordado);
        BigDecimal subtotal = precioAcordado.multiply(BigDecimal.valueOf(detalle.getCantidad()));
        validarImporte(subtotal);
        detalle.setSubtotal(subtotal);
        detallePedidoRepository.save(detalle);

        BigDecimal nuevoTotal = detallePedidoRepository.findByPedidoId(pedidoId)
                .stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(pedido.getCostoEnvio() != null ? pedido.getCostoEnvio() : BigDecimal.ZERO);
        validarImporte(nuevoTotal);
        pedido.setTotal(nuevoTotal);
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    private Map<String, Integer> agruparCantidades(List<DetallePedidoRequest> detalles) {
        if (detalles == null || detalles.isEmpty()) throw new BadRequestException("El pedido debe tener productos");
        Map<String, Integer> cantidades = new java.util.TreeMap<>();
        for (DetallePedidoRequest detalle : detalles) {
            if (detalle == null || detalle.getProductoId() == null || detalle.getProductoId().isBlank()
                    || detalle.getCantidad() == null || detalle.getCantidad() < 1) {
                throw new BadRequestException("Cada detalle debe tener un producto y una cantidad positiva");
            }
            try {
                cantidades.merge(detalle.getProductoId(), detalle.getCantidad(), Math::addExact);
            } catch (ArithmeticException ex) {
                throw new BadRequestException("La cantidad acumulada supera el limite permitido");
            }
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

    private void validarImporte(BigDecimal importe) {
        if (importe == null || importe.signum() <= 0 || importe.scale() > 2
                || importe.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new BadRequestException("El importe debe ser positivo, no superar 99999999.99 y tener hasta 2 decimales");
        }
    }

    private void validarPrecioAcordado(Producto producto, BigDecimal precioAcordado) {
        validarImporte(precioAcordado);
        BigDecimal precioMinimo = producto.getPrecioMinimoVenta() != null
                ? producto.getPrecioMinimoVenta()
                : producto.getPrecio();

        if (precioAcordado.compareTo(precioMinimo) < 0) {
            throw new BadRequestException(
                    "El precio acordado para " + producto.getNombre()
                            + " no puede ser menor a S/ " + precioMinimo
            );
        }
    }
}
