package gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import tareas.Tarea;

/**
 * Datos de la tabla de tareas: Nº, tipo, fecha, nombre y descripción.
 * Se usa solo desde el hilo de la ventana, como todo lo de Swing.
 */
public class ModeloTablaTareas extends AbstractTableModel {

    public static final int COLUMNA_NUMERO = 0;
    public static final int COLUMNA_TIPO = 1;
    public static final int COLUMNA_FECHA = 2;

    private static final String[] COLUMNAS = {"Nº", "Tipo", "Fecha", "Nombre", "Descripción"};

    private static final String[] MESES = {
        "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};

    /**
     * Fecha de una tarea tal como la guarda tareas.c (DDMM). Se muestra como
     * "12 oct" y se ordena por mes y día. Si no es una fecha DDMM válida
     * (tareas.c no lo comprueba), se muestra tal cual y va al final.
     */
    public record Fecha(String ddmm) implements Comparable<Fecha> {

        /** Mes y día en un número (1012 para el 12 de octubre), o -1 si no es DDMM. */
        int clave() {
            if (ddmm == null || !ddmm.matches("\\d{4}")) {
                return -1;
            }
            int dia = Integer.parseInt(ddmm.substring(0, 2));
            int mes = Integer.parseInt(ddmm.substring(2));
            return dia >= 1 && dia <= 31 && mes >= 1 && mes <= 12 ? mes * 100 + dia : -1;
        }

        @Override
        public String toString() {
            int clave = clave();
            return clave < 0 ? ddmm : (clave % 100) + " " + MESES[clave / 100 - 1];
        }

        @Override
        public int compareTo(Fecha otra) {
            int a = clave();
            int b = otra.clave();
            if (a < 0 || b < 0) {
                // Las fechas que no son DDMM, al final y en orden de texto.
                return a < 0 && b < 0 ? ddmm.compareTo(otra.ddmm) : (a < 0 ? 1 : -1);
            }
            return Integer.compare(a, b);
        }
    }

    private List<Tarea> tareas = List.of();

    /** Sustituye el contenido de la tabla y la repinta. */
    public void setTareas(List<Tarea> tareas) {
        // Copia propia: si quien la pasó cambia su lista, la tabla no se entera.
        this.tareas = List.copyOf(tareas);
        fireTableDataChanged();
    }

    /** Número de tareas de un tipo, o de todas si el tipo es null. */
    public long contar(Tarea.Tipo tipo) {
        return tipo == null ? tareas.size() : tareas.stream().filter(t -> t.tipo() == tipo).count();
    }

    @Override
    public int getRowCount() {
        return tareas.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int columna) {
        return COLUMNAS[columna];
    }

    /** El Nº es un número y la fecha una {@link Fecha}: así se alinean y se ordenan bien. */
    @Override
    public Class<?> getColumnClass(int columna) {
        return switch (columna) {
            case COLUMNA_NUMERO -> Integer.class;
            case COLUMNA_FECHA -> Fecha.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Tarea tarea = tareas.get(fila);
        return switch (columna) {
            case COLUMNA_NUMERO -> tarea.numero();
            case COLUMNA_TIPO -> tarea.tipo() == Tarea.Tipo.GENERAL ? "General" : "Específica";
            case COLUMNA_FECHA -> new Fecha(tarea.fecha());
            case 3 -> tarea.nombre();
            case 4 -> tarea.descripcion();
            default -> throw new IndexOutOfBoundsException("Columna inexistente: " + columna);
        };
    }
}
