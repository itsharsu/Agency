package com.example.Agency.model;


import com.example.Agency.util.UserIdSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payments {
    @Id
    private String paymentId = UUID.randomUUID().toString();

    // Add relationship
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    @JsonSerialize(using = UserIdSerializer.class)
    private User user;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = true)
    private Double amountPaid;

    @Column(name = "recived_by")
    private String receivedBy;
}