// src/main/java/org/tusalud/model/Medico.java
package org.tusalud.model;

import java.sql.Date;

/**
 * Subclase que representa a un Profesional Médico del sistema (Cumple R5: Herencia y Polimorfismo).
 */
public class Medico extends Usuario {
    private String especialidad;
    private String matricula;

    public Medico() {
        super();
        setIdRol(2);
        setRolNombre("MEDICO");
    }

    public Medico(int idUsuario, String nombre, String correo, String contraseniaHash, Date fechaNacimiento, String genero, String especialidad, String matricula) {
        super(idUsuario, 2, "MEDICO", nombre, correo, contraseniaHash, fechaNacimiento, genero, "DUAL_DIAGNOSTICADO");
        this.especialidad = especialidad;
        this.matricula = matricula;
    }

    @Override
    public String getDescripcionRol() {
        String esp = (especialidad != null && !especialidad.isEmpty()) ? especialidad : "Medicina General";
        String mat = (matricula != null && !matricula.isEmpty()) ? " (Mat. " + matricula + ")" : "";
        return "Profesional Médico — " + esp + mat;
    }

    @Override
    public boolean tieneAccesoPanelMedico() {
        return true;
    }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
}
