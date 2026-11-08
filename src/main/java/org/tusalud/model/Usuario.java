package org.tusalud.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Usuario {
    private int idUsuario;
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
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

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
