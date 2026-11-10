package org.tusalud.model;

import java.sql.Timestamp;

public class Medicion {
    private int idMedicion;
    private int idUsuario;
    private Timestamp fechaHora;
    private Double peso;
    private Double altura;
    private Integer frecuenciaCardiaca;
    private Double glucosaSangre;
    private Integer presionSistolica;
    private Integer presionDiastolica;

    public Medicion() {}

    public Medicion(int idUsuario, Timestamp fechaHora, Double peso, Double altura, Integer frecuenciaCardiaca, Double glucosaSangre) {
        this.idUsuario = idUsuario;
        this.fechaHora = fechaHora;
        this.peso = peso;
        this.altura = altura;
        this.frecuenciaCardiaca = frecuenciaCardiaca;
        this.glucosaSangre = glucosaSangre;
    }

    public Medicion(int idUsuario, Timestamp fechaHora, Double peso, Double altura, Integer frecuenciaCardiaca, Double glucosaSangre, Integer presionSistolica, Integer presionDiastolica) {
        this.idUsuario = idUsuario;
        this.fechaHora = fechaHora;
        this.peso = peso;
        this.altura = altura;
        this.frecuenciaCardiaca = frecuenciaCardiaca;
        this.glucosaSangre = glucosaSangre;
        this.presionSistolica = presionSistolica;
        this.presionDiastolica = presionDiastolica;
    }

    public int getIdMedicion() { return idMedicion; }
    public void setIdMedicion(int idMedicion) { this.idMedicion = idMedicion; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public Timestamp getFechaHora() { return fechaHora; }
    public void setFechaHora(Timestamp fechaHora) { this.fechaHora = fechaHora; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public Double getAltura() { return altura; }
    public void setAltura(Double altura) { this.altura = altura; }

    public Integer getFrecuenciaCardiaca() { return frecuenciaCardiaca; }
    public void setFrecuenciaCardiaca(Integer frecuenciaCardiaca) { this.frecuenciaCardiaca = frecuenciaCardiaca; }

    public Double getGlucosaSangre() { return glucosaSangre; }
    public void setGlucosaSangre(Double glucosaSangre) { this.glucosaSangre = glucosaSangre; }

    public Integer getPresionSistolica() { return presionSistolica; }
    public void setPresionSistolica(Integer presionSistolica) { this.presionSistolica = presionSistolica; }

    public Integer getPresionDiastolica() { return presionDiastolica; }
    public void setPresionDiastolica(Integer presionDiastolica) { this.presionDiastolica = presionDiastolica; }
}
