package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Clase que gestiona la lógica del chat: usuarios, mensajes y archivos adjuntos.
 * Los datos se guardan en ficheros XML a través de GestorXML.
 */
public class GestorChat {
    // Objeto que lee y escribe los ficheros XML
    private final GestorXML gestorXML = new GestorXML();
    // Tamaño máximo permitido para un adjunto (5 MB en bytes)
    private static final long TAMANO_MAXIMO = 5 * 1024 * 1024; // 5 MB
    // Extensiones de archivo que se pueden adjuntar
    private static final List<String> EXTENSIONES = List.of("jpg", "jpeg", "png", "gif", "pdf", "txt");

    // Comprueba si ya existe un usuario con ese nombre (sin distinguir mayúsculas)
    private boolean existeUsuario(String nombre) throws JAXBException {
        return gestorXML.leerUsuarios().getUsuarios().stream()
                .anyMatch(u -> u.getNombre().equalsIgnoreCase(nombre));
    }

    // Devuelve una lista con los nombres de todos los usuarios registrados
    public List<String> listarUsuarios() throws JAXBException {
        return gestorXML.leerUsuarios().getUsuarios().stream()
                .map(Usuario::getNombre)
                .collect(Collectors.toList());
    }

    // Registra un usuario nuevo, validando que el nombre no esté vacío ni repetido
    public void registrarUsuario(String nombre) throws JAXBException {
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        if (existeUsuario(nombre)) {
            throw new IllegalArgumentException("Ese usuario ya existe.");
        }
        // Leemos la lista actual, añadimos el usuario y volvemos a guardar el XML
        ListaUsuarios lista = gestorXML.leerUsuarios();
        lista.getUsuarios().add(new Usuario(nombre));
        gestorXML.guardarUsuarios(lista);
    }

    // Envía un mensaje de texto normal (sin adjunto)
    public void enviarMensaje(String emisor, String destinatario, String contenido) throws JAXBException {
        guardar(crearMensaje(emisor, destinatario, contenido));
    }

    // Envía un mensaje que lleva un archivo adjunto
    public void enviarConAdjunto(String emisor, String destinatario, String contenido, Path archivo)
            throws JAXBException, IOException {
        Mensaje m = crearMensaje(emisor, destinatario, contenido);
        adjuntarArchivo(m, archivo); // Valida y copia el archivo
        guardar(m);
    }

    // Crea el objeto Mensaje comprobando que emisor y destinatario existen
    private Mensaje crearMensaje(String emisor, String destinatario, String contenido) throws JAXBException {
        if (!existeUsuario(emisor) || !existeUsuario(destinatario)) {
            throw new IllegalArgumentException("Los dos usuarios tienen que estar registrados.");
        }
        // La fecha se guarda como texto, sin nanosegundos
        return new Mensaje(emisor, destinatario, contenido, LocalDateTime.now().withNano(0).toString());
    }

    // Añade el mensaje a la lista del XML y guarda el fichero
    private void guardar(Mensaje m) throws JAXBException {
        ListaMensajes lista = gestorXML.leerMensajes();
        lista.getMensajes().add(m);
        gestorXML.guardarMensajes(lista);
    }

    // Mensajes entre A y B en los dos sentidos
    public List<Mensaje> consultarConversacion(String a, String b) throws JAXBException {
        return gestorXML.leerMensajes().getMensajes().stream()
                // Nos quedamos con los mensajes de A a B o de B a A
                .filter(m -> (m.getEmisor().equalsIgnoreCase(a) && m.getDestinatario().equalsIgnoreCase(b))
                        || (m.getEmisor().equalsIgnoreCase(b) && m.getDestinatario().equalsIgnoreCase(a)))
                .collect(Collectors.toList());
    }

    // Mensajes por usuario (Stream)
    public Map<String, Long> estadisticas(String a, String b) throws JAXBException {
        return consultarConversacion(a, b).stream()
                // Agrupa por emisor y cuenta cuántos mensajes ha enviado cada uno
                .collect(Collectors.groupingBy(Mensaje::getEmisor, Collectors.counting()));
    }

    // Las 3 palabras más repetidas (Stream)
    public List<String> palabrasComunes(String a, String b) throws JAXBException {
        return consultarConversacion(a, b).stream()
                // Separa cada mensaje en palabras (en minúsculas)
                .flatMap(m -> Arrays.stream(m.getContenido().toLowerCase().split("\\W+")))
                // Ignora las palabras cortas (3 letras o menos)
                .filter(p -> p.length() > 3)
                // Cuenta cuántas veces aparece cada palabra
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()))
                .entrySet().stream()
                // Ordena de mayor a menor número de repeticiones
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3) // Solo las 3 primeras
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.toList());
    }

    // Texto de un mensaje, marcando el adjunto si falta
    public String formatear(Mensaje m) {
        String linea = "[" + m.getFecha() + "] " + m.getEmisor() + ": " + m.getContenido();
        if (m.getAdjuntoRuta() != null) {
            // Si el archivo sigue en disco lo indica; si no, avisa de que no se encuentra
            linea += Files.exists(Path.of(m.getAdjuntoRuta()))
                    ? " [adjunto: " + m.getAdjuntoNombre() + "]"
                    : " [ADJUNTO NO ENCONTRADO: " + m.getAdjuntoNombre() + "]";
        }
        return linea;
    }

    // Guarda la conversación entre A y B en un fichero de texto
    public void exportarConversacion(String a, String b, Path destino) throws JAXBException, IOException {
        List<String> lineas = consultarConversacion(a, b).stream()
                .map(this::formatear) // Convierte cada mensaje en una línea de texto
                .collect(Collectors.toList());
        Files.write(destino, lineas); // Escribe todas las líneas en el fichero
    }

    // Valida, copia el archivo a media/ con InputStream/OutputStream y guarda sus datos en el mensaje
    public void adjuntarArchivo(Mensaje mensaje, Path origen) throws IOException {
        // 1. Validaciones: que exista, que la extensión sea válida y que no pese demasiado
        if (!Files.isRegularFile(origen)) {
            throw new IllegalArgumentException("El archivo no existe.");
        }
        String nombre = origen.getFileName().toString();
        // Extensión = lo que hay después del último punto
        String extension = nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase();
        if (!EXTENSIONES.contains(extension)) {
            throw new IllegalArgumentException("Extensión no permitida. Permitidas: " + EXTENSIONES);
        }
        if (Files.size(origen) > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El archivo supera los 5 MB.");
        }

        // 2. Crea la carpeta media/ si no existe y define el destino
        // (se añade la hora al nombre para que no se repita)
        Files.createDirectories(Path.of("media"));
        Path destino = Path.of("media", System.currentTimeMillis() + "_" + nombre);

        // 3. Copia el archivo leyendo y escribiendo en bloques de 4096 bytes
        // (el try-with-resources cierra los streams automáticamente)
        try (InputStream in = new FileInputStream(origen.toFile());
             OutputStream out = new FileOutputStream(destino.toFile())) {
            byte[] buffer = new byte[4096];
            int leidos;
            // read devuelve -1 cuando se llega al final del archivo
            while ((leidos = in.read(buffer)) != -1) {
                out.write(buffer, 0, leidos);
            }
        }

        // 4. Guarda los datos del adjunto en el mensaje
        mensaje.setAdjuntoRuta(destino.toString());
        mensaje.setAdjuntoNombre(nombre);
        mensaje.setAdjuntoTamano(Files.size(destino));
        mensaje.setAdjuntoTipo(extension);
    }
}