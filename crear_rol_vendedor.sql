INSERT INTO rol (nombre)
SELECT 'VENDEDOR'
WHERE NOT EXISTS (
    SELECT 1 FROM rol WHERE UPPER(nombre) = 'VENDEDOR'
);

-- Ejemplo opcional para asignar el rol a un usuario existente (descomentar y ajustar correo):
-- UPDATE usuario u
-- JOIN rol r ON UPPER(r.nombre) = 'VENDEDOR'
-- SET u.Rol_idRol = r.idRol
-- WHERE u.Correo = 'vendedor@tuempresa.com';
