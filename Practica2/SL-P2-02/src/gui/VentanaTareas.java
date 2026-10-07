package gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;

import mainframe.MainframeClient;
import mainframe.SesionMusic;
import tareas.GestorTareas;
import tareas.Tarea;

/**
 * Ventana principal "Tareas": formulario de alta ({@link PanelAlta}), tabla
 * de tareas ({@link ModeloTablaTareas}) y botones "Actualizar" y "Salir".
 * Las operaciones con el mainframe se hacen en segundo plano, de una en una,
 * con la ventana activa y un indicador. Al salir o cerrar con la X se cierra
 * la sesión, se desconecta y termina el programa; si había una operación en
 * curso, se espera a que acabe.
 */
public class VentanaTareas extends JFrame {

    /** Una operación con el mainframe que termina devolviendo el listado actualizado. */
    @FunctionalInterface
    private interface Operacion {
        List<Tarea> ejecutar() throws IOException;
    }

    private final MainframeClient cliente;
    private final SesionMusic sesion;
    private final GestorTareas gestor;

    private final ModeloTablaTareas modelo = new ModeloTablaTareas();
    private final PanelAlta panelAlta = new PanelAlta(this::nueva);
    private final JButton botonActualizar = new JButton("Actualizar");
    private final JButton botonSalir = new JButton("Salir");
    private final JLabel estado = new JLabel(" ");
    private final JProgressBar indicador = new JProgressBar();

    /** Hay una operación con el mainframe en curso. */
    private boolean ocupada = false;
    /** Se pidió salir durante una operación: se sale al acabarla. */
    private boolean salirAlTerminar = false;
    /** Ya se está cerrando: no se aceptan más órdenes. */
    private boolean cerrando = false;

    /**
     * @param cliente conexión ya abierta
     * @param sesion  sesión con login hecho y tareas.c en su menú principal
     * @param gestor  operaciones de tareas.c sobre esa sesión
     */
    public VentanaTareas(MainframeClient cliente, SesionMusic sesion, GestorTareas gestor) {
        super("Tareas");
        this.cliente = cliente;
        this.sesion = sesion;
        this.gestor = gestor;

        JTable tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(60);

        indicador.setIndeterminate(true);
        indicador.setVisible(false);

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        izquierda.add(indicador);
        izquierda.add(estado);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        derecha.add(botonActualizar);
        derecha.add(botonSalir);
        JPanel abajo = new JPanel(new BorderLayout());
        abajo.add(izquierda, BorderLayout.CENTER);
        abajo.add(derecha, BorderLayout.EAST);

        add(panelAlta, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);

        botonActualizar.addActionListener(e -> actualizar());
        botonSalir.addActionListener(e -> pedirSalida());

        // La X no cierra directamente: pasa por el mismo cierre ordenado que "Salir".
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                actualizar();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                pedirSalida();
            }
        });

        setSize(760, 480);
        setLocationRelativeTo(null);
    }

    private void actualizar() {
        ejecutar("Leyendo tareas...", gestor::listar, () -> { });
    }

    /** Llamado por el formulario con datos ya validados. */
    private void nueva(String fecha, String nombre, String descripcion) {
        ejecutar("Creando tarea...", () -> {
            gestor.nueva(fecha, nombre, descripcion);
            return gestor.listar();
        }, panelAlta::limpiar);
    }

    /**
     * Lanza una operación en segundo plano y, al acabar, ya en el hilo de la
     * ventana, muestra el listado o el error. Si mientras tanto se pidió
     * salir, sale.
     */
    private void ejecutar(String mensaje, Operacion operacion, Runnable siVaBien) {
        if (ocupada || cerrando) {
            return;
        }
        setOcupada(true, mensaje);

        new SwingWorker<List<Tarea>, Void>() {
            @Override
            protected List<Tarea> doInBackground() throws IOException {
                return operacion.ejecutar();
            }

            @Override
            protected void done() {
                try {
                    modelo.setTareas(get());
                    siVaBien.run();
                } catch (ExecutionException e) {
                    mostrarError("No se ha podido completar la operación", e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    setOcupada(false, " ");
                    if (salirAlTerminar) {
                        salir();
                    }
                }
            }
        }.execute();
    }

    /** "Salir" o la X: sale ya o, si hay una operación en curso, al acabarla. */
    private void pedirSalida() {
        if (cerrando) {
            return;
        }
        if (ocupada) {
            salirAlTerminar = true;
            estado.setText("Saliendo al terminar...");
            return;
        }
        salir();
    }

    /**
     * Cierra tareas.c y la sesión, desconecta y termina el programa. La
     * desconexión va en un finally: se hace aunque falle el cierre de sesión.
     */
    private void salir() {
        cerrando = true;
        setOcupada(true, "Cerrando sesión...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws IOException {
                try {
                    sesion.cerrar();
                } finally {
                    cliente.desconectar();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (ExecutionException e) {
                    mostrarError("La sesión no se ha cerrado en orden (la conexión sí)", e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    dispose();
                    System.exit(0);
                }
            }
        }.execute();
    }

    /** Activa o desactiva los controles y muestra el indicador. */
    private void setOcupada(boolean ocupada, String mensaje) {
        this.ocupada = ocupada;
        panelAlta.setActivo(!ocupada);
        botonActualizar.setEnabled(!ocupada);
        // "Salir" sigue activo durante una operación (sale al acabar), pero no al cerrar.
        botonSalir.setEnabled(!cerrando);
        indicador.setVisible(ocupada);
        estado.setText(salirAlTerminar && ocupada && !cerrando ? "Saliendo al terminar..." : mensaje);
        setCursor(ocupada ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void mostrarError(String titulo, Throwable causa) {
        String mensaje = causa.getMessage() != null ? causa.getMessage() : causa.toString();
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }
}
