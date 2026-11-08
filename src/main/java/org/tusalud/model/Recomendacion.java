package org.tusalud.model;

import java.sql.Timestamp;

public class Recomendacion {
    private int idRecomendacion;
    private int idUsuario;
    private Integer idMedicion;
    private String tipo;
    private String mensaje;
    private Timestamp fechaGeneracion;

    public Recomendacion() {}

    public Recomendacion(int idUsuario, Integer idMedicion, String tipo, String mensaje) {
        this.idUsuario = idUsuario;
        this.idMedicion = idMedicion;
        this.tipo = tipo;
        this.mensaje = mensaje;
    }

    public int getIdRecomendacion() { return idRecomendacion; }
    public void setIdRecomendacion(int idRecomendacion) { this.idRecomendacion = idRecomendacion; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdMedicion() { return idMedicion; }
    public void setIdMedicion(Integer idMedicion) { this.idMedicion = idMedicion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Timestamp getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(Timestamp fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
}
