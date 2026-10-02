package gui;

import javax.swing.JFrame;

/**
 * Ventana inicial (decisión D1): host, puerto, usuario y clave ya
 * rellenados y editables, y el botón "Conectar". Si la conexión o el login
 * fallan, muestra el error y deja corregir los datos.
 */
public class VentanaConexion extends JFrame {

    public VentanaConexion() {
        super("Conexión");
    }
}
