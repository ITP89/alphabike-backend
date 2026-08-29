package com.alphabike.backend.shared.validation;

import com.alphabike.backend.pago.Pago;
import com.alphabike.backend.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnumUtilsTest {

    @Test
    void parseAceptaValoresSinImportarMayusculas() {
        Pago.MetodoPago metodoPago = EnumUtils.parse(Pago.MetodoPago.class, "yape", "metodoPago");

        assertThat(metodoPago).isEqualTo(Pago.MetodoPago.YAPE);
    }

    @Test
    void parseRechazaValoresInvalidosConMensajeControlado() {
        assertThatThrownBy(() -> EnumUtils.parse(Pago.ReferenciaTipo.class, "VENTA", "referenciaTipo"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Valores permitidos: PEDIDO, COTIZACION");
    }
}
