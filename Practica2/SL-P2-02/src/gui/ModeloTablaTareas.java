package gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import tareas.Tarea;

/** Datos de la tabla de tareas: Nº, tipo, fecha, nombre y descripción. */
public class ModeloTablaTareas extends AbstractTableModel {

    /** Sustituye el contenido de la tabla y la repinta. */
    public void setTareas(List<Tarea> tareas) {
        throw new UnsupportedOperationException("Pendiente");
    }

    @Override
    public int getRowCount() {
        throw new UnsupportedOperationException("Pendiente");
    }

    @Override
    public int getColumnCount() {
        throw new UnsupportedOperationException("Pendiente");
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        throw new UnsupportedOperationException("Pendiente");
    }
}
