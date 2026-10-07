package gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import tareas.Tarea;

/**
 * Datos de la tabla de tareas: Nº, tipo, fecha, nombre y descripción.
 * Se usa solo desde el hilo de la ventana, como todo lo de Swing.
 */
public class ModeloTablaTareas extends AbstractTableModel {

    private static final String[] COLUMNAS = {"Nº", "Tipo", "Fecha", "Nombre", "Descripción"};

    private List<Tarea> tareas = List.of();

    /** Sustituye el contenido de la tabla y la repinta. */
    public void setTareas(List<Tarea> tareas) {
        // Copia propia: si quien la pasó cambia su lista, la tabla no se entera.
        this.tareas = List.copyOf(tareas);
        fireTableDataChanged();
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

    /** El Nº es un número: así la tabla lo alinea a la derecha. */
    @Override
    public Class<?> getColumnClass(int columna) {
        return columna == 0 ? Integer.class : String.class;
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Tarea tarea = tareas.get(fila);
        return switch (columna) {
            case 0 -> tarea.numero();
            case 1 -> tarea.tipo() == Tarea.Tipo.GENERAL ? "General" : "Específica";
            case 2 -> tarea.fecha();
            case 3 -> tarea.nombre();
            case 4 -> tarea.descripcion();
            default -> throw new IndexOutOfBoundsException("Columna inexistente: " + columna);
        };
    }
}
