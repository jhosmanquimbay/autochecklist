package com.example.demostracion.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demostracion.model.Inventario;
import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.InventarioRepository;
import com.example.demostracion.repository.VehiculoRepository;

@Service
public class VehiculoPublicacionService {

    private final VehiculoRepository vehiculoRepository;
    private final InventarioRepository inventarioRepository;

    public VehiculoPublicacionService(VehiculoRepository vehiculoRepository,
                                      InventarioRepository inventarioRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
    }

    public List<Inventario> listarInventariosSeleccionables() {
        return inventarioRepository.findByActivoTrue().stream()
                .sorted(Comparator.comparing((Inventario inventario) -> valorOrden(inventario.getMarca()))
                        .thenComparing(inventario -> valorOrden(inventario.getModelo()))
                        .thenComparing(inventario -> valorOrden(inventario.getChasis())))
                .toList();
    }

    public List<Vehiculo> listarVehiculos(String estadoPublicacion) {
        String filtro = normalizarFiltro(estadoPublicacion);
        if ("todos".equals(filtro)) {
            return vehiculoRepository.findAllByOrderByFechaCreacionDesc();
        }
        return vehiculoRepository.findByEstadoPublicacionIgnoreCaseOrderByFechaCreacionDesc(filtro);
    }

    public long contarPorEstado(String estadoPublicacion) {
        return vehiculoRepository.countByEstadoPublicacionIgnoreCase(normalizarEstadoPublicacion(estadoPublicacion));
    }

    public Vehiculo obtenerVehiculo(Long idVehiculo) {
        return vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
    }

    public Vehiculo prepararNuevoVehiculoGerencia() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setEstadoPublicacion(Vehiculo.ESTADO_PUBLICACION_PENDIENTE);
        return vehiculo;
    }

    public Vehiculo prepararNuevoVehiculoGerencia(Long inventarioId) {
        Vehiculo vehiculo = prepararNuevoVehiculoGerencia();
        aplicarDatosInventarioSeleccionado(vehiculo, inventarioId, true);
        return vehiculo;
    }

    public Vehiculo prepararNuevoVehiculoAdmin() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setEstadoPublicacion(Vehiculo.ESTADO_PUBLICACION_PUBLICADO);
        vehiculo.setFechaPublicacion(LocalDateTime.now());
        return vehiculo;
    }

    public Vehiculo prepararNuevoVehiculoAdmin(Long inventarioId) {
        Vehiculo vehiculo = prepararNuevoVehiculoAdmin();
        aplicarDatosInventarioSeleccionado(vehiculo, inventarioId, true);
        return vehiculo;
    }

    public Vehiculo prepararVehiculoExistenteParaFormulario(Vehiculo vehiculo) {
        if (vehiculo == null) {
            return null;
        }
        if (vehiculo.getInventarioSeleccionadoId() == null) {
            String chasis = normalizarTextoOpcional(vehiculo.getChasis());
            if (chasis != null) {
                inventarioRepository.findByChasis(chasis).ifPresent(inventario -> vehiculo.setInventarioSeleccionadoId(inventario.getIdInventario()));
            }
        }
        return vehiculo;
    }

    @Transactional
    public Vehiculo crearDesdeGerencia(Vehiculo vehiculo,
                                       MultipartFile imagenFile,
                                       MultipartFile imagenFile2,
                                       MultipartFile imagenFile3,
                                       MultipartFile imagenFile4) throws IOException {
        Vehiculo nuevo = new Vehiculo();
        aplicarDatosFormulario(nuevo, vehiculo, false);
        validarVehiculo(nuevo, null);
        procesarImagenes(nuevo, imagenFile, imagenFile2, imagenFile3, imagenFile4, null);
        nuevo.setEstadoPublicacion(Vehiculo.ESTADO_PUBLICACION_PENDIENTE);
        nuevo.setFechaPublicacion(null);
        return vehiculoRepository.save(nuevo);
    }

    @Transactional
    public Vehiculo actualizarDesdeGerencia(Long idVehiculo,
                                            Vehiculo vehiculo,
                                            MultipartFile imagenFile,
                                            MultipartFile imagenFile2,
                                            MultipartFile imagenFile3,
                                            MultipartFile imagenFile4) throws IOException {
        Vehiculo existente = obtenerVehiculo(idVehiculo);
        aplicarDatosFormulario(existente, vehiculo, true);
        validarVehiculo(existente, idVehiculo);
        procesarImagenes(existente, imagenFile, imagenFile2, imagenFile3, imagenFile4, existente);
        existente.setEstadoPublicacion(Vehiculo.ESTADO_PUBLICACION_PENDIENTE);
        existente.setFechaPublicacion(null);
        return vehiculoRepository.save(existente);
    }

    @Transactional
    public Vehiculo crearDesdeAdmin(Vehiculo vehiculo,
                                    MultipartFile imagenFile,
                                    MultipartFile imagenFile2,
                                    MultipartFile imagenFile3,
                                    MultipartFile imagenFile4) throws IOException {
        Vehiculo nuevo = new Vehiculo();
        aplicarDatosFormulario(nuevo, vehiculo, false);
        validarVehiculo(nuevo, null);
        procesarImagenes(nuevo, imagenFile, imagenFile2, imagenFile3, imagenFile4, null);
        nuevo.setEstadoPublicacion(Vehiculo.ESTADO_PUBLICACION_PUBLICADO);
        nuevo.setFechaPublicacion(LocalDateTime.now());
        return vehiculoRepository.save(nuevo);
    }

    @Transactional
    public Vehiculo actualizarDesdeAdmin(Long idVehiculo,
                                         Vehiculo vehiculo,
                                         MultipartFile imagenFile,
                                         MultipartFile imagenFile2,
                                         MultipartFile imagenFile3,
                                         MultipartFile imagenFile4) throws IOException {
        Vehiculo existente = obtenerVehiculo(idVehiculo);
        aplicarDatosFormulario(existente, vehiculo, true);
        validarVehiculo(existente, idVehiculo);
        procesarImagenes(existente, imagenFile, imagenFile2, imagenFile3, imagenFile4, existente);
        if (existente.estaPublicado() && existente.getFechaPublicacion() == null) {
            existente.setFechaPublicacion(LocalDateTime.now());
        }
        return vehiculoRepository.save(existente);
    }

    @Transactional
    public Vehiculo publicar(Long idVehiculo) {
        Vehiculo vehiculo = obtenerVehiculo(idVehiculo);
        vehiculo.setEstadoPublicacion(Vehiculo.ESTADO_PUBLICACION_PUBLICADO);
        vehiculo.setFechaPublicacion(LocalDateTime.now());
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public Vehiculo devolverAGerencia(Long idVehiculo) {
        Vehiculo vehiculo = obtenerVehiculo(idVehiculo);
        vehiculo.setEstadoPublicacion(Vehiculo.ESTADO_PUBLICACION_DEVUELTO);
        vehiculo.setFechaPublicacion(null);
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public Vehiculo despublicar(Long idVehiculo) {
        Vehiculo vehiculo = obtenerVehiculo(idVehiculo);
        vehiculo.setEstadoPublicacion(Vehiculo.ESTADO_PUBLICACION_DESPUBLICADO);
        vehiculo.setFechaPublicacion(null);
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public Vehiculo moverABorrador(Long idVehiculo) {
        Vehiculo vehiculo = obtenerVehiculo(idVehiculo);
        vehiculo.setEstadoPublicacion(Vehiculo.ESTADO_PUBLICACION_BORRADOR);
        vehiculo.setFechaPublicacion(null);
        return vehiculoRepository.save(vehiculo);
    }

    public String normalizarFiltro(String estadoPublicacion) {
        if (estadoPublicacion == null || estadoPublicacion.isBlank()) {
            return "todos";
        }
        String valor = normalizarEstadoPublicacion(estadoPublicacion);
        return switch (valor) {
            case Vehiculo.ESTADO_PUBLICACION_BORRADOR,
                 Vehiculo.ESTADO_PUBLICACION_PENDIENTE,
                 Vehiculo.ESTADO_PUBLICACION_PUBLICADO,
                 Vehiculo.ESTADO_PUBLICACION_DEVUELTO,
                 Vehiculo.ESTADO_PUBLICACION_DESPUBLICADO -> valor;
            default -> "todos";
        };
    }

    private void aplicarDatosFormulario(Vehiculo destino, Vehiculo origen, boolean preservarDatosExistentes) {
        aplicarDatosInventarioSeleccionado(origen, origen.getInventarioSeleccionadoId(), true);

        destino.setInventarioSeleccionadoId(origen.getInventarioSeleccionadoId());
        destino.setChasis(normalizarChasis(origen.getChasis(), preservarDatosExistentes ? destino.getChasis() : null));
        destino.setMarca(normalizarTexto(origen.getMarca(), preservarDatosExistentes ? destino.getMarca() : null));
        destino.setModelo(normalizarTexto(origen.getModelo(), preservarDatosExistentes ? destino.getModelo() : null));
        destino.setAnio(origen.getAnio() != null ? origen.getAnio() : (preservarDatosExistentes ? destino.getAnio() : null));
        destino.setPrecio(origen.getPrecio());
        destino.setCilindrada(normalizarTexto(origen.getCilindrada(), preservarDatosExistentes ? destino.getCilindrada() : null));
        destino.setTipoCombustible(normalizarTexto(origen.getTipoCombustible(), preservarDatosExistentes ? destino.getTipoCombustible() : null));
        destino.setTransmision(normalizarTexto(origen.getTransmision(), preservarDatosExistentes ? destino.getTransmision() : null));
        destino.setDescripcion(normalizarTexto(origen.getDescripcion(), preservarDatosExistentes ? destino.getDescripcion() : null));
        destino.setEspecificacionesTecnicas(normalizarTexto(origen.getEspecificacionesTecnicas(), preservarDatosExistentes ? destino.getEspecificacionesTecnicas() : null));
        destino.setImagenUrl(normalizarTexto(origen.getImagenUrl(), preservarDatosExistentes ? destino.getImagenUrl() : null));

        if (origen.getPopularidad() != null) {
            destino.setPopularidad(origen.getPopularidad());
        } else if (!preservarDatosExistentes && destino.getPopularidad() == null) {
            destino.setPopularidad(0);
        }
    }

    private void procesarImagenes(Vehiculo vehiculo,
                                  MultipartFile imagenFile,
                                  MultipartFile imagenFile2,
                                  MultipartFile imagenFile3,
                                  MultipartFile imagenFile4,
                                  Vehiculo existente) throws IOException {
        vehiculo.setImagen(leerImagen(imagenFile, existente != null ? existente.getImagen() : null));
        vehiculo.setImagen2(leerImagen(imagenFile2, existente != null ? existente.getImagen2() : null));
        vehiculo.setImagen3(leerImagen(imagenFile3, existente != null ? existente.getImagen3() : null));
        vehiculo.setImagen4(leerImagen(imagenFile4, existente != null ? existente.getImagen4() : null));

        if (existente != null) {
            vehiculo.setFechaCreacion(existente.getFechaCreacion());
            vehiculo.setActivo(existente.isActivo());
            vehiculo.setEstadoPublicacion(existente.getEstadoPublicacion());
            vehiculo.setFechaPublicacion(existente.getFechaPublicacion());
            if (vehiculo.getPopularidad() == null) {
                vehiculo.setPopularidad(existente.getPopularidad());
            }
        }
    }

    private byte[] leerImagen(MultipartFile archivo, byte[] actual) throws IOException {
        if (archivo != null && !archivo.isEmpty()) {
            return archivo.getBytes();
        }
        return actual;
    }

    private void validarVehiculo(Vehiculo vehiculo, Long idVehiculoActual) {
        String chasis = normalizarTextoRequerido(vehiculo.getChasis(), "El chasis es obligatorio.");
        if (contieneEspaciosEnBlanco(chasis)) {
            throw new IllegalArgumentException("El chasis no puede contener espacios en blanco.");
        }

        vehiculo.setChasis(chasis.toUpperCase(Locale.ROOT));
        vehiculo.setMarca(normalizarTextoRequerido(vehiculo.getMarca(), "La marca es obligatoria."));
        vehiculo.setModelo(normalizarTextoRequerido(vehiculo.getModelo(), "El modelo es obligatorio."));
        vehiculo.setCilindrada(normalizarTextoOpcional(vehiculo.getCilindrada()));
        vehiculo.setTipoCombustible(normalizarTextoOpcional(vehiculo.getTipoCombustible()));
        vehiculo.setTransmision(normalizarTextoOpcional(vehiculo.getTransmision()));
        vehiculo.setDescripcion(normalizarTextoOpcional(vehiculo.getDescripcion()));
        vehiculo.setEspecificacionesTecnicas(normalizarTextoOpcional(vehiculo.getEspecificacionesTecnicas()));
        vehiculo.setImagenUrl(normalizarTextoOpcional(vehiculo.getImagenUrl()));

        boolean duplicado = idVehiculoActual == null
                ? vehiculoRepository.existsByChasisIgnoreCase(vehiculo.getChasis())
                : vehiculoRepository.existsByChasisIgnoreCaseAndIdVehiculoNot(vehiculo.getChasis(), idVehiculoActual);

        if (duplicado) {
            throw new IllegalArgumentException("Ya existe un vehículo con el chasis '" + vehiculo.getChasis() + "'.");
        }
    }

    private void aplicarDatosInventarioSeleccionado(Vehiculo vehiculo, Long inventarioId, boolean completarSoloVacios) {
        if (vehiculo == null || inventarioId == null) {
            return;
        }

        inventarioRepository.findById(inventarioId).ifPresent(inventario -> {
            vehiculo.setInventarioSeleccionadoId(inventario.getIdInventario());
            vehiculo.setInventario(inventario);

            if (!completarSoloVacios || valorVacio(vehiculo.getChasis())) {
                vehiculo.setChasis(inventario.getChasis());
            }
            if (!completarSoloVacios || valorVacio(vehiculo.getMarca())) {
                vehiculo.setMarca(inventario.getMarca());
            }
            if (!completarSoloVacios || valorVacio(vehiculo.getModelo())) {
                vehiculo.setModelo(inventario.getModelo());
            }
            if ((!completarSoloVacios || vehiculo.getAnio() == null) && inventario.getAnio() != null) {
                vehiculo.setAnio(inventario.getAnio());
            }
            if (!completarSoloVacios || valorVacio(vehiculo.getCilindrada())) {
                vehiculo.setCilindrada(inventario.getMotor());
            }
        });
    }

    private String normalizarEstadoPublicacion(String estadoPublicacion) {
        if (estadoPublicacion == null || estadoPublicacion.isBlank()) {
            return Vehiculo.ESTADO_PUBLICACION_BORRADOR;
        }
        return estadoPublicacion.trim().toUpperCase(Locale.ROOT)
                .replace('Á', 'A')
                .replace('É', 'E')
                .replace('Í', 'I')
                .replace('Ó', 'O')
                .replace('Ú', 'U');
    }

    private String normalizarTexto(String valor, String valorPorDefecto) {
        String normalizado = normalizarTextoOpcional(valor);
        return normalizado == null ? valorPorDefecto : normalizado;
    }

    private String normalizarChasis(String chasis, String actual) {
        String valor = normalizarTexto(chasis, actual);
        if (valor == null || valor.isBlank()) {
            return "AUTO-" + System.currentTimeMillis();
        }
        return valor;
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isBlank() ? null : normalizado;
    }

    private String normalizarTextoRequerido(String valor, String mensajeError) {
        String normalizado = normalizarTextoOpcional(valor);
        if (normalizado == null) {
            throw new IllegalArgumentException(mensajeError);
        }
        return normalizado;
    }

    private boolean valorVacio(String valor) {
        return normalizarTextoOpcional(valor) == null;
    }

    private boolean contieneEspaciosEnBlanco(String valor) {
        for (int i = 0; i < valor.length(); i++) {
            if (Character.isWhitespace(valor.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private String valorOrden(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }
}
