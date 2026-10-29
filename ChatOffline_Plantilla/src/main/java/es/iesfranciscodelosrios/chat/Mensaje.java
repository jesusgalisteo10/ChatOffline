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