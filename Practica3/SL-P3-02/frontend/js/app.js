/*
 * Página web de la práctica 3: consulta la aplicación legada a través de la API
 * del servidor (Java). Cada consulta llega a la aplicación GW-BASIC en ese momento:
 * aquí no se guarda ningún dato (R8).
 *
 * fetch() es la forma en que el navegador pide datos al servidor sin recargar la
 * página. Las respuestas son JSON:
 *   /api/registros        → {"totalRegistros": 763, "pantalla": [25 líneas]}
 *   /api/nombre?q=...     → {"juegos": [...], "pantalla": [...]}
 *   /api/cinta?id=...     → {"juegos": [...], "pantalla": [...]}
 *   /api/pantalla         → {"pantalla": [...], "hace": 120, "operando": true} (en directo)
 *   si algo falla         → {"error": "mensaje", "tipo": "entrada" | "ocupado" | "legado" | "interno"}
 */

const tabNombre = document.getElementById("tab-nombre");
const tabCinta = document.getElementById("tab-cinta");

const formNombre = document.getElementById("form-nombre");
const formCinta = document.getElementById("form-cinta");

const resultados = document.getElementById("resultados");
const estado = document.getElementById("estado");
const contadorResultados = document.getElementById("results-count");

const totalRegistros = document.getElementById("total-registros");
const horaRegistros = document.getElementById("hora-registros");
const botonActualizar = document.getElementById("actualizar-registros");

const loadingSpinner = document.getElementById("loading-spinner");
const pantalla = document.getElementById("pantalla");
const pantallaPie = document.getElementById("pantalla-pie");
const pantallaPanel = document.querySelector(".legacy-screen");

let buscando = false;

/*
 * Caracteres que no se pueden enviar a la aplicación: la coma y las comillas
 * (en un INPUT de BASIC separan y delimitan valores) y los que Robot no puede
 * escribir en DOSBox con el teclado español (comprobado en el apartado 4). El
 * servidor vuelve a comprobarlo con el teclado real; esto solo avisa antes.
 */
const NO_ENVIABLES = ",\"[\\]^`{|}~#@ñÑºªç";
const MAX_NOMBRE = 30;

/** Un error de la API, con su tipo para mostrarlo como aviso o como error. */
class ErrorConsulta extends Error {
    constructor(mensaje, tipo) {
        super(mensaje);
        this.tipo = tipo;
    }
}

/* ---- Pestañas ------------------------------------------------------------ */

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

    // Limpiar los campos de búsqueda y los resultados anteriores
    formNombre.reset();
    formCinta.reset();
    resultados.replaceChildren();
    contadorResultados.textContent = "";
    mostrarEstado(esNombre
        ? "Escribe el nombre de un programa, o una parte."
        : "Escribe la letra o el número de una cinta.");

    (esNombre ? formNombre : formCinta).querySelector("input").focus();
}

tabNombre.addEventListener("click", () => cambiarPestana("nombre"));
tabCinta.addEventListener("click", () => cambiarPestana("cinta"));

/*
 * Ejemplos para empezar: cambian a la pestaña, escriben el valor y lanzan la
 * consulta como si se hubiera pulsado el botón (con la misma validación).
 */
const ejemplos = document.querySelectorAll(".example");
ejemplos.forEach(boton => boton.addEventListener("click", () => {
    if (buscando) return;
    const tipo = boton.dataset.tipo;
    cambiarPestana(tipo);
    const form = tipo === "nombre" ? formNombre : formCinta;
    form.querySelector("input").value = boton.dataset.valor;
    form.requestSubmit();
}));

/* ---- Llamadas a la API ------------------------------------------------------ */

/**
 * Pide una dirección de la API y devuelve el JSON. Si el servidor responde con
 * un error, lanza ErrorConsulta con su mensaje (D7); si no se puede conectar,
 * con un mensaje propio.
 */
async function pedir(direccion) {
    let response;
    try {
        response = await fetch(direccion);
    } catch (e) {
        throw new ErrorConsulta(
            "No se puede conectar con el servidor. Comprueba que la aplicación está en marcha.", "conexion");
    }

    let datos = null;
    try {
        datos = await response.json();
    } catch (e) {
        // Respuesta que no es JSON (por ejemplo, una dirección mal formada).
    }

    if (!response.ok) {
        const mensaje = datos && datos.error
            ? datos.error
            : `La consulta ha fallado (HTTP ${response.status}).`;
        throw new ErrorConsulta(mensaje, datos && datos.tipo ? datos.tipo : "interno");
    }
    return datos;
}

/* ---- F1: número de registros, siempre visible (R9, D4) ---------------------- */

/**
 * Pide el número de registros a la aplicación y lo pone en el contador con la
 * hora. La pantalla de la aplicación solo se cambia si se pide (al cargar la
 * página y con "Actualizar"): tras una búsqueda se queda la de la búsqueda.
 */
async function cargarTotalRegistros(conPantalla) {
    botonActualizar.disabled = true;
    horaRegistros.textContent = "Leyendo la aplicación…";

    try {
        const data = await pedir("/api/registros");

        if (!Number.isInteger(data.totalRegistros) || data.totalRegistros < 0) {
            throw new ErrorConsulta("Respuesta inválida del servidor.", "interno");
        }

        mostrarContador(data.totalRegistros);
        horaRegistros.textContent = "Leído de la aplicación a las " + new Date().toLocaleTimeString("es-ES");
        if (conPantalla) mostrarPantalla(data.pantalla);

    } catch (error) {
        console.error("Error al cargar los registros:", error);
        totalRegistros.textContent = "—";
        totalRegistros.removeAttribute("aria-label");
        horaRegistros.textContent = "No disponible: " + error.message;
    } finally {
        botonActualizar.disabled = buscando;
    }
}

/**
 * Pone el número en el contador, una cifra por ventanita, como el contador de
 * un reproductor de casete. Las cifras ruedan al cambiar, salvo que el sistema
 * pida reducir el movimiento.
 */
function mostrarContador(numero) {
    const cifras = String(numero).padStart(3, "0").split("");
    totalRegistros.replaceChildren(...cifras.map((cifra, i) => {
        const ventana = document.createElement("span");
        ventana.className = "tape-counter__digit";
        ventana.style.setProperty("--retraso", `${i * 70}ms`);
        ventana.textContent = cifra;
        return ventana;
    }));
    totalRegistros.setAttribute("aria-label", `${numero} programas`);
}

botonActualizar.addEventListener("click", () => {
    if (!buscando) cargarTotalRegistros(true);
});

cargarTotalRegistros(true);

/* ---- Pantalla de la aplicación (D9) ----------------------------------------- */

/** Muestra las 25 líneas de la pantalla de la aplicación que ha leído el wrapper. */
function mostrarPantalla(lineas) {
    if (!Array.isArray(lineas)) return;
    pantalla.textContent = lineas.map(l => l.replace(/\s+$/, "")).join("\n");
}

const PIE_ULTIMA = "La última pantalla que ha leído el wrapper en DOSBox para la consulta.";
const PIE_DIRECTO = "En directo: lo que está mostrando ahora la aplicación en DOSBox mientras el wrapper la maneja.";
const ESPERA_DIRECTO_MS = 300;
let enDirecto = false;

/**
 * Mientras dura una consulta, pide cada poco la última captura que ha hecho el
 * wrapper y la pinta: se ve la aplicación navegando por sus menús y pantallas
 * (D9). No añade trabajo a DOSBox: son las capturas que el wrapper ya hace.
 */
async function seguirEnDirecto() {
    enDirecto = true;
    pantallaPanel.classList.add("legacy-screen--live");
    pantallaPie.textContent = PIE_DIRECTO;
    while (enDirecto) {
        try {
            const datos = await pedir("/api/pantalla");
            if (enDirecto && datos.pantalla) mostrarPantalla(datos.pantalla);
        } catch (e) {
            // Si una petición falla, se sigue con la siguiente.
        }
        await new Promise(r => setTimeout(r, ESPERA_DIRECTO_MS));
    }
}

/** Deja de seguir en directo y muestra la pantalla final de la consulta. */
function pararDirecto(lineasFinales) {
    enDirecto = false;
    pantallaPanel.classList.remove("legacy-screen--live");
    pantallaPie.textContent = PIE_ULTIMA;
    mostrarPantalla(lineasFinales);
}

/* ---- F2 y F3 ---------------------------------------------------------------- */

/** Comprueba el nombre como el servidor, para avisar antes de enviarlo. Devuelve el error o null. */
function errorNombre(nombre) {
    if (!nombre) return "Escribe el nombre del programa (o parte de él).";
    if (nombre.length > MAX_NOMBRE) return `El nombre puede tener como mucho ${MAX_NOMBRE} caracteres.`;
    for (const c of nombre) {
        if (c === ",") return "El nombre no puede llevar comas: la aplicación legada no lo recibiría tal cual.";
        if (c === "\"") return "El nombre no puede llevar comillas: la aplicación legada no lo recibiría tal cual.";
        if (NO_ENVIABLES.includes(c) || c.charCodeAt(0) > 126) {
            return `El carácter '${c}' no se puede escribir en la aplicación legada.`;
        }
    }
    return null;
}

/** Tabla de la búsqueda por nombre: las columnas que muestra la aplicación (sin registro). */
function tablaNombre(juegos) {
    const tabla = document.createElement("table");
    tabla.className = "results-table";

    const columnas = [["numero", "Nº"], ["nombre", "Nombre"], ["tipo", "Tipo"], ["cinta", "Cinta"]];

    const thead = document.createElement("thead");
    const filaCabecera = document.createElement("tr");
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
            celda.textContent = juego[campo];
            celda.dataset.campo = campo;
            fila.appendChild(celda);
        });
        tbody.appendChild(fila);
    });

    tabla.append(thead, tbody);
    return tabla;
}

/**
 * Programas de una cinta como la carátula de un casete: numerados en el orden en
 * que están grabados (el de registro, AMB-5). Si un programa está también en
 * otras cintas, se indica.
 */
function caratulaCinta(id, juegos) {
    const caratula = document.createElement("article");
    caratula.className = "j-card";

    // El lomo del casete, como se ve en la estantería: el nombre de la cinta en vertical.
    const lomo = document.createElement("h3");
    lomo.className = "j-card__spine";
    lomo.textContent = `Cinta ${id}`;

    const cuerpo = document.createElement("div");
    cuerpo.className = "j-card__body";
    const pie = document.createElement("p");
    pie.className = "j-card__caption";
    pie.textContent = "En el orden en que están grabados";

    const lista = document.createElement("ol");
    lista.className = "j-card__list";
    juegos.forEach((juego, i) => {
        const item = document.createElement("li");

        // Posición en la cinta: el orden es la información de F3 (R11).
        const posicion = document.createElement("span");
        posicion.className = "j-card__pos";
        posicion.textContent = String(i + 1);

        const nombre = document.createElement("span");
        nombre.className = "j-card__name";
        nombre.textContent = juego.nombre;

        const tipo = document.createElement("span");
        tipo.className = "j-card__type";
        tipo.textContent = juego.tipo;

        const registro = document.createElement("span");
        registro.className = "j-card__record";
        registro.textContent = `registro ${juego.registro}`;

        item.append(posicion, nombre, tipo, registro);

        const otras = juego.cinta.split("-").filter(c => c !== id);
        if (otras.length > 0) {
            const tambien = document.createElement("span");
            tambien.className = "j-card__also";
            tambien.textContent = `también en ${otras.length === 1 ? "la cinta" : "las cintas"} ${otras.join(", ")}`;
            item.appendChild(tambien);
        }
        lista.appendChild(item);
    });

    cuerpo.append(pie, lista);
    caratula.append(lomo, cuerpo);
    return caratula;
}

function mostrarResultados(tipo, valor, juegos) {
    resultados.replaceChildren();
    contadorResultados.textContent = `${juegos.length} programa${juegos.length === 1 ? "" : "s"}`;

    if (juegos.length === 0) {
        mostrarEstado(tipo === "nombre"
            ? `Ningún programa contiene «${valor}» en su nombre. Prueba con una parte más corta o revisa las mayúsculas.`
            : `La cinta ${valor} no tiene ningún programa en la base de datos.`);
        return;
    }

    mostrarEstado(tipo === "nombre"
        ? `Programas cuyo nombre contiene «${valor}», en el orden en que los da la aplicación.`
        : "");
    resultados.appendChild(tipo === "nombre" ? tablaNombre(juegos) : caratulaCinta(valor, juegos));
}

/** Mensaje bajo el título de resultados: normal, aviso (ocupado) o error. */
function mostrarEstado(texto, clase) {
    estado.textContent = texto;
    estado.classList.remove("error-message", "warning-message");
    if (clase) estado.classList.add(clase);
    estado.hidden = texto === "";
}

function establecerCargando(cargando) {
    buscando = cargando;

    loadingSpinner.hidden = !cargando;

    tabNombre.disabled = cargando;
    tabCinta.disabled = cargando;
    botonActualizar.disabled = cargando;

    formNombre.querySelector("button").disabled = cargando;
    formCinta.querySelector("button").disabled = cargando;

    formNombre.querySelector("input").disabled = cargando;
    formCinta.querySelector("input").disabled = cargando;

    ejemplos.forEach(boton => { boton.disabled = cargando; });
}

async function realizarBusqueda(tipo, valor) {
    if (buscando) return;

    establecerCargando(true);
    resultados.replaceChildren();
    contadorResultados.textContent = "";
    mostrarEstado("");
    seguirEnDirecto();
    let final = null;

    try {
        const endpoint = tipo === "nombre"
            ? `/api/nombre?q=${encodeURIComponent(valor)}`
            : `/api/cinta?id=${encodeURIComponent(valor)}`;
        const datos = await pedir(endpoint);
        if (!Array.isArray(datos.juegos)) throw new ErrorConsulta("Respuesta inválida del servidor.", "interno");
        mostrarResultados(tipo, valor, datos.juegos);
        final = datos.pantalla;

    } catch (error) {
        console.error("Error durante la búsqueda:", error);
        mostrarEstado(error.message, error.tipo === "ocupado" ? "warning-message" : "error-message");

    } finally {
        pararDirecto(final);
        establecerCargando(false);
        // Después de cada consulta, el número de registros se vuelve a pedir (D4).
        cargarTotalRegistros();
    }
}

formNombre.addEventListener("submit", event => {
    event.preventDefault();

    const nombre = document.getElementById("nombre").value.trim();
    const error = errorNombre(nombre);
    if (error) {
        mostrarEstado(error, "error-message");
        return;
    }

    realizarBusqueda("nombre", nombre);
});

formCinta.addEventListener("submit", event => {
    event.preventDefault();

    const cinta = document.getElementById("cinta")
        .value.trim().toUpperCase();

    if (!/^(?:[A-Z]|\d+)$/.test(cinta)) {
        mostrarEstado("El identificador de cinta debe ser una letra (A-Z) o un número.", "error-message");
        return;
    }

    realizarBusqueda("cinta", cinta);
});
