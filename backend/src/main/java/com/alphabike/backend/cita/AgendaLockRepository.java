package com.alphabike.backend.cita;
import org.springframework.data.jpa.repository.*;
public interface AgendaLockRepository extends JpaRepository<AgendaLock, Integer> {
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AgendaLock a where a.id = 1")
    java.util.Optional<AgendaLock> bloquear();
}
