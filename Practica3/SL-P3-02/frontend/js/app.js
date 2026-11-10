const tabNombre = document.getElementById("tab-nombre");
const tabCinta = document.getElementById("tab-cinta");

const formNombre = document.getElementById("form-nombre");
const formCinta = document.getElementById("form-cinta");

const resultados = document.getElementById("resultados");
const estado = document.getElementById("estado");
const contadorResultados = document.getElementById("results-count");

const totalRegistros = document.getElementById("total-registros");

const loadingSpinner = document.getElementById("loading-spinner");

let buscando = false;

function cambiarPestana(tipo) {
    if (buscando) return;

    const esNombre = tipo === "nombre";

    // Actualizar la pestaña activa
    tabNombre.classList.toggle("active", esNombre);
    tabCinta.classList.toggle("active", !esNombre);

    // Actualizar atributos de accesibilidad
    tabNombre.setAttribute("aria-pressed", String(esNombre));
    tabCinta.setAttribute("aria-pressed", String(!esNombre));

    // Mostrar el formulario correspondiente
    formNombre.hidden = !esNombre;
    formCinta.hidden = esNombre;

    // Limpiar los campos de búsqueda
    formNombre.reset();
    formCinta.reset();

    // Limpiar resultados anteriores
    resultados.replaceChildren();
    contadorResultados.textContent = "";

    // Restaurar el mensaje inicial
    estado.textContent =
        "Realiza una búsqueda para consultar los programas.";

    estado.classList.remove("error-message");
}

tabNombre.addEventListener("click", () => {
    cambiarPestana("nombre");
});

tabCinta.addEventListener("click", () => {
    cambiarPestana("cinta");
});

async function cargarTotalRegistros() {
    totalRegistros.textContent = "Cargando...";

    try {
        const response = await fetch("/api/registros");

        if (!response.ok) {
            throw new Error(
                `Error HTTP: ${response.status}`
            );
        }

        const data = await response.json();

        if (!Number.isInteger(data.totalRegistros)
            || data.totalRegistros < 0) {
            throw new Error("Respuesta inválida del servidor");
        }

        totalRegistros.textContent = data.totalRegistros;

    } catch (error) {
        console.error("Error al cargar los registros:", error);

        totalRegistros.textContent = "No disponible";
    }
}

cargarTotalRegistros();

async function consultarJuegos(tipo, valor) {

    const endpoint = tipo === "nombre"
        ? `/api/nombre?q=${encodeURIComponent(valor)}`
        : `/api/cinta?id=${encodeURIComponent(valor)}`;

    const response = await fetch(endpoint);

    if (!response.ok) {
        throw new Error(
            `La consulta ha fallado (HTTP ${response.status})`
        );
    }

    const datos = await response.json();

    if (!Array.isArray(datos)) {
        throw new Error("Respuesta inválida del servidor");
    }

    return datos;
}

function mostrarResultados(juegos) {
    resultados.replaceChildren();

    contadorResultados.textContent =
        `${juegos.length} resultado${juegos.length === 1 ? "" : "s"}`;

    if (juegos.length === 0) {
        estado.textContent =
            "No se encontraron programas para esta consulta.";
        return;
    }

    estado.textContent = "";

    const tabla = document.createElement("table");
    tabla.className = "results-table";

    const thead = document.createElement("thead");
    const filaCabecera = document.createElement("tr");

    const columnas = [
        ["numero", "Nº"],
        ["nombre", "Nombre"],
        ["tipo", "Tipo"],
        ["cinta", "Cinta"],
        ["registro", "Registro"]
    ];

    columnas.forEach(([, titulo]) => {
        const th = document.createElement("th");
        th.textContent = titulo;
        th.scope = "col";
        filaCabecera.appendChild(th);
    });

    thead.appendChild(filaCabecera);

    const tbody = document.createElement("tbody");

    juegos.forEach(juego => {
        const fila = document.createElement("tr");

        columnas.forEach(([campo]) => {
            const celda = document.createElement("td");
            celda.textContent = juego[campo] ?? "—";
            fila.appendChild(celda);
        });

        tbody.appendChild(fila);
    });

    tabla.append(thead, tbody);
    resultados.appendChild(tabla);
}

function establecerCargando(cargando) {
    buscando = cargando;

    loadingSpinner.hidden = !cargando;

    tabNombre.disabled = cargando;
    tabCinta.disabled = cargando;

    formNombre.querySelector("button").disabled = cargando;
    formCinta.querySelector("button").disabled = cargando;

    formNombre.querySelector("input").disabled = cargando;
    formCinta.querySelector("input").disabled = cargando;
}

async function realizarBusqueda(tipo, valor) {
    if (buscando) return;

    establecerCargando(true);

    resultados.replaceChildren();
    contadorResultados.textContent = "";

    estado.classList.remove("error-message");
    estado.textContent = "Consultando el sistema legado...";

    try {
        const juegos = await consultarJuegos(tipo, valor);
        mostrarResultados(juegos);

    } catch (error) {
        console.error("Error durante la búsqueda:", error);

        if (error instanceof TypeError) {
            estado.textContent =
                "No se puede conectar con el servidor. Comprueba que la aplicación esté iniciada.";
        } else {
            estado.textContent =
                "No se ha podido completar la consulta. Comprueba el estado del servidor e inténtalo de nuevo.";
        }

        estado.classList.add("error-message");

    } finally {
        establecerCargando(false);
    }
}

formNombre.addEventListener("submit", event => {
    event.preventDefault();

    const nombre = document.getElementById("nombre").value.trim();

    if (!nombre) return;

    realizarBusqueda("nombre", nombre);
});

formCinta.addEventListener("submit", event => {
    event.preventDefault();

    const cinta = document.getElementById("cinta").value.trim();

    if (!cinta) return;

    realizarBusqueda("cinta", cinta);
});