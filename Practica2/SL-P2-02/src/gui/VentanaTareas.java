package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import mainframe.MainframeClient;
import mainframe.SesionMusic;
import tareas.GestorTareas;
import tareas.Tarea;
import tareas.Validador;

/**
 * Ventana principal "Tareas": formulario de alta ({@link PanelAlta}), tabla
 * de tareas ({@link ModeloTablaTareas}) con pestañas Todas / Generales /
 * Específicas y buscador, y botones "Actualizar" y "Salir".
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

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final MainframeClient cliente;
    private final SesionMusic sesion;
    private final GestorTareas gestor;

    private final ModeloTablaTareas modelo = new ModeloTablaTareas();
    private final PanelAlta panelAlta = new PanelAlta(this::nueva);
    private final JLabel subtitulo = Estilo.subtitulo(" ");
    private final JTextField buscador = new CampoConPista("Buscar tarea…", 16);
    private final JTabbedPane pestanas = new JTabbedPane();
    /** Una tabla por pestaña, todas sobre el mismo modelo: Todas, Generales y Específicas. */
    private final JTable[] tablas = new JTable[3];
    private static final Tarea.Tipo[] TIPO_PESTANA = {null, Tarea.Tipo.GENERAL, Tarea.Tipo.ESPECIFICA};
    private static final String[] NOMBRE_PESTANA = {"Todas", "Generales", "Específicas"};

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
        setIconImage(Estilo.icono());

        JPanel contenido = new JPanel(new BorderLayout(0, Estilo.MARGEN));
        contenido.setBorder(BorderFactory.createEmptyBorder(
                Estilo.MARGEN, Estilo.MARGEN, Estilo.MARGEN, Estilo.MARGEN));
        contenido.add(crearCabecera(), BorderLayout.NORTH);
        contenido.add(crearCentro(), BorderLayout.CENTER);
        contenido.add(crearBarraInferior(), BorderLayout.SOUTH);
        setContentPane(contenido);

        modelo.addTableModelListener(e -> actualizarPestanas());
        actualizarPestanas();

        // La X no cierra directamente: pasa por el mismo cierre ordenado que "Salir".
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                panelAlta.empezarAEscribir();
                actualizar();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                pedirSalida();
            }
        });

        setSize(880, 640);
        setMinimumSize(new Dimension(720, 520));
        setLocationRelativeTo(null);
    }

    /** Usuario y host de la sesión, para el título y la cabecera. */
    public void setInfoConexion(String usuario, String host) {
        setTitle("Tareas · " + usuario + " @ " + host);
        subtitulo.setText("Sesión de " + usuario + " en " + host + " · aplicación tareas.c en MUSIC/SP");
    }

    // ------------------------------------------------------------------
    // Construcción de la ventana
    // ------------------------------------------------------------------

    private JComponent crearCabecera() {
        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Estilo.titulo("Gestor de tareas"));
        textos.add(Box.createVerticalStrut(2));
        textos.add(subtitulo);

        buscador.setToolTipText("Filtra la tabla por cualquier columna");
        buscador.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltros();
            }
        });
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JLabel etiquetaBuscar = new JLabel("Buscar");
        etiquetaBuscar.setLabelFor(buscador);
        derecha.add(etiquetaBuscar);
        derecha.add(buscador);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(textos, BorderLayout.WEST);
        cabecera.add(derecha, BorderLayout.EAST);
        return cabecera;
    }

    private JComponent crearCentro() {
        for (int i = 0; i < tablas.length; i++) {
            tablas[i] = crearTabla(TIPO_PESTANA[i]);
            pestanas.addTab(NOMBRE_PESTANA[i], new JScrollPane(tablas[i]));
        }
        JPanel centro = new JPanel(new BorderLayout(0, Estilo.MARGEN));
        centro.add(panelAlta, BorderLayout.NORTH);
        centro.add(pestanas, BorderLayout.CENTER);
        return centro;
    }

    /** Tabla que solo enseña las tareas de un tipo (todas si es null), con filas alternas. */
    private JTable crearTabla(Tarea.Tipo tipo) {
        JTable tabla = new JTable(modelo) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int fila, int columna) {
                Component c = super.prepareRenderer(renderer, fila, columna);
                if (!isRowSelected(fila)) {
                    c.setBackground(fila % 2 == 0 ? getBackground() : Estilo.FILA_ALTERNA);
                }
                return c;
            }

            /** Con la tabla vacía, un aviso en el centro en lugar de un hueco en blanco. */
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getRowCount() == 0) {
                    String aviso = modelo.getRowCount() == 0 || buscador.getText().isBlank()
                            ? (ocupada ? "Cargando tareas…" : "No hay tareas")
                            : "Ninguna tarea coincide con la búsqueda";
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(Estilo.TEXTO_SECUNDARIO);
                    int ancho = g2.getFontMetrics().stringWidth(aviso);
                    g2.drawString(aviso, (getWidth() - ancho) / 2, getParent().getHeight() / 2);
                    g2.dispose();
                }
            }
        };
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(tabla.getRowHeight() + 10);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(0xE6E9EF));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);

        int[] anchos = {50, 100, 80, 160, 300};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        // Todo alineado a la izquierda, como las cabeceras, y con margen dentro de la celda.
        DefaultTableCellRenderer celdaConMargen = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                    boolean foco, int fila, int columna) {
                super.getTableCellRendererComponent(t, valor, seleccionada, foco, fila, columna);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return this;
            }
        };
        for (int i = 0; i < modelo.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(celdaConMargen);
        }

        // Clic en la cabecera para ordenar; de entrada, por número.
        TableRowSorter<ModeloTablaTareas> ordenador = new TableRowSorter<>(modelo);
        ordenador.setSortKeys(List.of(new RowSorter.SortKey(
                ModeloTablaTareas.COLUMNA_NUMERO, SortOrder.ASCENDING)));
        ordenador.setSortsOnUpdates(true);
        tabla.setRowSorter(ordenador);
        tabla.putClientProperty(Tarea.Tipo.class, tipo);
        return tabla;
    }

    private JComponent crearBarraInferior() {
        indicador.setIndeterminate(true);
        indicador.setVisible(false);
        indicador.setPreferredSize(new Dimension(90, indicador.getPreferredSize().height));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izquierda.add(indicador);
        izquierda.add(estado);

        botonActualizar.setMnemonic(KeyEvent.VK_A);
        botonActualizar.setToolTipText("Volver a leer las tareas del mainframe (F5)");
        botonSalir.setMnemonic(KeyEvent.VK_S);
        botonSalir.setToolTipText("Cerrar la sesión y salir (Alt+S)");
        botonActualizar.addActionListener(e -> actualizar());
        botonSalir.addActionListener(e -> pedirSalida());

        // F5 actualiza desde cualquier parte de la ventana.
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "actualizar");
        getRootPane().getActionMap().put("actualizar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (botonActualizar.isEnabled()) {
                    actualizar();
                }
            }
        });

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.add(botonActualizar);
        derecha.add(botonSalir);

        JPanel barra = new JPanel(new BorderLayout());
        barra.add(izquierda, BorderLayout.CENTER);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    /** Pone en cada pestaña cuántas tareas tiene. */
    private void actualizarPestanas() {
        for (int i = 0; i < tablas.length; i++) {
            pestanas.setTitleAt(i, NOMBRE_PESTANA[i] + " (" + modelo.contar(TIPO_PESTANA[i]) + ")");
        }
        aplicarFiltros();
    }

    /** Cada tabla enseña su tipo de tarea y lo que coincida con el buscador. */
    private void aplicarFiltros() {
        String texto = buscador.getText().strip().toLowerCase(Locale.ROOT);
        for (JTable tabla : tablas) {
            Tarea.Tipo tipo = (Tarea.Tipo) tabla.getClientProperty(Tarea.Tipo.class);
            @SuppressWarnings("unchecked")
            TableRowSorter<ModeloTablaTareas> ordenador = (TableRowSorter<ModeloTablaTareas>) tabla.getRowSorter();
            ordenador.setRowFilter(new RowFilter<ModeloTablaTareas, Integer>() {
                @Override
                public boolean include(Entry<? extends ModeloTablaTareas, ? extends Integer> fila) {
                    String tipoFila = fila.getStringValue(ModeloTablaTareas.COLUMNA_TIPO);
                    if (tipo == Tarea.Tipo.GENERAL && !tipoFila.equals("General")
                            || tipo == Tarea.Tipo.ESPECIFICA && !tipoFila.equals("Específica")) {
                        return false;
                    }
                    if (texto.isEmpty()) {
                        return true;
                    }
                    for (int c = 0; c < fila.getValueCount(); c++) {
                        if (fila.getStringValue(c).toLowerCase(Locale.ROOT).contains(texto)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            tabla.repaint();
        }
    }

    // ------------------------------------------------------------------
    // Operaciones con el mainframe
    // ------------------------------------------------------------------

    private void actualizar() {
        ejecutar("Leyendo tareas…", gestor::listar,
                () -> mostrarEstado("Actualizado a las " + LocalTime.now().format(HORA), null));
    }

    /** Llamado por el formulario con datos ya validados. */
    private void nueva(String fecha, String nombre, String descripcion) {
        // Con el máximo, tareas.c no pregunta nada: se avisa sin tocar el mainframe.
        if (modelo.contar(null) >= Validador.MAX_TAREAS) {
            panelAlta.mostrarError("tareas.c admite como máximo " + Validador.MAX_TAREAS
                    + " tareas: no se pueden crear más.");
            return;
        }
        ejecutar("Creando tarea…", () -> {
            gestor.nueva(fecha, nombre, descripcion);
            return gestor.listar();
        }, () -> {
            panelAlta.limpiar();
            mostrarEstado("✓ Tarea creada · actualizado a las " + LocalTime.now().format(HORA), Estilo.EXITO);
        });
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
                boolean bien = false;
                try {
                    modelo.setTareas(get());
                    bien = true;
                } catch (ExecutionException e) {
                    mostrarError("No se ha podido completar la operación", e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    setOcupada(false, " ");
                    if (bien) {
                        siVaBien.run();
                    } else {
                        mostrarEstado("La última operación ha fallado", Estilo.ERROR);
                    }
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
            mostrarEstado("Saliendo al terminar…", null);
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
        setOcupada(true, "Cerrando sesión…");

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
        mostrarEstado(salirAlTerminar && ocupada && !cerrando ? "Saliendo al terminar…" : mensaje, null);
        setCursor(ocupada ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        for (JTable tabla : tablas) {
            tabla.repaint();
        }
    }

    /** Texto de la barra inferior; color null para el normal. */
    private void mostrarEstado(String texto, Color color) {
        estado.setText(texto);
        estado.setForeground(color != null ? color : Estilo.TEXTO_SECUNDARIO);
    }

    private void mostrarError(String titulo, Throwable causa) {
        String mensaje = causa.getMessage() != null ? causa.getMessage() : causa.toString();
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }

    /** Campo de texto que, vacío y sin foco, enseña una pista en gris. */
    private static class CampoConPista extends JTextField {
        private final String pista;

        CampoConPista(String pista, int columnas) {
            super(columnas);
            this.pista = pista;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(Estilo.TEXTO_SECUNDARIO);
                Insets m = getInsets();
                g2.drawString(pista, m.left + 2, m.top + g2.getFontMetrics().getAscent());
                g2.dispose();
            }
        }
    }
}
