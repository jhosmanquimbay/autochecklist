package com.example.demostracion.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demostracion.dto.ClientePerfilForm;
import com.example.demostracion.dto.ClienteRegistroForm;
import com.example.demostracion.dto.SolicitudInteresForm;
import com.example.demostracion.model.Cliente;
import com.example.demostracion.model.FavoritoVehiculo;
import com.example.demostracion.model.Pedido;
import com.example.demostracion.model.Rol;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.ClienteRepository;
import com.example.demostracion.repository.FavoritoVehiculoRepository;
import com.example.demostracion.repository.PedidoRepository;
import com.example.demostracion.repository.RolRepository;
import com.example.demostracion.repository.UsuarioRepository;
import com.example.demostracion.repository.VehiculoRepository;

@Service
public class ClienteCuentaService {

    private static final String ROL_CLIENTE = "CLIENTE";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final FavoritoVehiculoRepository favoritoVehiculoRepository;
    private final PedidoRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteCuentaService(UsuarioRepository usuarioRepository,
                                RolRepository rolRepository,
                                ClienteRepository clienteRepository,
                                VehiculoRepository vehiculoRepository,
                                FavoritoVehiculoRepository favoritoVehiculoRepository,
                                PedidoRepository pedidoRepository,
                                PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.clienteRepository = clienteRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.favoritoVehiculoRepository = favoritoVehiculoRepository;
        this.pedidoRepository = pedidoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean correoPerteneceAOtroUsuario(String correo, Long usuarioActualId) {
        if (correo == null || correo.isBlank()) {
            return false;
        }
        return usuarioRepository.findByCorreo(normalizarCorreo(correo))
                .filter(usuario -> usuarioActualId == null || !usuario.getIdUsuario().equals(usuarioActualId))
                .isPresent();
    }

    public boolean cedulaPerteneceAOtroUsuario(String cedula, Long usuarioActualId) {
        if (cedula == null || cedula.isBlank()) {
            return false;
        }
        return usuarioRepository.findByCedula(normalizarTexto(cedula))
                .filter(usuario -> usuarioActualId == null || !usuario.getIdUsuario().equals(usuarioActualId))
                .isPresent();
    }

    @Transactional
    public Usuario registrarCliente(ClienteRegistroForm form) {
        Usuario usuario = new Usuario();
        mapearDatosRegistro(usuario, form);
        usuario.setContrasena(passwordEncoder.encode(form.getContrasena().trim()));
        usuario.setRol(obtenerOCrearRolCliente());
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);
        sincronizarCliente(guardado, null, null);
        return guardado;
    }

    public Usuario obtenerUsuarioPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(normalizarCorreo(correo))
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario autenticado."));
    }

    public ClientePerfilForm construirPerfil(String correo) {
        Usuario usuario = obtenerUsuarioPorCorreo(correo);
        ClientePerfilForm form = new ClientePerfilForm();
        form.setNombreCompleto(usuario.getNombre());
        form.setCorreo(usuario.getCorreo());
        form.setCedula(usuario.getCedula());
        form.setTelefono(usuario.getTelefono());
        form.setDireccion(usuario.getDireccion());
        form.setCiudad(usuario.getCiudad());
        form.setBarrio(usuario.getBarrio());
        form.setLocalidad(usuario.getLocalidad());
        return form;
    }

    @Transactional
    public Usuario actualizarPerfil(String correoActual, ClientePerfilForm form) {
        Usuario usuario = obtenerUsuarioPorCorreo(correoActual);
        String correoAnterior = usuario.getCorreo();
        String cedulaAnterior = usuario.getCedula();

        usuario.setNombre(normalizarTexto(form.getNombreCompleto()));
        usuario.setCorreo(normalizarCorreo(form.getCorreo()));
        usuario.setCedula(normalizarTexto(form.getCedula()));
        usuario.setTelefono(normalizarTexto(form.getTelefono()));
        usuario.setDireccion(normalizarTextoOpcional(form.getDireccion()));
        usuario.setCiudad(normalizarTextoOpcional(form.getCiudad()));
        usuario.setBarrio(normalizarTextoOpcional(form.getBarrio()));
        usuario.setLocalidad(normalizarTextoOpcional(form.getLocalidad()));

        if (form.getNuevaContrasena() != null && !form.getNuevaContrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(form.getNuevaContrasena().trim()));
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        sincronizarCliente(actualizado, correoAnterior, cedulaAnterior);
        return actualizado;
    }

    public List<Pedido> obtenerCompras(String correo) {
        Usuario usuario = obtenerUsuarioPorCorreo(correo);
        return pedidoRepository.findByClienteUsuarioIdUsuarioOrderByFechaCreacionDesc(usuario.getIdUsuario());
    }

    public List<FavoritoVehiculo> obtenerFavoritos(String correo) {
        Usuario usuario = obtenerUsuarioPorCorreo(correo);
        return favoritoVehiculoRepository.findByUsuarioIdUsuarioOrderByFechaCreacionDesc(usuario.getIdUsuario());
    }

    public Set<Long> obtenerIdsFavoritos(String correo) {
        return obtenerFavoritos(correo).stream()
                .map(FavoritoVehiculo::getVehiculo)
                .map(Vehiculo::getIdVehiculo)
                .collect(Collectors.toSet());
    }

    @Transactional
    public boolean alternarFavorito(String correo, Long idVehiculo) {
        if (idVehiculo == null) {
            throw new IllegalArgumentException("Debes seleccionar un vehículo válido.");
        }

        Usuario usuario = obtenerUsuarioPorCorreo(correo);

        Optional<FavoritoVehiculo> favorito = favoritoVehiculoRepository
                .findByUsuarioIdUsuarioAndVehiculoIdVehiculo(usuario.getIdUsuario(), idVehiculo);

        if (favorito.isPresent()) {
            Long usuarioId = Objects.requireNonNull(usuario.getIdUsuario(), "El usuario no tiene un identificador válido.");
            favoritoVehiculoRepository.deleteByUsuarioIdUsuarioAndVehiculoIdVehiculo(usuarioId, idVehiculo);
            return false;
        }

        Vehiculo vehiculo = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado."));

        FavoritoVehiculo nuevo = new FavoritoVehiculo();
        nuevo.setUsuario(usuario);
        nuevo.setVehiculo(vehiculo);
        favoritoVehiculoRepository.save(nuevo);
        return true;
    }

    public boolean esCliente(Usuario usuario) {
        if (usuario == null || usuario.getRol() == null || usuario.getRol().getNombre() == null) {
            return false;
        }
        String nombreRol = usuario.getRol().getNombre().trim().toUpperCase(Locale.ROOT);
        return nombreRol.equals(ROL_CLIENTE) || nombreRol.equals("ROLE_" + ROL_CLIENTE);
    }

    @Transactional
    public int resetearTodasLasContrasenas(String nuevaContrasena) {
        String hashComun = passwordEncoder.encode(nuevaContrasena);
        List<Usuario> usuarios = usuarioRepository.findAll();
        usuarios.forEach(usuario -> usuario.setContrasena(hashComun));
        usuarioRepository.saveAll(usuarios);
        return usuarios.size();
    }

    @Transactional
    public void registrarInteresVehiculo(Long idVehiculo, SolicitudInteresForm form, String correoAutenticado) {
        Vehiculo vehiculo = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado."));

        Usuario usuarioInteresado = obtenerUsuarioInteresado(correoAutenticado, form.getCorreo());
        Cliente cliente = resolverClienteInteres(form, correoAutenticado);
        cliente.setInteresVehiculo(vehiculo.getMarca() + " " + vehiculo.getModelo());
        cliente.setEstado("Interesado");
        cliente.setActivo(true);
        cliente.setFechaUltimaInteraccion(LocalDateTime.now());
        cliente.setNotas(construirNotaInteres(cliente.getNotas(), vehiculo, form.getMensaje()));
        cliente = clienteRepository.save(cliente);

        registrarOportunidadWeb(vehiculo, cliente, usuarioInteresado, form.getMensaje());
    }

    private void mapearDatosRegistro(Usuario usuario, ClienteRegistroForm form) {
        usuario.setNombre(normalizarTexto(form.getNombreCompleto()));
        usuario.setCorreo(normalizarCorreo(form.getCorreo()));
        usuario.setCedula(normalizarTexto(form.getCedula()));
        usuario.setTelefono(normalizarTexto(form.getTelefono()));
        usuario.setDireccion(normalizarTextoOpcional(form.getDireccion()));
        usuario.setCiudad(normalizarTextoOpcional(form.getCiudad()));
        usuario.setBarrio(normalizarTextoOpcional(form.getBarrio()));
        usuario.setLocalidad(normalizarTextoOpcional(form.getLocalidad()));
    }

    private Rol obtenerOCrearRolCliente() {
        return rolRepository.findByNombre(ROL_CLIENTE)
                .or(() -> rolRepository.findByNombre("ROLE_" + ROL_CLIENTE))
                .orElseGet(() -> {
                    Rol rol = new Rol();
                    rol.setNombre(ROL_CLIENTE);
                    return rolRepository.save(rol);
                });
    }

    private void sincronizarCliente(Usuario usuario, String correoAnterior, String cedulaAnterior) {
        Optional<Cliente> clienteExistente = Optional.empty();

        if (correoAnterior != null && !correoAnterior.isBlank()) {
            clienteExistente = clienteRepository.findByCorreo(correoAnterior);
        }
        if (clienteExistente.isEmpty() && usuario.getCorreo() != null) {
            clienteExistente = clienteRepository.findByCorreo(usuario.getCorreo());
        }
        if (clienteExistente.isEmpty() && cedulaAnterior != null && !cedulaAnterior.isBlank()) {
            clienteExistente = clienteRepository.findByCedula(cedulaAnterior);
        }
        if (clienteExistente.isEmpty() && usuario.getCedula() != null && !usuario.getCedula().isBlank()) {
            clienteExistente = clienteRepository.findByCedula(usuario.getCedula());
        }

        Cliente cliente = clienteExistente.orElseGet(Cliente::new);
        cliente.setNombre(usuario.getNombre());
        cliente.setCorreo(usuario.getCorreo());
        cliente.setTelefono(usuario.getTelefono());
        cliente.setCedula(usuario.getCedula());
        cliente.setCiudad(usuario.getCiudad());
        cliente.setBarrio(usuario.getBarrio());
        cliente.setLocalidad(usuario.getLocalidad());
        cliente.setDireccion(usuario.getDireccion());
        cliente.setActivo(true);
        cliente.setFechaUltimaInteraccion(LocalDateTime.now());

        if (cliente.getEstado() == null || cliente.getEstado().isBlank()) {
            cliente.setEstado("Nuevo");
        }

        clienteRepository.save(cliente);
    }

    private Cliente resolverClienteInteres(SolicitudInteresForm form, String correoAutenticado) {
        if (correoAutenticado != null && !correoAutenticado.isBlank()) {
            Usuario usuario = obtenerUsuarioPorCorreo(correoAutenticado);
            Optional<Cliente> clienteExistente = clienteRepository.findByCorreo(usuario.getCorreo())
                    .or(() -> usuario.getCedula() != null && !usuario.getCedula().isBlank()
                            ? clienteRepository.findByCedula(usuario.getCedula())
                            : Optional.empty());

            Cliente cliente = clienteExistente.orElseGet(Cliente::new);
            cliente.setNombre(usuario.getNombre());
            cliente.setCorreo(usuario.getCorreo());
            cliente.setTelefono(usuario.getTelefono());
            cliente.setCedula(usuario.getCedula());
            cliente.setCiudad(usuario.getCiudad());
            cliente.setBarrio(usuario.getBarrio());
            cliente.setLocalidad(usuario.getLocalidad());
            cliente.setDireccion(usuario.getDireccion());
            return cliente;
        }

        String correo = normalizarCorreo(form.getCorreo());
        Cliente cliente = correo != null
                ? clienteRepository.findByCorreo(correo).orElseGet(Cliente::new)
                : new Cliente();

        cliente.setNombre(normalizarTexto(form.getNombreCompleto()));
        cliente.setCorreo(correo);
        cliente.setTelefono(normalizarTexto(form.getTelefono()));
        return cliente;
    }

    private Usuario obtenerUsuarioInteresado(String correoAutenticado, String correoFormulario) {
        if (correoAutenticado != null && !correoAutenticado.isBlank()) {
            return obtenerUsuarioPorCorreo(correoAutenticado);
        }

        String correoNormalizado = normalizarCorreo(correoFormulario);
        if (correoNormalizado == null || correoNormalizado.isBlank()) {
            return null;
        }

        return usuarioRepository.findByCorreo(correoNormalizado).orElse(null);
    }

    private void registrarOportunidadWeb(Vehiculo vehiculo, Cliente cliente, Usuario usuarioInteresado, String mensaje) {
        Pedido pedido = new Pedido();
        pedido.setVehiculo(vehiculo);
        pedido.setClienteUsuario(usuarioInteresado);
        pedido.setEstado("pendiente");
        pedido.setDescripcion(construirDescripcionInteresWeb(cliente, mensaje));
        pedidoRepository.save(pedido);
    }

    private String construirDescripcionInteresWeb(Cliente cliente, String mensaje) {
        StringBuilder builder = new StringBuilder("[WEB] ");
        builder.append(cliente.getNombre() != null && !cliente.getNombre().isBlank()
                ? cliente.getNombre().trim()
                : "Cliente sin nombre");

        if (cliente.getTelefono() != null && !cliente.getTelefono().isBlank()) {
            builder.append(" | Tel: ").append(cliente.getTelefono().trim());
        }
        if (cliente.getCorreo() != null && !cliente.getCorreo().isBlank()) {
            builder.append(" | Correo: ").append(cliente.getCorreo().trim());
        }
        if (mensaje != null && !mensaje.isBlank()) {
            builder.append(" | Nota: ").append(resumirTextoPedido(mensaje.trim(), 70));
        }

        String descripcion = builder.toString();
        return descripcion.length() > 255 ? descripcion.substring(0, 252) + "..." : descripcion;
    }

    private String resumirTextoPedido(String valor, int maximo) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        if (valor.length() <= maximo) {
            return valor;
        }
        return valor.substring(0, Math.max(0, maximo - 3)).trim() + "...";
    }

    private String construirNotaInteres(String notasActuales, Vehiculo vehiculo, String mensaje) {
        StringBuilder builder = new StringBuilder();
        if (notasActuales != null && !notasActuales.isBlank()) {
            builder.append(notasActuales.trim()).append(System.lineSeparator()).append(System.lineSeparator());
        }

        builder.append("[Solicitud web] Interés en ")
                .append(vehiculo.getMarca())
                .append(' ')
                .append(vehiculo.getModelo())
                .append(" registrada el ")
                .append(LocalDateTime.now());

        if (mensaje != null && !mensaje.isBlank()) {
            builder.append(System.lineSeparator()).append("Mensaje del cliente: ").append(mensaje.trim());
        }

        return builder.toString();
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String normalizarTextoOpcional(String valor) {
        String normalizado = normalizarTexto(valor);
        return normalizado == null || normalizado.isBlank() ? null : normalizado;
    }
}