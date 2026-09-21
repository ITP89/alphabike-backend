package com.alphabike.backend.proveedor;

import com.alphabike.backend.proveedor.dto.*;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import com.alphabike.backend.shared.validation.EnumUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public List<ProveedorResponse> listar() {
        return proveedorRepository.findAll()
                .stream()
                .map(ProveedorResponse::from)
                .toList();
    }

    public ProveedorResponse obtener(String id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
        return ProveedorResponse.from(proveedor);
    }

    public ProveedorResponse crear(ProveedorRequest request) {
        String ruc = normalizar(request.getRuc());
        if (proveedorRepository.findByRuc(ruc).isPresent()) {
            throw new BadRequestException("Ya existe un proveedor con ese RUC");
        }

        Proveedor proveedor = Proveedor.builder()
                .nombre(request.getNombre())
                .ruc(ruc)
                .telefono(request.getTelefono())
                .email(normalizar(request.getEmail()))
                .direccion(request.getDireccion())
                .contactoPrincipal(request.getContactoPrincipal())
                .estado(Proveedor.Estado.ACTIVO)
                .build();

        return ProveedorResponse.from(proveedorRepository.save(proveedor));
    }

    public ProveedorResponse actualizar(String id, ProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
        String ruc = normalizar(request.getRuc());

        proveedorRepository.findByRuc(ruc)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Ya existe un proveedor con ese RUC");
                });

        proveedor.setNombre(request.getNombre());
        proveedor.setRuc(ruc);
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(normalizar(request.getEmail()));
        proveedor.setDireccion(request.getDireccion());
        proveedor.setContactoPrincipal(request.getContactoPrincipal());

        return ProveedorResponse.from(proveedorRepository.save(proveedor));
    }

    public ProveedorResponse cambiarEstado(String id, String estado) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
        proveedor.setEstado(EnumUtils.parse(Proveedor.Estado.class, estado, "estado"));
        return ProveedorResponse.from(proveedorRepository.save(proveedor));
    }

    public void inactivar(String id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
        proveedor.setEstado(Proveedor.Estado.INACTIVO);
        proveedorRepository.save(proveedor);
    }

    private String normalizar(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
