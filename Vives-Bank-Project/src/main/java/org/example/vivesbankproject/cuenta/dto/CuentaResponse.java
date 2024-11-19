package org.example.vivesbankproject.cuenta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.vivesbankproject.cliente.models.Cliente;
import org.example.vivesbankproject.cuenta.models.TipoCuenta;
import org.example.vivesbankproject.tarjeta.models.Tarjeta;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaResponse {
    private UUID id;
    private String iban;
    private Double saldo;
    private Cliente cliente;
    private TipoCuenta tipoCuenta;
    private Tarjeta tarjeta;
}