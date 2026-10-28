package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class GestorChat {
    private final GestorXML gestorXML = new GestorXML();
    private static final long TAMANO_MAXIMO = 5 * 1024 * 1024; // 5 MB
    private static final List<String> EXTENSIONES = List.of("jpg", "jpeg", "png", "gif", "pdf", "txt");

    private boolean existeUsuario(String nombre) throws JAXBException {
        return gestorXML.leerUsuarios().getUsuarios().stream()
                .anyMatch(u -> u.getNombre().equalsIgnoreCase(nombre));
    }

    public List<String> listarUsuarios() throws JAXBException {
        return gestorXML.leerUsuarios().getUsuarios().stream()
                .map(Usuario::getNombre)
                .collect(Collectors.toList());
    }

    public void registrarUsuario(String nombre) throws JAXBException {
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        if (existeUsuario(nombre)) {
            throw new IllegalArgumentException("Ese usuario ya existe.");
        }
        ListaUsuarios lista = gestorXML.leerUsuarios();
        lista.getUsuarios().add(new Usuario(nombre));
        gestorXML.guardarUsuarios(lista);
    }

    public void enviarMensaje(String emisor, String destinatario, String contenido) throws JAXBException {
        guardar(crearMensaje(emisor, destinatario, contenido));
    }

    public void enviarConAdjunto(String emisor, String destinatario, String contenido, Path archivo)
            throws JAXBException, IOException {
        Mensaje m = crearMensaje(emisor, destinatario, contenido);
        adjuntarArchivo(m, archivo);
        guardar(m);
    }

    private Mensaje crearMensaje(String emisor, String destinatario, String contenido) throws JAXBException {
        if (!existeUsuario(emisor) || !existeUsuario(destinatario)) {
            throw new IllegalArgumentException("Los dos usuarios tienen que estar registrados.");
        }
        return new Mensaje(emisor, destinatario, contenido, LocalDateTime.now().withNano(0).toString());
    }

    private void guardar(Mensaje m) throws JAXBException {
        ListaMensajes lista = gestorXML.leerMensajes();
        lista.getMensajes().add(m);
        gestorXML.guardarMensajes(lista);
    }

    // Mensajes entre A y B en los dos sentidos
    public List<Mensaje> consultarConversacion(String a, String b) throws JAXBException {
        return gestorXML.leerMensajes().getMensajes().stream()
                .filter(m -> (m.getEmisor().equalsIgnoreCase(a) && m.getDestinatario().equalsIgnoreCase(b))
                        || (m.getEmisor().equalsIgnoreCase(b) && m.getDestinatario().equalsIgnoreCase(a)))
                .collect(Collectors.toList());
    }

    // Mensajes por usuario (Stream)
    public Map<String, Long> estadisticas(String a, String b) throws JAXBException {
        return consultarConversacion(a, b).stream()
                .collect(Collectors.groupingBy(Mensaje::getEmisor, Collectors.counting()));
    }

    // Las 3 palabras más repetidas (Stream)
    public List<String> palabrasComunes(String a, String b) throws JAXBException {
        return consultarConversacion(a, b).stream()
                .flatMap(m -> Arrays.stream(m.getContenido().toLowerCase().split("\\W+")))
                .filter(p -> p.length() > 3)
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.toList());
    }

    // Texto de un mensaje, marcando el adjunto si falta
    public String formatear(Mensaje m) {
        String linea = "[" + m.getFecha() + "] " + m.getEmisor() + ": " + m.getContenido();
        if (m.getAdjuntoRuta() != null) {
            linea += Files.exists(Path.of(m.getAdjuntoRuta()))
                    ? " [adjunto: " + m.getAdjuntoNombre() + "]"
                    : " [ADJUNTO NO ENCONTRADO: " + m.getAdjuntoNombre() + "]";
        }
        return linea;
    }

    public void exportarConversacion(String a, String b, Path destino) throws JAXBException, IOException {
        List<String> lineas = consultarConversacion(a, b).stream()
                .map(this::formatear)
                .collect(Collectors.toList());
        Files.write(destino, lineas);
    }

    // Valida, copia el archivo a media/ con InputStream/OutputStream y guarda sus datos en el mensaje
    public void adjuntarArchivo(Mensaje mensaje, Path origen) throws IOException {
        if (!Files.isRegularFile(origen)) {
            throw new IllegalArgumentException("El archivo no existe.");
        }
        String nombre = origen.getFileName().toString();
        String extension = nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase();
        if (!EXTENSIONES.contains(extension)) {
            throw new IllegalArgumentException("Extensión no permitida. Permitidas: " + EXTENSIONES);
        }
        if (Files.size(origen) > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El archivo supera los 5 MB.");
        }

        Files.createDirectories(Path.of("media"));
        Path destino = Path.of("media", System.currentTimeMillis() + "_" + nombre);
        try (InputStream in = new FileInputStream(origen.toFile());
             OutputStream out = new FileOutputStream(destino.toFile())) {
            byte[] buffer = new byte[4096];
            int leidos;
            while ((leidos = in.read(buffer)) != -1) {
                out.write(buffer, 0, leidos);
            }
        }

        mensaje.setAdjuntoRuta(destino.toString());
        mensaje.setAdjuntoNombre(nombre);
        mensaje.setAdjuntoTamano(Files.size(destino));
        mensaje.setAdjuntoTipo(extension);
    }
}