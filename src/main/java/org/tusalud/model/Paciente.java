// src/main/java/org/tusalud/model/Paciente.java
package org.tusalud.model;

import org.tusalud.service.PerfilMedicoService;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Subclase que representa a un Paciente del sistema clínico (Cumple R5: Herencia y Polimorfismo).
 */
public class Paciente extends Usuario {
    private List<Medicion> mediciones = new ArrayList<>();
    private List<Recomendacion> recomendaciones = new ArrayList<>();

    public Paciente() {
        super();
        setIdRol(1);
        setRolNombre("PACIENTE");
    }

    public Paciente(int idUsuario, String nombre, String correo, String contraseniaHash, Date fechaNacimiento, String genero, String perfilMedico) {
        super(idUsuario, 1, "PACIENTE", nombre, correo, contraseniaHash, fechaNacimiento, genero, perfilMedico);
    }

    @Override
    public String getDescripcionRol() {
        return "Paciente Clínico — Perfil: " + PerfilMedicoService.getNombreLegible(getPerfilMedico());
    }

    @Override
    public boolean tieneAccesoPanelMedico() {
        return false;
    }

    public List<Medicion> getMediciones() { return mediciones; }
    public void setMediciones(List<Medicion> mediciones) { this.mediciones = mediciones; }

    public List<Recomendacion> getRecomendaciones() { return recomendaciones; }
    public void setRecomendaciones(List<Recomendacion> recomendaciones) { this.recomendaciones = recomendaciones; }
}
