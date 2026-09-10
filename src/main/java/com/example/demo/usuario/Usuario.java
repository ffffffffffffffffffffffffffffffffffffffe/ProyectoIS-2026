package com.example.demo.usuario;

import com.example.demo.rol.Rol;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "rut", unique = true, length = 10)
    private String rut;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"usuario_id", "rol_id"}
            )
    )
    private Set<Rol> roles = new HashSet<>();

    protected Usuario() {
    }

    public Usuario(String nombre, String correo, String passwordHash) {
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = RutValidator.normalizarYValidar(rut);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    public void agregarRol(Rol rol) {
        roles.add(rol);
    }

    public void quitarRol(Rol rol) {
        roles.remove(rol);
    }
}