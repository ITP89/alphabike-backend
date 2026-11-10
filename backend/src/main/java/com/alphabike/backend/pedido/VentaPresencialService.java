package com.alphabike.backend.pedido;

import com.alphabike.backend.pedido.dto.*;
import com.alphabike.backend.pago.PagoService;
import com.alphabike.backend.pago.dto.PagoRequest;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class VentaPresencialService {
    private final PedidoService pedidos;
    private final PedidoRepository repository;
    private final PagoService pagos;
    private final UsuarioRepository usuarios;
    private final ObjectMapper mapper;

    @Transactional
    public PedidoResponse registrar(VentaPresencialRequest request, String email) {
        // Serialize retries by operator, including the first attempt before an order exists.
        Usuario usuario = usuarios.findByEmailForUpdate(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (usuario.getRol() != Usuario.Rol.ADMIN && usuario.getRol() != Usuario.Rol.ENCARGADO) {
            throw new BadRequestException("Solo el personal puede registrar una venta presencial");
        }
        request.setOperacionId(java.util.UUID.fromString(request.getOperacionId()).toString());
        request.setMetodoPago(com.alphabike.backend.shared.validation.EnumUtils.parse(
                com.alphabike.backend.pago.Pago.MetodoPago.class, request.getMetodoPago(), "metodoPago").name());
        String hash = fingerprint(request);
        var existing = repository.findByOperacionId(request.getOperacionId());
        if (existing.isPresent()) {
            Pedido pedido = existing.get();
            if (!pedido.getCliente().getId().equals(usuario.getId()) || !hash.equals(pedido.getSolicitudHash())) {
                throw new BadRequestException("El identificador de operacion ya corresponde a otra solicitud");
            }
            return pedidos.obtener(pedido.getId(), email);
        }
        PedidoResponse creado = pedidos.crear(new PedidoRequest("RECOJO_TIENDA", null, request.getDetalles()), email);
        Pedido pedido = repository.findById(creado.getId()).orElseThrow();
        pedido.setOperacionId(request.getOperacionId());
        pedido.setSolicitudHash(hash);
        repository.save(pedido);
        pagos.registrar(new PagoRequest("PEDIDO", pedido.getId(), creado.getTotal(), request.getMetodoPago(),
                null, null, null, null), email);
        return pedidos.obtener(pedido.getId(), email);
    }

    private String fingerprint(VentaPresencialRequest request) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(mapper.writeValueAsBytes(request)));
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No se pudo identificar la solicitud de venta", ex);
        }
    }
}
