package es.iesfranciscodelosrios.chat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        GestorChat chat = new GestorChat();
        int opcion = -1;

        while (opcion != 0) {
            System.out.println("\n--- Chat Offline ---");
            System.out.println("1. Registrar usuario");
            System.out.println("2. Enviar mensaje");
            System.out.println("3. Consultar conversación");
            System.out.println("4. Estadísticas");
            System.out.println("5. Exportar conversación");
            System.out.println("6. Enviar mensaje con archivo adjunto");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            try {
                opcion = Integer.parseInt(teclado.nextLine().trim());
                switch (opcion) {
                    case 1 -> {
                        System.out.print("Nombre: ");
                        chat.registrarUsuario(teclado.nextLine().trim());
                        System.out.println("Usuario registrado.");
                    }
                    case 2 -> {
                        String[] par = elegirPar(teclado, chat, "Emisor", "Destinatario");
                        if (par != null) {
                            chat.enviarMensaje(par[0], par[1], pedir(teclado, "Mensaje: "));
                            System.out.println("Mensaje enviado.");
                        }
                    }
                    case 3 -> {
                        String[] par = elegirPar(teclado, chat, "Tu usuario", "Conversación con");
                        if (par != null) {
                            chat.consultarConversacion(par[0], par[1])
                                    .forEach(m -> System.out.println(chat.formatear(m)));
                        }
                    }
                    case 4 -> {
                        String[] par = elegirPar(teclado, chat, "Usuario A", "Usuario B");
                        if (par != null) {
                            Map<String, Long> stats = chat.estadisticas(par[0], par[1]);
                            System.out.println("Total de mensajes: " + stats.values().stream().mapToLong(Long::longValue).sum());
                            stats.forEach((u, n) -> System.out.println("  " + u + ": " + n));
                            System.out.println("Palabras más usadas: " + chat.palabrasComunes(par[0], par[1]));
                        }
                    }
                    case 5 -> {
                        String[] par = elegirPar(teclado, chat, "Usuario A", "Usuario B");
                        if (par != null) {
                            String ruta = pedir(teclado, "Archivo de destino (ej. conversacion.txt): ");
                            chat.exportarConversacion(par[0], par[1], Path.of(ruta));
                            System.out.println("Exportado.");
                        }
                    }
                    case 6 -> {
                        String[] par = elegirPar(teclado, chat, "Emisor", "Destinatario");
                        if (par != null) {
                            String texto = pedir(teclado, "Mensaje: ");
                            String ruta = pedir(teclado, "Ruta del archivo: ");
                            chat.enviarConAdjunto(par[0], par[1], texto, Path.of(ruta));
                            System.out.println("Mensaje con adjunto enviado.");
                        }
                    }
                    case 0 -> System.out.println("Adiós.");
                    default -> System.out.println("Opción no válida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número.");
                opcion = -1;
            } catch (java.util.NoSuchElementException e) {
                break;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        teclado.close();
    }
    private static String pedir(Scanner teclado, String texto) {
        System.out.print(texto);
        return teclado.nextLine().trim();
    }

    // Lista los usuarios numerados y devuelve el elegido (o null si no es válido)
    private static String elegirUsuario(Scanner teclado, List<String> usuarios, String texto) {
        System.out.println(texto + ":");
        for (int i = 0; i < usuarios.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + usuarios.get(i));
        }
        System.out.print("Número: ");
        try {
            int n = Integer.parseInt(teclado.nextLine().trim());
            if (n >= 1 && n <= usuarios.size()) {
                return usuarios.get(n - 1);
            }
        } catch (NumberFormatException e) {
            // se trata como número no válido
        }
        System.out.println("Número no válido.");
        return null;
    }

    // Elige dos usuarios distintos. Devuelve null si no se puede o si la elección no es válida.
    private static String[] elegirPar(Scanner teclado, GestorChat chat, String textoA, String textoB)
            throws Exception {
        List<String> usuarios = chat.listarUsuarios();
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios creados: no se pueden leer conversaciones ni enviar mensajes.");
            return null;
        }
        if (usuarios.size() < 2) {
            System.out.println("Solo hay 1 usuario. Registra al menos otro para poder chatear.");
            return null;
        }
        String a = elegirUsuario(teclado, usuarios, textoA);
        if (a == null) {
            return null;
        }
        List<String> otros = new ArrayList<>(usuarios);
        otros.remove(a);
        String b = elegirUsuario(teclado, otros, textoB);
        return b == null ? null : new String[] {a, b};
    }
}