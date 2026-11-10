// src/main/java/org/tusalud/model/Usuario.java
package org.tusalud.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Clase base de la jerarquía de usuarios del sistema (Cumple R1 y R5).
 * De esta clase heredan Paciente y Medico aplicando herencia y polimorfismo.
 */
public class Usuario {
    private int idUsuario;
    private int idRol = 1; // 1 = PACIENTE, 2 = MEDICO, 3 = ADMIN
    private String rolNombre = "PACIENTE";
    private String nombre;
    private String correo;
    private String contraseniaHash;
    private Date fechaNacimiento;
    private String genero;
    private Timestamp fechaRegistro;
    private String perfilMedico;

    public Usuario() {}

    public Usuario(int idUsuario, String nombre, String correo, String contraseniaHash, Date fechaNacimiento, String genero) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.contraseniaHash = contraseniaHash;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.idRol = 1;
        this.rolNombre = "PACIENTE";
    }

    public Usuario(int idUsuario, int idRol, String rolNombre, String nombre, String correo, String contraseniaHash, Date fechaNacimiento, String genero, String perfilMedico) {
        this.idUsuario = idUsuario;
        this.idRol = idRol;
        this.rolNombre = rolNombre;
        this.nombre = nombre;
        this.correo = correo;
        this.contraseniaHash = contraseniaHash;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.perfilMedico = perfilMedico;
    }

    /**
     * Método polimórfico sobrescrito por las subclases Paciente y Medico (R5).
     */
    public String getDescripcionRol() {
        return "Usuario general del sistema (" + rolNombre + ")";
    }

    /**
     * Determina polimórficamente si el usuario tiene permisos médicos/administrativos (R3 y R5).
     */
    public boolean tieneAccesoPanelMedico() {
        return idRol == 2 || idRol == 3 || "MEDICO".equalsIgnoreCase(rolNombre) || "ADMIN".equalsIgnoreCase(rolNombre);
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public String getRolNombre() { return rolNombre; }
    public void setRolNombre(String rolNombre) { this.rolNombre = rolNombre; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContraseniaHash() { return contraseniaHash; }
    public void setContraseniaHash(String contraseniaHash) { this.contraseniaHash = contraseniaHash; }

    public Date getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public Timestamp getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Timestamp fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getPerfilMedico() { return perfilMedico; }
    public void setPerfilMedico(String perfilMedico) { this.perfilMedico = perfilMedico; }
}
