# Chat Offline

Chat por **terminal** hecho en Java que funciona **sin internet y sin servidor**. Los usuarios y los mensajes se guardan en archivos **XML** (con JAXB) y los archivos adjuntos en la carpeta `media/`.

> Proyecto de la Unidad 1 de **Acceso a Datos** · 2.º DAM · IES Francisco de los Ríos
> Autores: Rubén y Jesús

## Funcionalidades

- **Registrar usuarios**: no se permiten nombres vacíos ni repetidos.
- **Enviar mensajes** entre usuarios registrados.
- **Consultar conversaciones**: todos los mensajes entre dos usuarios, en los dos sentidos.
- **Estadísticas** con la Stream API: total de mensajes, mensajes por usuario y las 3 palabras más usadas.
- **Exportar** la conversación a un archivo de texto (`.txt`).
- **Archivos adjuntos**: se copian a `media/` con `InputStream`/`OutputStream` y el mensaje guarda su referencia.
- Los usuarios se **eligen por número** desde una lista, sin escribir el nombre a mano.
- Si no hay usuarios creados, el programa avisa de que no se pueden leer conversaciones ni enviar mensajes.
- Los mensajes **no se pueden borrar**.

## Requisitos

- JDK 17 o superior
- Maven

## Ejecutar

Desde la carpeta del proyecto:

```bash
mvn compile exec:java
```

Al usarlo se crean en esa misma carpeta los archivos `usuarios.xml` y `mensajes.xml` y la carpeta `media/` (si hay adjuntos).

## Uso

```
--- Chat Offline ---
1. Registrar usuario
2. Enviar mensaje
3. Consultar conversación
4. Estadísticas
5. Exportar conversación
6. Enviar mensaje con archivo adjunto
0. Salir
```

1. Registra al menos **dos usuarios** (opción 1).
2. Envía mensajes (opción 2): eliges emisor y destinatario de la lista escribiendo su número.
3. Consulta la conversación (opción 3) o mira las estadísticas (opción 4).
4. Exporta la conversación (opción 5) escribiendo el nombre del archivo, por ejemplo `conversacion.txt`. Si solo pones un nombre, se guarda en la carpeta desde la que ejecutas el programa.
5. Para mandar un archivo (opción 6), escribe su ruta completa.

## Archivos adjuntos

- Extensiones permitidas: `jpg`, `jpeg`, `png`, `gif`, `pdf` y `txt`.
- Tamaño máximo: **5 MB**.
- Se guardan en `media/` con un prefijo único (la hora en milisegundos) para que no se sobrescriban.
- En el XML solo se guarda la **referencia**: ruta, nombre, tamaño y tipo.
- Si un adjunto desaparece de `media/`, el mensaje **no se borra**: se sigue viendo en la conversación marcado como `[ADJUNTO NO ENCONTRADO]`.

## Estructura del proyecto

```
src/main/java/es/iesfranciscodelosrios/chat/
├── Main.java            Menú por terminal
├── GestorChat.java      Lógica: usuarios, mensajes, Streams, exportar y adjuntos
├── GestorXML.java       Lectura y escritura de los XML con JAXB
├── Usuario.java         Modelo de usuario
├── ListaUsuarios.java   Raíz de usuarios.xml
├── Mensaje.java         Modelo de mensaje
└── ListaMensajes.java   Raíz de mensajes.xml
media/                   Archivos adjuntos
```

## Formato de los datos

**usuarios.xml**

```xml
<usuarios>
  <usuario><nombre>ana</nombre></usuario>
  <usuario><nombre>bob</nombre></usuario>
</usuarios>
```

**mensajes.xml**

```xml
<mensajes>
  <mensaje>
    <emisor>ana</emisor>
    <destinatario>bob</destinatario>
    <contenido>mira esto</contenido>
    <fecha>2026-10-30T10:53:00</fecha>
    <adjuntoRuta>media/1761821580000_foto.png</adjuntoRuta>
    <adjuntoNombre>foto.png</adjuntoNombre>
    <adjuntoTamano>3000</adjuntoTamano>
    <adjuntoTipo>png</adjuntoTipo>
  </mensaje>
</mensajes>
```

La fecha se guarda en formato ISO-8601. Si el mensaje no tiene adjunto, los campos `adjuntoRuta`, `adjuntoNombre` y `adjuntoTipo` no aparecen.

## Tecnologías

- Java 17
- JAXB (Jakarta XML Binding)
- Stream API (`java.util.stream`)
- `InputStream` / `OutputStream`
- Maven

## Autores

- Rubén
- Jesús
