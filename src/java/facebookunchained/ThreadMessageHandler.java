/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facebookunchained;
//jgroups
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
//
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;

import javax.annotation.Resource;
import javax.jms.*;
import javax.naming.NamingException;

public class ThreadMessageHandler extends ReceiverAdapter implements MessageListener, ExceptionListener {

    @Resource(mappedName = "jms/FBFactory")
    private static ConnectionFactory connectionFactory;
    private JChannel channel;
    private Directorio miDirectorio;
    private LinkedList<Directorio> directorios;
    private Session session;
    private Connection connection;
    private InitialContext ctx;
    private int numNuevos;
    private String nombreDirectorio;

    public ThreadMessageHandler(String nombreDirectorio) {
        try {
            this.nombreDirectorio = nombreDirectorio;
            numNuevos = 0;
            ctx = new InitialContext();
            miDirectorio = new Directorio(nombreDirectorio);
            directorios = new LinkedList<Directorio>();
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException ex) {
            Logger.getLogger(ThreadMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(ThreadMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ThreadMessageHandler(Directorio miDirectorio, String nombreDirectorio) {

        try {
            this.nombreDirectorio = nombreDirectorio;
            numNuevos = 0;
            ctx = new InitialContext();
            this.miDirectorio = miDirectorio;
            directorios = new LinkedList<Directorio>();
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException ex) {
            Logger.getLogger(ThreadMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(ThreadMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ThreadMessageHandler() {
        try {
            this.nombreDirectorio = nombreDirectorio;
            numNuevos = 0;
            ctx = new InitialContext();
            miDirectorio = new Directorio(nombreDirectorio);
            directorios = new LinkedList<Directorio>();
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException ex) {
            Logger.getLogger(ThreadMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(ThreadMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() throws Exception {
        numNuevos = 0;
        ctx = new InitialContext();
        directorios = new LinkedList<Directorio>();
        try {
            String nombreQ = nombreDirectorio + "QMessages";
            Queue queue = session.createQueue(nombreQ);
            MessageConsumer messageConsumer = session.createConsumer(queue);
            // set an asynchronous message listener
            //MessageHandler asyncReceiver = new MessageHandler();
            messageConsumer.setMessageListener(this);
            // set an asynchronous exception listener on the connection
            connection.setExceptionListener(this);

            connection.start();
            //Inicializacion del grupo entre directorios
            joinGroup();
            eventLoop();
            channel.close();
            messageConsumer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called asynchronously by JMS when a message arrives at the
     * queue. Client applications must not throw any exceptions in the onMessage
     * method.
     *
     * @param message A JMS message.
     */
    public void onMessage(javax.jms.Message message) {
        try {
            ObjectMessage objm = (ObjectMessage) message;
            System.out.println(objm.getObject().toString());
            Mensaje m = (Mensaje) objm.getObject();
            System.out.println(m.getTipo());
            System.out.println(m.getUsuario());
            int tipo = m.getTipo();
            switch (tipo) {
                //Enviar mensaje normal
                case 1:
                    System.out.println("Voy a propagar el mensaje...");
                    propagaMensaje(m);
                    break;
                //Reghistra Usuario
                case 2:
                    registraUsuario(m.getUsuario());
                    enviaMensaje(m.getUsuario(), "El usuario se registro con exito", 1);
                    break;
                //Inicia sesion
                case 3:
                    iniciaSesion(m.getUsuario());
                    enviaMensaje(m.getUsuario(), "Bienvenido : " + m.getUsuario().getNombre(), 1);
                    break;
                //Opten lista de amigos
                case 4:
                    System.out.println("Se solicito una busqueda de :" + m.getMensaje());
                    LinkedList<User> resultado = new LinkedList<User>();
                    resultado = buscaUsuarios(m.getMensaje());
                    Mensaje resp = new Mensaje();
                    resp.setAmigos(resultado);
                    resp.setTipo(2);
                    resp.setMensaje("Respuesta de busqueda");
                    enviaMensaje(m.getUsuario(), resp, 2);
                case 5:
                case 6:
            }
        } catch (NamingException ex) {
            Logger.getLogger(ThreadMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JMSException ex) {
            Logger.getLogger(ThreadMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void joinGroup() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect("FUCCluster");
        //hacemos que mis mensajes no me lleguen a mi
        channel.setDiscardOwnMessages(true);

    }

    private void eventLoop() {

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > 10000 /*|| numNuevos >= 5*/){
                    //System.out.println("Se cumplio una condicion");
                    //System.out.println("de propagacion de directorios");
                    channel.send(null, miDirectorio);
                    numNuevos = 0;
                    startTime = System.currentTimeMillis();
                }
            } catch (Exception e) {
            }

        }

    }

    @Override
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    @Override
    public void receive(org.jgroups.Message msg) {
        //
        System.out.println(msg.getSrc() + ": Recivido");
        System.out.println("");
        Directorio temp = (Directorio) msg.getObject();
        if (directorios != null && !directorios.isEmpty()) {
            System.out.println("Recivi el directorio de :" + temp.getNombre());
            System.out.println("Voy a comparar los directorios");
            for (Directorio d : directorios) {
                System.out.println("Voy a comparar contra :" + d.getNombre());
                if (d.getNombre().equals(temp.getNombre())) {
                    System.out.println("Encontre el directorio a actualizar");
                    if (!d.equals(temp)) {
                        directorios.remove(d);
                        directorios.add(temp);
                        System.out.println("Se actualizo el directorio de " + d.getNombre());
                        return;
                    }
                    System.out.println("El directorio de " + d.getNombre() + " ya esta actualizado");
                    return;
                }
            }
            System.out.println("No encontre el directorio");
            System.out.println("Supongo que es nuevo, lo voy a agregar");
            directorios.add(temp);
        }
        else{
             directorios = new LinkedList<Directorio>();
             if(temp == null){
                 System.out.println("Se recivio un directorio nulo");
                 return;
             }
             directorios.add(temp);
             System.out.println("Se creo la lista de directorios");
        }

    }

    /**
     * This method is called asynchronously by JMS when some error occurs. When
     * using an asynchronous message listener it is recommended to use an
     * exception listener also since JMS have no way to report errors otherwise.
     *
     * @param exception A JMS exception.
     */
    public void onException(JMSException exception) {
        System.err.println("an error occurred: " + exception);
    }

    private void propagaMensaje(Mensaje m) throws NamingException, JMSException {
        MessageProducer PMmessageProducer;
        ObjectMessage PMobjMessage;
        Queue PMqueue = session.createQueue(m.getUsuario().getCola());
        PMmessageProducer = session.createProducer(PMqueue);
        PMobjMessage = session.createObjectMessage();
        String mensaje = "Usuario : " + m.getUsuario().getNombre() + " Mensaje : " + m.getMensaje();
        m.setMensaje(mensaje);
        PMobjMessage.setObject(m);
        System.out.println("Sending the following message: " + mensaje);
        PMmessageProducer.send(PMobjMessage);
        System.out.println("voy a checar si tiene amigos...");
        if (m.getUsuario().getAmigos() == null || m.getUsuario().getAmigos().isEmpty()) {
            System.out.println("No tiene amigos");
        } else {
            for (User amigo : m.getUsuario().getAmigos()) {
                User temp = new User();
                temp = buscaUsuario(amigo.getNombre());
                if (temp == null) {
                    System.out.println("El usuario no existe en ningun directorio");
                    System.out.println("Lo registraremos en este directorio");
                    temp = new User(amigo.getNombre(), "User" + amigo.getNombre(), nombreDirectorio);
                    registraUsuario(temp);
                    PMqueue = session.createQueue(temp.getCola());
                    PMmessageProducer = session.createProducer(PMqueue);
                    PMmessageProducer.send(PMobjMessage);
                    System.out.println("Mensaje enviado");
                } else {
                    if (temp.getDiccionario() == nombreDirectorio) {
                        System.out.println(temp.getNombre() + " pertenece a mi"
                                + " directorio");
                        System.out.println("Mandando mensaje a : " + temp.getNombre());
                        PMqueue = session.createQueue(temp.getCola());
                        PMmessageProducer = session.createProducer(PMqueue);
                        PMmessageProducer.send(PMobjMessage);
                        System.out.println("Mensaje enviado");
                    } else {
                        System.out.println("Pertenece al directorio de " + temp.getDiccionario());
                        System.out.println("Mandando mensaje a : " + temp.getNombre());
                        PMqueue = session.createQueue(temp.getCola());
                        PMmessageProducer = session.createProducer(PMqueue);
                        PMmessageProducer.send(PMobjMessage);
                        System.out.println("Mensaje enviado");
                    }
                }
            }
        } 
        PMmessageProducer .close();
    }

    public void enviaMensaje(User u, String mensaje, int tipo) throws NamingException, JMSException {
        MessageProducer EMmessageProducer;
        ObjectMessage EMobjMessage;
        String s = u.getCola();
        System.out.println("La cola es: " + s);
        Queue EMqueue = session.createQueue(s);
        EMmessageProducer = session.createProducer(EMqueue);
        EMobjMessage = session.createObjectMessage();
        Mensaje m = new Mensaje(mensaje);
        m.setTipo(tipo);
        EMobjMessage.setObject(m);
        System.out.println("Sending the following message: " + EMobjMessage.getObject().toString());
        EMmessageProducer.send(EMobjMessage);
        EMmessageProducer.close();
    }

    public void enviaMensaje(User u, Mensaje m, int tipo) throws NamingException, JMSException {
        MessageProducer EMmessageProducer;
        ObjectMessage EMobjMessage;
        String s = u.getCola();
        System.out.println("La cola es: " + s);
        Queue EMqueue = session.createQueue(s);
        EMmessageProducer = session.createProducer(EMqueue);
        EMobjMessage = session.createObjectMessage();
        m.setTipo(tipo);
        EMobjMessage.setObject(m);
        System.out.println("Sending the following message: " + EMobjMessage.getObject().toString());
        EMmessageProducer.send(EMobjMessage);
        EMmessageProducer.close();
        
    }

    public synchronized boolean registraUsuario(User yo) throws JMSException {
        System.out.println("agregando al usuario: " + yo.getNombre());
        if (miDirectorio != null) {
            miDirectorio.agregaUsuario(yo);
        } else {
            miDirectorio = new Directorio(nombreDirectorio);
            miDirectorio.agregaUsuario(yo);
        }
        System.out.println("El usuario:" + yo.getNombre() + " se agrego al direcotrio");
        numNuevos++;
        return true;
    }

    public User iniciaSesion(User yo) throws JMSException {
        if (miDirectorio != null && !miDirectorio.isEmpty()) {
            for (User u : miDirectorio.getUsuarios()) {
                if (u.getNombre().equals(yo.getNombre())) {
                    System.out.println("El usuario:" + yo.getNombre() + " ha iniciado sesion");
                    return u;
                }
            }
        }
        registraUsuario(yo);
        return yo;
    }

    private User buscaUsuario(String nombre) {
        if (miDirectorio == null) {
            miDirectorio = new Directorio();
            return buscaUsuarioEnDirectorios(nombre);
        } else {
            if (!miDirectorio.getUsuarios().isEmpty()) {
                for (User u : miDirectorio.getUsuarios()) {
                    if (u.getNombre().equals(nombre)) {
                        return u;
                    }
                }
                return buscaUsuarioEnDirectorios(nombre);
            } else {
                return buscaUsuarioEnDirectorios(nombre);
            }
        }
    }

    private User buscaUsuarioEnDirectorios(String nombre) {
        if (directorios == null) {
            directorios = new LinkedList<Directorio>();
            return null;
        } else {
            if (directorios.isEmpty()) {
                return null;
            } else {
                for (Directorio d : directorios) {
                    if (!d.getUsuarios().isEmpty()) {
                        for (User u : d.getUsuarios()) {
                            if (u.getNombre().equals(nombre)) {
                                return u;
                            }
                        }
                    }
                }
                return null;
            }
        }
    }

    private LinkedList<User> buscaUsuarios(String nombre) {
        LinkedList<User> resultado = new LinkedList<User>();
        nombre = nombre.toLowerCase();
        if (miDirectorio == null) {
            miDirectorio = new Directorio();
            return buscaUsuariosEnDirectorios(nombre);
        } else {
            if (!miDirectorio.getUsuarios().isEmpty()) {
                if (nombre.equals("todos")) {
                    System.out.println("Se solicito regresar todo el directorio...");
                    for (User u : miDirectorio.getUsuarios()) {
                        System.out.println("Agregando " + u.getNombre() +" a la respuesta");
                        resultado.add(u);
                    }
                    return resultado;
                } else {
                    for (User u : miDirectorio.getUsuarios()) {
                        System.out.println("Se solicito encontrar un usuario especifico");
                        if (u.getNombre().toLowerCase().contains(nombre)) {
                            System.out.println("Agregando " + u.getNombre() +" a la respuesta");
                            resultado.add(u);
                        }
                    }

                    LinkedList<User> temp = new LinkedList<User>();
                    temp = buscaUsuariosEnDirectorios(nombre);

                    if (temp == null || temp.isEmpty()) {
                        return resultado;
                    } else {
                        resultado.addAll(temp);
                        return resultado;
                    }
                }
            } else {
                return buscaUsuariosEnDirectorios(nombre);
            }
        }
    }

    private LinkedList<User> buscaUsuariosEnDirectorios(String nombre) {
        LinkedList<User> resultado = new LinkedList<User>();
        if (directorios == null) {
            directorios = new LinkedList<Directorio>();
            return null;
        } else {
            if (directorios.isEmpty()) {
                return null;
            } else {
                for (Directorio d : directorios) {
                    if (d.getUsuarios() != null && !d.getUsuarios().isEmpty()) {
                        for (User u : d.getUsuarios()) {
                            if (u.getNombre().toLowerCase().contains(nombre)) {
                                resultado.add(u);
                            }
                        }
                    }
                }
                return resultado;
            }
        }
    }

    public static void main(String[] args) {
        try {
            ThreadMessageHandler mh1 = new ThreadMessageHandler("AMERICA");
            //ThreadMessageHandler mh2 = new ThreadMessageHandler("EUROPA");
            //ThreadMessageHandler mh3 = new ThreadMessageHandler("ASIA");
            mh1.start();
            //mh2.start();
            //mh3.start();
            
        } catch (Exception ex) {
            Logger.getLogger(ThreadMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
