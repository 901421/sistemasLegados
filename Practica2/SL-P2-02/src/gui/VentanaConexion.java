package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import mainframe.MainframeClient;
import mainframe.SesionMusic;
import tareas.GestorTareas;

/**
 * Ventana inicial (decisión D1): host, puerto, usuario y clave ya
 * rellenados y editables, y el botón "Conectar". La conexión se hace en
 * segundo plano; si falla, desconecta, muestra el error y deja corregir los
 * datos. Si va bien, abre {@link VentanaTareas} y se cierra.
 */
public class VentanaConexion extends JFrame {

    /** Lo necesario para trabajar con tareas.c tras un login correcto. */
    public record Conexion(MainframeClient cliente, SesionMusic sesion, GestorTareas gestor) {
    }

    /** Conecta, hace login y arranca tareas.c, o lanza el error para el usuario. */
    @FunctionalInterface
    public interface Conector {
        Conexion conectar(String host, int puerto, String usuario, String clave) throws IOException;
    }

    private static final Color COLOR_ERROR = new Color(0xB00020);

    private final Conector conector;

    // Valores por defecto de D1.
    private final JTextField host = new JTextField("155.210.152.51", 15);
    private final JTextField puerto = new JTextField("3270", 15);
    private final JTextField usuario = new JTextField("grupo_02", 15);
    private final JPasswordField clave = new JPasswordField("secreto6", 15);
    private final JButton botonConectar = new JButton("Conectar");
    private final JProgressBar indicador = new JProgressBar();
    private final JLabel estado = new JLabel(" ");

    private boolean conectando = false;
    /** Se cerró la ventana durante la conexión: se sale al acabar. */
    private boolean salirAlTerminar = false;

    /** Ventana que conecta con el mainframe de verdad. */
    public VentanaConexion() {
        this(VentanaConexion::conectarAlMainframe);
    }

    /** Ventana con otra forma de conectar (por ejemplo, una simulada para pruebas). */
    public VentanaConexion(Conector conector) {
        super("Conexión");
        this.conector = conector;

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBorder(new EmptyBorder(10, 10, 0, 10));
        anadirFila(campos, 0, "Host", host);
        anadirFila(campos, 1, "Puerto", puerto);
        anadirFila(campos, 2, "Usuario", usuario);
        anadirFila(campos, 3, "Clave", clave);

        indicador.setIndeterminate(true);
        indicador.setVisible(false);
        JPanel boton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boton.add(indicador);
        boton.add(botonConectar);

        estado.setBorder(new EmptyBorder(0, 10, 10, 10));

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.add(boton, BorderLayout.NORTH);
        abajo.add(estado, BorderLayout.SOUTH);

        add(campos, BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);

        // Intro en cualquier campo equivale a pulsar "Conectar".
        getRootPane().setDefaultButton(botonConectar);
        botonConectar.addActionListener(e -> conectar());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                pedirSalida();
            }
        });

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private static void anadirFila(JPanel panel, int fila, String etiqueta, JTextField campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(3, 3, 3, 3);
        c.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(campo, c);
    }

    private void conectar() {
        if (conectando) {
            return;
        }
        String h = host.getText().strip();
        String u = usuario.getText().strip();
        String k = new String(clave.getPassword());
        int p;
        try {
            p = Integer.parseInt(puerto.getText().strip());
        } catch (NumberFormatException e) {
            p = -1;
        }
        if (h.isEmpty() || u.isEmpty() || k.isEmpty()) {
            mostrarAviso("Rellena host, usuario y clave.");
            return;
        }
        if (p < 1 || p > 65535) {
            mostrarAviso("El puerto debe ser un número entre 1 y 65535.");
            return;
        }
        int numeroPuerto = p;

        setConectando(true);
        new SwingWorker<Conexion, Void>() {
            @Override
            protected Conexion doInBackground() throws IOException {
                return conector.conectar(h, numeroPuerto, u, k);
            }

            @Override
            protected void done() {
                setConectando(false);
                try {
                    Conexion conexion = get();
                    if (salirAlTerminar) {
                        cerrarYSalir(conexion);
                    } else {
                        abrirTareas(conexion);
                    }
                } catch (ExecutionException e) {
                    if (salirAlTerminar) {
                        salir();
                    } else {
                        mostrarError(e.getCause());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void abrirTareas(Conexion conexion) {
        new VentanaTareas(conexion.cliente(), conexion.sesion(), conexion.gestor()).setVisible(true);
        dispose();
    }

    /** La X: sale ya o, si se está conectando, al acabar. */
    private void pedirSalida() {
        if (conectando) {
            salirAlTerminar = true;
            estado.setForeground(getForeground());
            estado.setText("Saliendo al terminar...");
            return;
        }
        salir();
    }

    private void salir() {
        dispose();
        System.exit(0);
    }

    /** Se cerró la ventana mientras se entraba: cierra la sesión en orden antes de salir. */
    private void cerrarYSalir(Conexion conexion) {
        setConectando(true);
        estado.setText("Cerrando sesión...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws IOException {
                try {
                    conexion.sesion().cerrar();
                } finally {
                    conexion.cliente().desconectar();
                }
                return null;
            }

            @Override
            protected void done() {
                salir();
            }
        }.execute();
    }

    private void setConectando(boolean conectando) {
        this.conectando = conectando;
        for (JTextField campo : new JTextField[] {host, puerto, usuario, clave}) {
            campo.setEnabled(!conectando);
        }
        botonConectar.setEnabled(!conectando);
        indicador.setVisible(conectando);
        estado.setForeground(getForeground());
        estado.setText(conectando ? (salirAlTerminar ? "Saliendo al terminar..." : "Conectando...") : " ");
        setCursor(conectando ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        pack();
    }

    /** Datos mal escritos: en rojo bajo el botón, sin cuadro de diálogo. */
    private void mostrarAviso(String mensaje) {
        estado.setForeground(COLOR_ERROR);
        estado.setText(mensaje);
        pack();
    }

    private void mostrarError(Throwable causa) {
        String mensaje = causa.getMessage() != null ? causa.getMessage() : causa.toString();
        if (mensaje.contains("Hercules")) {
            JOptionPane.showMessageDialog(this,
                    mensaje + "\nNo vuelvas a intentarlo: avisa al profesor.",
                    "Mainframe bloqueado", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, mensaje, "No se ha podido entrar", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Conexión real: conecta, hace login y arranca tareas.c. Si algo falla
     * después de conectar, desconecta (respetando el tiempo mínimo) antes de
     * devolver el error.
     */
    private static Conexion conectarAlMainframe(String host, int puerto, String usuario, String clave)
            throws IOException {
        MainframeClient cliente = new MainframeClient();
        try {
            try {
                cliente.conectar(host, puerto);
            } catch (IOException e) {
                if (e.getMessage() != null && e.getMessage().contains("Hercules")) {
                    throw e;
                }
                throw new IOException("No se ha podido conectar con " + host + ":" + puerto
                        + ". Comprueba la red y los datos.\n(" + e.getMessage() + ")", e);
            }
            SesionMusic sesion = new SesionMusic(cliente);
            sesion.iniciar(usuario, clave);
            sesion.arrancarTareas();
            return new Conexion(cliente, sesion, new GestorTareas(cliente));
        } catch (IOException | RuntimeException e) {
            cliente.desconectar();
            throw e;
        }
    }
}
