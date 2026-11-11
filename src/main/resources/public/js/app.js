// src/main/resources/public/js/app.js

let usuarioActual = null;
let medicionesCache = [];
let chartInstancia = null;
let chartMedicoInstancia = null;

document.addEventListener("DOMContentLoaded", () => {
    const page = document.body.getAttribute("data-page");
    if (page === "auth") {
        inicializarVistaAuth();
    } else if (page === "dashboard") {
        inicializarVistaDashboard();
    } else if (page === "medico") {
        inicializarVistaMedico();
    }
});

function mostrarAlerta(mensaje, tipo = "danger") {
    const cont = document.getElementById("alertContainer");
    if (!cont) return;
    cont.innerHTML = `
        <div class="alert alert-${tipo} alert-dismissible fade show shadow-sm" role="alert">
            <i class="bi ${tipo === "danger" ? "bi-exclamation-triangle-fill" : "bi-check-circle-fill"} me-2"></i>
            ${mensaje}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
}

async function apiFetch(url, options = {}) {
    const config = {
        headers: { "Content-Type": "application/json" },
        ...options
    };
    const resp = await fetch(url, config);
    const data = await resp.json().catch(() => ({}));
    if (!resp.ok) {
        throw new Error(data.error || "Error al comunicarse con el servidor.");
    }
    return data;
}

// =============================================================================
// 1. VISTA DE LOGIN Y REGISTRO (index.html)
// =============================================================================
function inicializarVistaAuth() {
    const formLogin = document.getElementById("formLogin");
    if (formLogin) {
        formLogin.addEventListener("submit", async (e) => {
            e.preventDefault();
            try {
                const u = await apiFetch("/api/login", {
                    method: "POST",
                    body: JSON.stringify({
                        correo: document.getElementById("loginCorreo").value,
                        password: document.getElementById("loginPassword").value
                    })
                });
                window.location.href = u.tieneAccesoMedico ? "/medico.html" : "/dashboard.html";
            } catch (err) {
                mostrarAlerta(err.message, "danger");
            }
        });
    }

    const formRegister = document.getElementById("formRegister");
    if (formRegister) {
        formRegister.addEventListener("submit", async (e) => {
            e.preventDefault();
            try {
                const u = await apiFetch("/api/register", {
                    method: "POST",
                    body: JSON.stringify({
                        nombre: document.getElementById("regNombre").value,
                        correo: document.getElementById("regCorreo").value,
                        fechaNacimiento: document.getElementById("regFechaNac").value,
                        genero: document.getElementById("regGenero").value,
                        password: document.getElementById("regPassword").value
                    })
                });
                window.location.href = u.tieneAccesoMedico ? "/medico.html" : "/dashboard.html";
            } catch (err) {
                mostrarAlerta(err.message, "danger");
            }
        });
    }
}

// =============================================================================
// 2. VISTA PANEL DEL PACIENTE (dashboard.html)
// =============================================================================
async function inicializarVistaDashboard() {
    try {
        usuarioActual = await apiFetch("/api/me");
    } catch (_) {
        window.location.href = "/index.html";
        return;
    }

    aplicarRestriccionesPerfilUI(usuarioActual);
    configurarEventosDashboard();
    await recargarDatosDashboard();

    // Si el paciente aún no completó el cuestionario médico, abrir el modal automáticamente
    if (!usuarioActual.perfilMedico && !usuarioActual.tieneAccesoMedico) {
        const modalEl = document.getElementById("modalCuestionario");
        if (modalEl) new bootstrap.Modal(modalEl).show();
    }
}

function aplicarRestriccionesPerfilUI(u) {
    document.getElementById("navUserName").textContent = u.nombre;
    document.getElementById("badgePerfilActual").textContent = u.nombreLegiblePerfil;
    document.getElementById("kpiRolDescripcion").textContent = u.descripcionRol;

    const btnMedico = document.getElementById("btnIrPanelMedico");
    if (btnMedico) {
        btnMedico.classList.toggle("d-none", !u.tieneAccesoMedico);
    }

    document.getElementById("perfNombre").value = u.nombre || "";
    document.getElementById("perfFechaNac").value = u.fechaNacimiento || "";
    document.getElementById("perfGenero").value = u.genero || "Masculino";

    configurarCampoSegunPerfil("inputPeso", "lockBadgePeso", "motivoPeso", "editPeso", u.permitePeso, u.motivoPeso);
    configurarCampoSegunPerfil("inputAltura", "lockBadgeAltura", "motivoAltura", "editAltura", u.permiteAltura, u.motivoAltura);
    configurarCampoSegunPerfil("inputFC", "lockBadgeFC", "motivoFC", "editFC", u.permiteFrecuencia, u.motivoFrecuencia);
    configurarCampoSegunPerfil("inputGlucosa", "lockBadgeGlucosa", "motivoGlucosa", "editGlucosa", u.permiteGlucosa, u.motivoGlucosa);
}

function configurarCampoSegunPerfil(inputId, badgeId, motivoId, editId, permitido, motivoTexto) {
    const input = document.getElementById(inputId);
    const badge = document.getElementById(badgeId);
    const motivo = document.getElementById(motivoId);
    const editInput = document.getElementById(editId);

    if (!input) return;
    if (permitido) {
        input.disabled = false;
        input.classList.remove("field-locked");
        input.removeAttribute("title");
        if (badge) badge.classList.add("d-none");
        if (motivo) motivo.classList.add("d-none");
        if (editInput) editInput.disabled = false;
    } else {
        input.value = "";
        input.disabled = true;
        input.classList.add("field-locked");
        input.setAttribute("title", motivoTexto);
        if (badge) badge.classList.remove("d-none");
        if (motivo) {
            motivo.textContent = motivoTexto;
            motivo.classList.remove("d-none");
        }
        if (editInput) {
            editInput.value = "";
            editInput.disabled = true;
        }
    }
}

async function recargarDatosDashboard() {
    const dataMed = await apiFetch("/api/mediciones");
    medicionesCache = dataMed.mediciones || [];

    document.getElementById("kpiImc").textContent = dataMed.imc > 0 ? dataMed.imc.toFixed(1) : "--";
    document.getElementById("kpiCardiaco").textContent = dataMed.promedioCardiaco > 0 ? dataMed.promedioCardiaco.toFixed(0) : "--";
    document.getElementById("kpiGlucosa").textContent = dataMed.promedioGlucosa > 0 ? dataMed.promedioGlucosa.toFixed(1) : "--";
    document.getElementById("kpiTotal").textContent = dataMed.totalMediciones || 0;

    renderizarTablaMediciones(medicionesCache);
    renderizarSelectoresComparacion(medicionesCache);
    renderizarGraficoEvolucion(medicionesCache, document.getElementById("filtroGrafico").value);

    const recomendaciones = await apiFetch("/api/recomendaciones");
    renderizarRecomendaciones(recomendaciones);
}

function renderizarTablaMediciones(lista) {
    const tbody = document.getElementById("tablaMedicionesBody");
    if (!lista || lista.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-3">No hay mediciones registradas todavía.</td></tr>`;
        return;
    }

    tbody.innerHTML = lista.slice().reverse().map(m => {
        const fecha = m.fechaHora ? new Date(m.fechaHora).toLocaleString("es-UY") : "-";
        const presion = (m.presionSistolica != null && m.presionDiastolica != null) ? `${m.presionSistolica}/${m.presionDiastolica}` : '<span class="text-muted">-</span>';
        const imc = (m.peso && m.altura && m.altura > 0) ? (m.peso / (m.altura * m.altura)).toFixed(1) : "-";
        return `
            <tr>
                <td class="fw-bold">#${m.idMedicion}</td>
                <td>${fecha}</td>
                <td><span class="badge bg-danger-subtle text-danger border">${presion}</span></td>
                <td>${m.frecuenciaCardiaca != null ? m.frecuenciaCardiaca + " bpm" : '<span class="text-muted">-</span>'}</td>
                <td>${m.peso != null ? m.peso + " kg" : '<span class="text-muted">-</span>'}</td>
                <td>${m.altura != null ? m.altura + " m" : '<span class="text-muted">-</span>'}</td>
                <td><span class="badge bg-light text-dark border">${imc}</span></td>
                <td>${m.glucosaSangre != null ? m.glucosaSangre + " mg/dL" : '<span class="text-muted">-</span>'}</td>
                <td class="text-end">
                    <button class="btn btn-outline-primary btn-sm me-1" onclick="abrirModalEditarMedicion(${m.idMedicion})">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-outline-danger btn-sm" onclick="eliminarMedicion(${m.idMedicion})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>
        `;
    }).join("");
}

function renderizarSelectoresComparacion(lista) {
    const s1 = document.getElementById("selectComp1");
    const s2 = document.getElementById("selectComp2");
    if (!s1 || !s2) return;
    const opts = lista.map(m => {
        const f = m.fechaHora ? new Date(m.fechaHora).toLocaleDateString("es-UY") : "";
        return `<option value="${m.idMedicion}">#${m.idMedicion} (${f})</option>`;
    }).join("");
    s1.innerHTML = opts;
    s2.innerHTML = opts;
    if (lista.length >= 2) {
        s1.value = lista[0].idMedicion;
        s2.value = lista[lista.length - 1].idMedicion;
    }
}

function renderizarGraficoEvolucion(lista, filtro) {
    const ctx = document.getElementById("chartEvolucion");
    if (!ctx) return;
    if (chartInstancia) chartInstancia.destroy();

    const labels = lista.map(m => m.fechaHora ? new Date(m.fechaHora).toLocaleDateString("es-UY") + " #" + m.idMedicion : "#" + m.idMedicion);
    const datasets = [];

    if (filtro === "PRESION" || filtro === "TODAS") {
        const tienePresion = lista.some(m => m.presionSistolica != null || m.presionDiastolica != null);
        if (tienePresion) {
            datasets.push({
                label: "Sístole (mmHg)",
                data: lista.map(m => m.presionSistolica),
                borderColor: "#dc3545",
                backgroundColor: "rgba(220,53,69,0.1)",
                tension: 0.3,
                spanGaps: true
            });
            datasets.push({
                label: "Diástole (mmHg)",
                data: lista.map(m => m.presionDiastolica),
                borderColor: "#fd7e14",
                backgroundColor: "rgba(253,126,20,0.1)",
                tension: 0.3,
                spanGaps: true
            });
        }
    }
    if ((filtro === "TODAS" || filtro === "PESO") && usuarioActual.permitePeso) {
        datasets.push({
            label: "Peso (kg)",
            data: lista.map(m => m.peso),
            borderColor: "#0d6efd",
            backgroundColor: "rgba(13,110,253,0.1)",
            tension: 0.3,
            spanGaps: true
        });
    }
    if (filtro === "IMC" && usuarioActual.permitePeso) {
        datasets.push({
            label: "IMC (kg/m²)",
            data: lista.map(m => (m.peso && m.altura) ? Number((m.peso / (m.altura * m.altura)).toFixed(1)) : null),
            borderColor: "#6610f2",
            backgroundColor: "rgba(102,16,242,0.1)",
            tension: 0.3,
            spanGaps: true
        });
    }
    if ((filtro === "TODAS" || filtro === "CARDIACO") && usuarioActual.permiteFrecuencia) {
        datasets.push({
            label: "Frecuencia Cardíaca (bpm)",
            data: lista.map(m => m.frecuenciaCardiaca),
            borderColor: "#0dcaf0",
            backgroundColor: "rgba(13,202,240,0.1)",
            tension: 0.3,
            spanGaps: true
        });
    }
    if ((filtro === "TODAS" || filtro === "GLUCOSA") && usuarioActual.permiteGlucosa) {
        datasets.push({
            label: "Glucosa (mg/dL)",
            data: lista.map(m => m.glucosaSangre),
            borderColor: "#198754",
            backgroundColor: "rgba(25,135,84,0.1)",
            tension: 0.3,
            spanGaps: true
        });
    }

    chartInstancia = new Chart(ctx, {
        type: "line",
        data: { labels, datasets },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: "bottom" } }
        }
    });
}

function renderizarRecomendaciones(lista) {
    const cont = document.getElementById("listaRecomendaciones");
    if (!cont) return;
    if (!lista || lista.length === 0) {
        cont.innerHTML = `<div class="text-muted py-2">Registra tus mediciones para recibir recomendaciones clínicas personalizadas.</div>`;
        return;
    }

    cont.innerHTML = lista.map(r => {
        const badgeColor = r.tipo === "INDICACION_MEDICA" ? "bg-warning text-dark" : (r.tipo === "GLUCOSA" ? "bg-success" : "bg-primary");
        return `
            <div class="list-group-item d-flex justify-content-between align-items-start px-0">
                <div>
                    <span class="badge ${badgeColor} me-2">${r.tipo}</span>
                    <span>${r.mensaje}</span>
                </div>
                <button class="btn btn-link text-danger btn-sm p-0 ms-2" onclick="eliminarRecomendacion(${r.idRecomendacion})" title="Eliminar">
                    <i class="bi bi-x-circle"></i>
                </button>
            </div>
        `;
    }).join("");
}

function configurarEventosDashboard() {
    document.getElementById("btnLogout").addEventListener("click", async () => {
        await apiFetch("/api/logout", { method: "POST" });
        window.location.href = "/index.html";
    });

    document.getElementById("filtroGrafico").addEventListener("change", (e) => {
        renderizarGraficoEvolucion(medicionesCache, e.target.value);
    });

    // Visibilidad dinámica dentro del modal de Cuestionario Médico
    const selectSituacion = document.getElementById("cuestSituacion");
    selectSituacion.addEventListener("change", () => {
        const val = selectSituacion.value;
        document.getElementById("subpanelHipertension").classList.toggle("d-none", val !== "HIPERTENSION");
        document.getElementById("subpanelDiabetes").classList.toggle("d-none", val !== "DIABETES");
        document.getElementById("subpanelBienestar").classList.toggle("d-none", val !== "BIENESTAR");
    });

    document.getElementById("formCuestionario").addEventListener("submit", async (e) => {
        e.preventDefault();
        try {
            usuarioActual = await apiFetch("/api/cuestionario", {
                method: "POST",
                body: JSON.stringify({
                    situacion: document.getElementById("cuestSituacion").value,
                    controlHipertension: document.getElementById("cuestControlHTA").value,
                    alcanceHipertension: document.getElementById("cuestAlcanceHTA").value,
                    controlDiabetes: document.getElementById("cuestControlDBT").value,
                    alcanceDiabetes: document.getElementById("cuestAlcanceDBT").value,
                    objetivoGeneral: document.getElementById("cuestObjetivoGeneral").value
                })
            });
            aplicarRestriccionesPerfilUI(usuarioActual);
            await recargarDatosDashboard();
            bootstrap.Modal.getInstance(document.getElementById("modalCuestionario")).hide();
            mostrarAlerta("Perfil médico configurado: " + usuarioActual.nombreLegiblePerfil, "success");
        } catch (err) {
            mostrarAlerta(err.message, "danger");
        }
    });

    document.getElementById("formNuevaMedicion").addEventListener("submit", async (e) => {
        e.preventDefault();
        try {
            const resp = await apiFetch("/api/mediciones", {
                method: "POST",
                body: JSON.stringify({
                    peso: document.getElementById("inputPeso").value,
                    altura: document.getElementById("inputAltura").value,
                    frecuenciaCardiaca: document.getElementById("inputFC").value,
                    glucosaSangre: document.getElementById("inputGlucosa").value,
                    presionSistolica: document.getElementById("inputSistole") ? document.getElementById("inputSistole").value : null,
                    presionDiastolica: document.getElementById("inputDiastole") ? document.getElementById("inputDiastole").value : null
                })
            });
            document.getElementById("formNuevaMedicion").reset();
            mostrarAlerta(resp.mensaje, "success");
            await recargarDatosDashboard();
        } catch (err) {
            mostrarAlerta(err.message, "danger");
        }
    });

    document.getElementById("formEditarMedicion").addEventListener("submit", async (e) => {
        e.preventDefault();
        const id = document.getElementById("editIdMedicion").value;
        try {
            await apiFetch(`/api/mediciones/${id}`, {
                method: "PUT",
                body: JSON.stringify({
                    peso: document.getElementById("editPeso").value,
                    altura: document.getElementById("editAltura").value,
                    frecuenciaCardiaca: document.getElementById("editFC").value,
                    glucosaSangre: document.getElementById("editGlucosa").value
                })
            });
            bootstrap.Modal.getInstance(document.getElementById("modalEditarMedicion")).hide();
            mostrarAlerta("Medición actualizada correctamente.", "success");
            await recargarDatosDashboard();
        } catch (err) {
            mostrarAlerta(err.message, "danger");
        }
    });

    document.getElementById("btnComparar").addEventListener("click", async () => {
        const id1 = document.getElementById("selectComp1").value;
        const id2 = document.getElementById("selectComp2").value;
        if (!id1 || !id2) return;
        try {
            const comp = await apiFetch(`/api/mediciones/comparar?id1=${id1}&id2=${id2}`);
            const box = document.getElementById("resultadoComparacion");
            box.textContent = comp.diagnostico;
            box.classList.remove("d-none");
        } catch (err) {
            mostrarAlerta(err.message, "danger");
        }
    });

    document.getElementById("formNuevaRecomendacion").addEventListener("submit", async (e) => {
        e.preventDefault();
        try {
            await apiFetch("/api/recomendaciones", {
                method: "POST",
                body: JSON.stringify({
                    tipo: document.getElementById("recTipo").value,
                    mensaje: document.getElementById("recMensaje").value
                })
            });
            document.getElementById("recMensaje").value = "";
            await recargarDatosDashboard();
        } catch (err) {
            mostrarAlerta(err.message, "danger");
        }
    });

    document.getElementById("formPerfilUsuario").addEventListener("submit", async (e) => {
        e.preventDefault();
        try {
            usuarioActual = await apiFetch("/api/usuarios/perfil", {
                method: "PUT",
                body: JSON.stringify({
                    nombre: document.getElementById("perfNombre").value,
                    fechaNacimiento: document.getElementById("perfFechaNac").value,
                    genero: document.getElementById("perfGenero").value
                })
            });
            aplicarRestriccionesPerfilUI(usuarioActual);
            bootstrap.Modal.getInstance(document.getElementById("modalPerfilUsuario")).hide();
            mostrarAlerta("Datos de usuario actualizados.", "success");
        } catch (err) {
            mostrarAlerta(err.message, "danger");
        }
    });

    document.getElementById("btnEliminarCuenta").addEventListener("click", async () => {
        if (!confirm("¿Estás seguro de eliminar tu cuenta y todas tus mediciones?")) return;
        await apiFetch("/api/usuarios/me", { method: "DELETE" });
        window.location.href = "/index.html";
    });
}

window.abrirModalEditarMedicion = function(idMedicion) {
    const m = medicionesCache.find(x => x.idMedicion === idMedicion);
    if (!m) return;
    document.getElementById("editIdMedicion").value = m.idMedicion;
    document.getElementById("editMedIdLabel").textContent = m.idMedicion;
    document.getElementById("editPeso").value = m.peso != null ? m.peso : "";
    document.getElementById("editAltura").value = m.altura != null ? m.altura : "";
    document.getElementById("editFC").value = m.frecuenciaCardiaca != null ? m.frecuenciaCardiaca : "";
    document.getElementById("editGlucosa").value = m.glucosaSangre != null ? m.glucosaSangre : "";
    new bootstrap.Modal(document.getElementById("modalEditarMedicion")).show();
};

window.eliminarMedicion = async function(idMedicion) {
    if (!confirm(`¿Deseas eliminar la medición #${idMedicion}?`)) return;
    try {
        await apiFetch(`/api/mediciones/${idMedicion}`, { method: "DELETE" });
        mostrarAlerta(`Medición #${idMedicion} eliminada.`, "info");
        await recargarDatosDashboard();
    } catch (err) {
        mostrarAlerta(err.message, "danger");
    }
};

window.eliminarRecomendacion = async function(idRec) {
    try {
        await apiFetch(`/api/recomendaciones/${idRec}`, { method: "DELETE" });
        await recargarDatosDashboard();
    } catch (err) {
        mostrarAlerta(err.message, "danger");
    }
};

// =============================================================================
// 3. VISTA PANEL EXCLUSIVO DEL MÉDICO (medico.html) — Cumple R3 y R6
// =============================================================================
async function inicializarVistaMedico() {
    try {
        usuarioActual = await apiFetch("/api/me");
        if (!usuarioActual.tieneAccesoMedico) {
            window.location.href = "/dashboard.html";
            return;
        }
    } catch (_) {
        window.location.href = "/index.html";
        return;
    }

    document.getElementById("medicoHeaderNombre").textContent = usuarioActual.descripcionRol + " — " + usuarioActual.nombre;

    document.getElementById("btnLogoutMedico").addEventListener("click", async () => {
        await apiFetch("/api/logout", { method: "POST" });
        window.location.href = "/index.html";
    });

    document.getElementById("btnBuscarPaciente").addEventListener("click", () => {
        cargarPacientesMedico(document.getElementById("inputBuscarPaciente").value);
    });

    document.getElementById("inputBuscarPaciente").addEventListener("keyup", (e) => {
        if (e.key === "Enter") cargarPacientesMedico(e.target.value);
    });

    document.getElementById("formIndicacionMedica").addEventListener("submit", async (e) => {
        e.preventDefault();
        const idPaciente = document.getElementById("fichaIdUsuario").value;
        const mensaje = document.getElementById("inputMensajeMedico").value;
        try {
            await apiFetch("/api/medico/recomendaciones", {
                method: "POST",
                body: JSON.stringify({ idUsuario: idPaciente, tipo: "INDICACION_MEDICA", mensaje })
            });
            document.getElementById("inputMensajeMedico").value = "";
            mostrarAlerta("Indicación médica registrada en la historia clínica del paciente.", "success");
            await verFichaPaciente(idPaciente);
        } catch (err) {
            mostrarAlerta(err.message, "danger");
        }
    });

    await cargarReportesGlobales();
    await cargarPacientesMedico("");
}

async function cargarReportesGlobales() {
    const data = await apiFetch("/api/medico/reportes");
    const porPerfil = data.reportePorPerfil || [];
    const evolucion = data.evolucionMensual || [];

    const tbody = document.getElementById("tablaReportePerfilBody");
    tbody.innerHTML = porPerfil.map(r => `
        <tr>
            <td class="fw-semibold">${r.perfilMedico}</td>
            <td class="text-center"><span class="badge bg-primary">${r.totalPacientes}</span></td>
            <td class="text-center"><span class="badge bg-secondary">${r.totalMediciones}</span></td>
            <td class="text-center">${r.promedioPeso > 0 ? r.promedioPeso + " kg" : "-"}</td>
            <td class="text-center">${r.promedioFc > 0 ? r.promedioFc + " bpm" : "-"}</td>
            <td class="text-center">${r.promedioGlucosa > 0 ? r.promedioGlucosa + " mg/dL" : "-"}</td>
        </tr>
    `).join("");

    const ctx = document.getElementById("chartReporteMensual");
    if (ctx) {
        if (chartMedicoInstancia) chartMedicoInstancia.destroy();
        chartMedicoInstancia = new Chart(ctx, {
            type: "bar",
            data: {
                labels: evolucion.map(e => e.periodo),
                datasets: [
                    {
                        label: "Glucosa Promedio (mg/dL)",
                        data: evolucion.map(e => e.promedioGlucosa),
                        backgroundColor: "rgba(25, 135, 84, 0.75)"
                    },
                    {
                        label: "Pulso Promedio (bpm)",
                        data: evolucion.map(e => e.promedioFc),
                        backgroundColor: "rgba(220, 53, 69, 0.75)"
                    }
                ]
            },
            options: { responsive: true, maintainAspectRatio: false }
        });
    }
}

async function cargarPacientesMedico(filtro) {
    const pacientes = await apiFetch(`/api/medico/pacientes?q=${encodeURIComponent(filtro || "")}`);
    const tbody = document.getElementById("tablaPacientesBody");
    if (!pacientes || pacientes.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3" class="text-center text-muted py-3">No se encontraron pacientes.</td></tr>`;
        return;
    }

    tbody.innerHTML = pacientes.map(p => `
        <tr>
            <td>
                <div class="fw-bold">${p.nombre}</div>
                <div class="text-muted small">${p.correo}</div>
            </td>
            <td><span class="badge bg-info text-dark">${p.perfilMedico || "SIN_CUESTIONARIO"}</span></td>
            <td class="text-end">
                <button class="btn btn-primary btn-sm" onclick="verFichaPaciente(${p.idUsuario})">
                    <i class="bi bi-folder2-open me-1"></i>Ver Ficha JOIN
                </button>
            </td>
        </tr>
    `).join("");
}

window.verFichaPaciente = async function(idUsuario) {
    try {
        const ficha = await apiFetch(`/api/medico/ficha/${idUsuario}`);
        document.getElementById("fichaVaciaPlaceholder").classList.add("d-none");
        document.getElementById("fichaPacienteContenido").classList.remove("d-none");

        document.getElementById("fichaIdUsuario").value = ficha.idUsuario;
        document.getElementById("fichaNombrePaciente").textContent = ficha.nombre;
        document.getElementById("fichaDatosPaciente").textContent = `${ficha.correo} | Género: ${ficha.genero} | Nac: ${ficha.fechaNacimiento}`;
        document.getElementById("fichaPerfilBadge").textContent = ficha.nombreLegiblePerfil;

        document.getElementById("fichaImc").textContent = ficha.imc > 0 ? ficha.imc : "--";
        document.getElementById("fichaFC").textContent = ficha.promedioCardiaco > 0 ? ficha.promedioCardiaco + " bpm" : "--";
        document.getElementById("fichaGlucosa").textContent = ficha.promedioGlucosa > 0 ? ficha.promedioGlucosa + " mg/dL" : "--";

        const filasJoin = ficha.historialJoin || [];
        const tbody = document.getElementById("tablaFichaJoinBody");
        if (filasJoin.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">El paciente aún no registra mediciones.</td></tr>`;
        } else {
            tbody.innerHTML = filasJoin.map(f => {
                const fecha = f.fechaHora ? new Date(f.fechaHora).toLocaleString("es-UY") : "-";
                return `
                    <tr>
                        <td>${fecha}</td>
                        <td>${f.peso != null ? f.peso + "kg" : "-"} / ${f.altura != null ? f.altura + "m" : "-"}</td>
                        <td>${f.frecuenciaCardiaca != null ? f.frecuenciaCardiaca + " bpm" : "-"}</td>
                        <td>${f.glucosaSangre != null ? f.glucosaSangre + " mg/dL" : "-"}</td>
                        <td>${f.recomendacionMensaje ? `<span class="badge bg-secondary me-1">${f.recomendacionTipo}</span>${f.recomendacionMensaje}` : '<span class="text-muted">Sin alerta</span>'}</td>
                    </tr>
                `;
            }).join("");
        }
    } catch (err) {
        mostrarAlerta(err.message, "danger");
    }
};
