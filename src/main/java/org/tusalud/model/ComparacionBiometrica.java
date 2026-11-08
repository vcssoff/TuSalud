package org.tusalud.model;

public class ComparacionBiometrica {
    private Medicion medicionAnterior;
    private Medicion medicionPosterior;

    private Double pesoAnterior;
    private Double pesoPosterior;
    private Double deltaPeso;

    private Double imcAnterior;
    private Double imcPosterior;
    private Double deltaIMC;

    private Integer fcAnterior;
    private Integer fcPosterior;
    private Integer deltaFC;

    private Double glucosaAnterior;
    private Double glucosaPosterior;
    private Double deltaGlucosa;

    private String diagnostico;

    public ComparacionBiometrica() {}

    public Medicion getMedicionAnterior() { return medicionAnterior; }
    public void setMedicionAnterior(Medicion medicionAnterior) { this.medicionAnterior = medicionAnterior; }

    public Medicion getMedicionPosterior() { return medicionPosterior; }
    public void setMedicionPosterior(Medicion medicionPosterior) { this.medicionPosterior = medicionPosterior; }

    public Double getPesoAnterior() { return pesoAnterior; }
    public void setPesoAnterior(Double pesoAnterior) { this.pesoAnterior = pesoAnterior; }

    public Double getPesoPosterior() { return pesoPosterior; }
    public void setPesoPosterior(Double pesoPosterior) { this.pesoPosterior = pesoPosterior; }

    public Double getDeltaPeso() { return deltaPeso; }
    public void setDeltaPeso(Double deltaPeso) { this.deltaPeso = deltaPeso; }

    public Double getImcAnterior() { return imcAnterior; }
    public void setImcAnterior(Double imcAnterior) { this.imcAnterior = imcAnterior; }

    public Double getImcPosterior() { return imcPosterior; }
    public void setImcPosterior(Double imcPosterior) { this.imcPosterior = imcPosterior; }

    public Double getDeltaIMC() { return deltaIMC; }
    public void setDeltaIMC(Double deltaIMC) { this.deltaIMC = deltaIMC; }

    public Integer getFcAnterior() { return fcAnterior; }
    public void setFcAnterior(Integer fcAnterior) { this.fcAnterior = fcAnterior; }

    public Integer getFcPosterior() { return fcPosterior; }
    public void setFcPosterior(Integer fcPosterior) { this.fcPosterior = fcPosterior; }

    public Integer getDeltaFC() { return deltaFC; }
    public void setDeltaFC(Integer deltaFC) { this.deltaFC = deltaFC; }

    public Double getGlucosaAnterior() { return glucosaAnterior; }
    public void setGlucosaAnterior(Double glucosaAnterior) { this.glucosaAnterior = glucosaAnterior; }

    public Double getGlucosaPosterior() { return glucosaPosterior; }
    public void setGlucosaPosterior(Double glucosaPosterior) { this.glucosaPosterior = glucosaPosterior; }

    public Double getDeltaGlucosa() { return deltaGlucosa; }
    public void setDeltaGlucosa(Double deltaGlucosa) { this.deltaGlucosa = deltaGlucosa; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
}
