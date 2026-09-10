package com.example.demo.usuario;

import java.util.Locale;

public final class RutValidator {

    private RutValidator() {
    }

    public static String normalizarYValidar(String entrada) {
        if (entrada == null || entrada.isBlank()) {
            throw new IllegalArgumentException("Debes ingresar un RUT.");
        }

        String valor = entrada.strip().toUpperCase(Locale.ROOT);

        // Admite 12.345.678-5, 12345678-5 o 123456785.
        boolean formatoValido =
                valor.matches("[0-9]{1,8}-?[0-9K]")
                        || valor.matches("[0-9]{1,2}\\.[0-9]{3}\\.[0-9]{3}-[0-9K]");

        if (!formatoValido) {
            throw new IllegalArgumentException(
                    "Ingresa un RUT válido, por ejemplo 12.345.678-5."
            );
        }

        String compacto = valor.replace(".", "").replace("-", "");
        String cuerpo = compacto.substring(0, compacto.length() - 1);
        char verificador = compacto.charAt(compacto.length() - 1);

        // Evita almacenar el mismo RUT con distintos ceros iniciales.
        cuerpo = cuerpo.replaceFirst("^0+(?!$)", "");

        if (cuerpo.equals("0")) {
            throw new IllegalArgumentException("El RUT no es válido.");
        }

        int suma = 0;
        int factor = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }

        int resultado = 11 - (suma % 11);

        char esperado = switch (resultado) {
            case 11 -> '0';
            case 10 -> 'K';
            default -> (char) ('0' + resultado);
        };

        if (verificador != esperado) {
            throw new IllegalArgumentException(
                    "El dígito verificador del RUT no es correcto."
            );
        }

        return cuerpo + "-" + verificador;
    }
}