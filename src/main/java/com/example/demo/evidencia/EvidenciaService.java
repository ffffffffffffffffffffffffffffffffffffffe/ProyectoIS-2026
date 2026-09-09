package com.example.demo.evidencia;

import com.example.demo.portafolio.Portafolio;
import com.example.demo.portafolio.PortafolioRepository;
import com.example.demo.usuario.Usuario;
import com.example.demo.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;
import java.util.List;

@Service
@PreAuthorize("hasRole('ESTUDIANTE')")
public class EvidenciaService {

    private static final Logger log =
            LoggerFactory.getLogger(EvidenciaService.class);

    private final EvidenciaRepository evidenciaRepository;
    private final PortafolioRepository portafolioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlmacenamientoService almacenamientoService;

    public EvidenciaService(
            EvidenciaRepository evidenciaRepository,
            PortafolioRepository portafolioRepository,
            UsuarioRepository usuarioRepository,
            AlmacenamientoService almacenamientoService) {
        this.evidenciaRepository = evidenciaRepository;
        this.portafolioRepository = portafolioRepository;
        this.usuarioRepository = usuarioRepository;
        this.almacenamientoService = almacenamientoService;
    }

    @Transactional
    public Long subir(
            Long portafolioId,
            String titulo,
            String descripcion,
            String actividad,
            MultipartFile archivo) {

        Usuario estudiante = obtenerEstudianteActual();
        Portafolio portafolio = buscarPortafolioPropio(
                portafolioId, estudiante.getId()
        );

        String tituloValidado = validarTexto(
                titulo, "El título", 150, true
        );

        String descripcionValidada = validarTexto(
                descripcion, "La descripción", 1000, false
        );

        String actividadValidada = validarTexto(
                actividad, "La actividad", 150, true
        );

        var guardado = almacenamientoService.guardarPdf(archivo);

        registrarLimpiezaSiSeRevierte(guardado.clave());

        Evidencia evidencia = new Evidencia(
                portafolio,
                estudiante,
                tituloValidado,
                descripcionValidada,
                actividadValidada,
                guardado.nombreOriginal(),
                guardado.clave(),
                guardado.tipoContenido(),
                guardado.tamanoBytes()
        );

        return evidenciaRepository.saveAndFlush(evidencia).getId();
    }

    @Transactional(readOnly = true)
    public List<EvidenciaResumen> listarPropias(Long portafolioId) {
        Usuario estudiante = obtenerEstudianteActual();

        buscarPortafolioPropio(portafolioId, estudiante.getId());

        return evidenciaRepository
                .findByPortafolio_IdOrderByFechaSubidaDesc(portafolioId)
                .stream()
                .map(evidencia -> new EvidenciaResumen(
                        evidencia.getId(),
                        evidencia.getTitulo(),
                        evidencia.getDescripcion(),
                        evidencia.getActividad(),
                        evidencia.getNombreOriginal(),
                        evidencia.getTamanoBytes(),
                        evidencia.getFechaSubida()
                ))
                .toList();
    }

    private Usuario obtenerEstudianteActual() {
        String correo = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "No se encontró una cuenta válida."
                        )
                );

        boolean esEstudiante = usuario.getRoles().stream()
                .anyMatch(rol ->
                        rol.getNombre().equals("ESTUDIANTE")
                );

        if (!usuario.isActivo() || !esEstudiante) {
            throw new AccessDeniedException(
                    "Necesitas una cuenta activa con rol ESTUDIANTE."
            );
        }

        return usuario;
    }

    private Portafolio buscarPortafolioPropio(
            Long portafolioId,
            Long estudianteId) {

        if (portafolioId == null || portafolioId <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del portafolio no es válido."
            );
        }

        return portafolioRepository
                .findByIdAndEstudiante_Id(portafolioId, estudianteId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "El portafolio no está disponible para tu cuenta."
                        )
                );
    }

    private String validarTexto(
            String valor,
            String campo,
            int maximo,
            boolean obligatorio) {

        if (valor == null || valor.isBlank()) {
            if (obligatorio) {
                throw new IllegalArgumentException(
                        campo + " es obligatorio."
                );
            }
            return null;
        }

        String resultado = valor.strip();

        if (resultado.length() > maximo) {
            throw new IllegalArgumentException(
                    campo + " admite hasta " + maximo + " caracteres."
            );
        }

        return resultado;
    }

    private void registrarLimpiezaSiSeRevierte(String clave) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            try {
                                almacenamientoService.eliminar(clave);
                            } catch (RuntimeException exception) {
                                log.error(
                                        "No se pudo limpiar el archivo {} "
                                                + "tras revertir la transacción.",
                                        clave,
                                        exception
                                );
                            }
                        } else if (status == STATUS_UNKNOWN) {
                            log.error(
                                    "Resultado de transacción desconocido. "
                                            + "Revisar el archivo {} y su registro.",
                                    clave
                            );
                        }
                    }
                }
        );
    }

    public record EvidenciaResumen(
            Long id,
            String titulo,
            String descripcion,
            String actividad,
            String nombreOriginal,
            long tamanoBytes,
            LocalDateTime fechaSubida) {
    }

    @Transactional(readOnly = true)
    public DescargaEvidencia descargarPropia(Long evidenciaId) {
        Usuario estudiante = obtenerEstudianteActual();

        if (evidenciaId == null || evidenciaId <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la evidencia no es válido."
            );
        }

        Evidencia evidencia = evidenciaRepository
                .findByIdAndPortafolio_Estudiante_Id(
                        evidenciaId,
                        estudiante.getId()
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "La evidencia no está disponible para tu cuenta."
                        )
                );

        Resource recurso = almacenamientoService.cargar(
                evidencia.getClaveAlmacenamiento()
        );

        return new DescargaEvidencia(
                recurso,
                evidencia.getNombreOriginal()
        );
    }

    public record DescargaEvidencia(
            Resource recurso,
            String nombreOriginal) {
    }
}