package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.File;

public class GestorXML {
    private final File archivoUsuarios = new File("usuarios.xml");
    private final File archivoMensajes = new File("mensajes.xml");

    public void guardarUsuarios(ListaUsuarios usuarios) throws JAXBException {
        Marshaller m = JAXBContext.newInstance(ListaUsuarios.class).createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(usuarios, archivoUsuarios);
    }

    public ListaUsuarios leerUsuarios() throws JAXBException {
        if (!archivoUsuarios.exists()) {
            return new ListaUsuarios();
        }
        return (ListaUsuarios) JAXBContext.newInstance(ListaUsuarios.class)
                .createUnmarshaller().unmarshal(archivoUsuarios);
    }

    public void guardarMensajes(ListaMensajes mensajes) throws JAXBException {
        Marshaller m = JAXBContext.newInstance(ListaMensajes.class).createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(mensajes, archivoMensajes);
    }

    public ListaMensajes leerMensajes() throws JAXBException {
        if (!archivoMensajes.exists()) {
            return new ListaMensajes();
        }
        return (ListaMensajes) JAXBContext.newInstance(ListaMensajes.class)
                .createUnmarshaller().unmarshal(archivoMensajes);
    }
}
