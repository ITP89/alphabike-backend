package com.alphabike.backend.cita;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="agenda_lock", schema="taller") @Getter @Setter @NoArgsConstructor
public class AgendaLock { @Id private Integer id; }
