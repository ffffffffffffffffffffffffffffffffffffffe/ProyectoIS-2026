package com.example.demo.evidencia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class AlmacenamientoService {

    private static final long MAX_BYTES = 10L * 1024 * 1024;

    private final Path directorio;

    public AlmacenamientoService(
            @Value("${app.evidencias.directorio}") String directorio) {
        this.directorio = Path.of(directorio)
                .toAbsolutePath()
                .normalize();
    }

    public ArchivoGuardado guardarPdf(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un archivo PDF con contenido."
            );
        }

        if (archivo.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException(
                    "El archivo no puede superar 10 MB."
            );
        }

        String nombreOriginal = obtenerNombre(archivo);
        String clave = UUID.randomUUID() + ".pdf";
        Path destino = directorio.resolve(clave);

        boolean creado = false;

        try {
            Files.createDirectories(directorio);

            try (InputStream entrada = archivo.getInputStream()) {
                byte[] cabecera = entrada.readNBytes(5);

                String firma = new String(
                        cabecera,
                        StandardCharsets.US_ASCII
                );

                if (!"%PDF-".equals(firma)) {
                    throw new IllegalArgumentException(
                            "El archivo no contiene una cabecera PDF válida."
                    );
                }

                long total = cabecera.length;

                try (OutputStream salida = Files.newOutputStream(
                        destino,
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE)) {

                    creado = true;
                    salida.write(cabecera);

                    byte[] buffer = new byte[8192];
                    int leidos;

                    while ((leidos = entrada.read(buffer)) != -1) {
                        total += leidos;

                        if (total > MAX_BYTES) {
                            throw new IllegalArgumentException(
                                    "El archivo no puede superar 10 MB."
                            );
                        }

                        salida.write(buffer, 0, leidos);
                    }
                }

                return new ArchivoGuardado(
                        clave,
                        nombreOriginal,
                        "application/pdf",
                        total
                );
            }
        } catch (IOException | RuntimeException exception) {
            if (creado) {
                try {
                    Files.deleteIfExists(destino);
                } catch (IOException limpieza) {
                    exception.addSuppressed(limpieza);
                }
            }

            if (exception instanceof IllegalArgumentException validacion) {
                throw validacion;
            }

            throw new IllegalStateException(
                    "No se pudo guardar el archivo.",
                    exception
            );
        }
    }

    public void eliminar(String clave) {
        Path archivo = resolverClave(clave);

        try {
            Files.deleteIfExists(archivo);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "No se pudo eliminar el archivo.",
                    exception
            );
        }
    }

    private Path resolverClave(String clave) {
        if (clave == null || !clave.matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-"
                        + "[0-9a-f]{4}-[0-9a-f]{12}\\.pdf")) {
            throw new IllegalArgumentException(
                    "La clave de almacenamiento no es válida."
            );
        }

        Path resultado = directorio.resolve(clave).normalize();

        if (!resultado.startsWith(directorio)) {
            throw new IllegalArgumentException(
                    "La ruta del archivo no es válida."
            );
        }

        return resultado;
    }

    private String obtenerNombre(MultipartFile archivo) {
        String nombre = archivo.getOriginalFilename();

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El archivo debe tener un nombre."
            );
        }

        nombre = nombre.replace('\\', '/');
        nombre = nombre.substring(nombre.lastIndexOf('/') + 1).strip();

        if (nombre.length() > 255
                || nombre.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(
                    "El nombre del archivo no es válido."
            );
        }

        if (!nombre.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException(
                    "Solo se permiten archivos con extensión .pdf."
            );
        }

        return nombre;
    }

    public record ArchivoGuardado(
            String clave,
            String nombreOriginal,
            String tipoContenido,
            long tamanoBytes) {
    }

    public Resource cargar(String clave) {
        Path ruta = resolverClave(clave);

        try {
            Resource recurso = new UrlResource(ruta.toUri());

            if (!Files.isRegularFile(ruta) || !recurso.isReadable()) {
                throw new IllegalStateException(
                        "El archivo no está disponible en el almacenamiento."
                );
            }

            return recurso;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "No se pudo acceder al archivo.",
                    exception
            );
        }
    }
}