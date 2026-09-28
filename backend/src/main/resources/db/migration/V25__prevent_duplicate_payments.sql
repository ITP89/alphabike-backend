-- Un pago confirmado por referencia. Evita cobros duplicados incluso bajo concurrencia.
CREATE UNIQUE INDEX IF NOT EXISTS uq_pagos_referencia_pagada
    ON pagos.pagos (referencia_tipo, referencia_id)
    WHERE estado = 'PAGADO';
