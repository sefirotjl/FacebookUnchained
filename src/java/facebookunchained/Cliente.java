/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facebookunchained;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;

import javax.jms.*;
import javax.naming.NamingException;

public class Cliente implements MessageListener, ExceptionListener {

    private User u;
    @Resource(mappedName = "jms/FBFactory")
    private static ConnectionFactory connectionFactory;
    BufferedReader bufferRead;
    String s;
    Connection CConn;
    Session CSession;
    //creamos los productores de mensajes
    MessageProducer messageProducer;
    ObjectMessage objMessage;

    public Cliente(User u) {
        try {
            this.u = u;
            // create a queue connection
            CConn = connectionFactory.createConnection();

            // create a queue session
            CSession = CConn.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public Cliente() {
    }

    public void start() throws Exception {
        // lookup the queue object
        String nombreQ = u.getDiccionario() + "QMessages";
        Queue MHqueue = CSession.createQueue(nombreQ);
        messageProducer = CSession.createProducer(MHqueue);

        //Inicio de session
        System.out.println("Inicio de sesion...");
//        System.out.println("Introduce tu nombre de usuario : ");
//        bufferRead = new BufferedReader(new InputStreamReader(System.in));
//        s = bufferRead.readLine();
//        System.out.println(s);
//        System.out.println("Si tu usuario no existe, se creara uno nuevo...");
//        //verifica usuario
//        //nos unimos al queue del cliente
        String c = "User" + u.getNombre();
        Queue queue = CSession.createQueue(c);
        u.setCola(c);
        objMessage = CSession.createObjectMessage();

        // create a queue receiver
        MessageConsumer messageReceiver = CSession.createConsumer(queue);

        // set an asynchronous message listener
        Cliente asyncReceiver = new Cliente();
        messageReceiver.setMessageListener(asyncReceiver);

        // set an asynchronous exception listener on the connection
        CConn.setExceptionListener(asyncReceiver);

        // start the connection
        CConn.start();

        //Manda inicio de sesion
        Mensaje mensaje = new Mensaje(u, 3);
        objMessage.setObject(mensaje);
        messageProducer.send(objMessage);
        // wait for messages
        boolean escuchar = true;
        while (escuchar) {
            System.out.println("¿Que deseas hacer?");
            System.out.println("'1' si quieres mandar un mensaje normal");
            System.out.println("'2' si quieres agregar un amigo");
            System.out.println("'3' ver mis amigos");
            System.out.println("'4' buscar personas");
            bufferRead = new BufferedReader(new InputStreamReader(System.in));
            s = bufferRead.readLine();
            System.out.println(s);
            if (s.equals("1")) {
                System.out.println("Escribe el mensaje : ");
                bufferRead = new BufferedReader(new InputStreamReader(System.in));
                s = bufferRead.readLine();
                System.out.println(s);
                objMessage = CSession.createObjectMessage();
                //Creamos el mensaje
                //Tipo 1 es un mensaje normal
                mensaje = new Mensaje(u, s, u.getAmigos(), 1);
                objMessage.setObject(mensaje);
                messageProducer.send(objMessage);
                Thread.sleep(1500);
            }
            if (s.equals("3")) {
                if (u.getAmigos() != null && u.tieneAmigos()) {
                    System.out.println("tus amigos son : ");
                    int i = 0;
                    for (User amigo : u.getAmigos()) {
                        i++;
                        System.out.println(i + ".- " + amigo.getNombre());
                    }
                } else {
                    System.out.println("Aun no tienes amigos");
                }
            }
            if (s.equals("2")) {
                System.out.println("Escribe el nombre de tu amigo : ");
                bufferRead = new BufferedReader(new InputStreamReader(System.in));
                s = bufferRead.readLine();
                //Mandar a buscar en el servidor que el usuario exista
                u.addAmigo(s);
                System.out.println("Se agrego el amigo : " + s);
            }
            if (s.equals("4")) {
                System.out.println("Escribe el nombre de la persona que buscas : ");
                bufferRead = new BufferedReader(new InputStreamReader(System.in));
                s = bufferRead.readLine();
                System.out.println("Buscando a "+s+"...");
                objMessage = CSession.createObjectMessage();
                //Creamos el mensaje
                //Tipo 1 es un mensaje normal
                mensaje = new Mensaje(u, s, u.getAmigos(), 4);
                objMessage.setObject(mensaje);
                messageProducer.send(objMessage);
                Thread.sleep(1500);
            }

        }
        System.out.println();

        // close the queue connection
        CConn.close();
    }

    public void enviaMensaje(User u, String mensaje, int tipo) throws NamingException, JMSException {
        ObjectMessage EMobjMessage;
        EMobjMessage = CSession.createObjectMessage();
        Mensaje m = new Mensaje(mensaje);
        m.setTipo(tipo);
        EMobjMessage.setObject(m);
        System.out.println("Sending the following message: " + EMobjMessage.getObject().toString());
        messageProducer.send(EMobjMessage);
    }

    /**
     * This method is called asynchronously by JMS when a message arrives at the
     * queue. Client applications must not throw any exceptions in the onMessage
     * method.
     *
     * @param message A JMS message.
     */
    public void onMessage(Message message) {
        try {
            ObjectMessage objm = (ObjectMessage) message;

            if (objm != null) {
                Mensaje temp = (Mensaje) objm.getObject();
                if (temp.getTipo() == 2) {
                    System.out.println("El resultado de la busqueda de amigos...");
                    LinkedList<User> resp = temp.getAmigos();
                    if (resp == null || resp.isEmpty()) {
                        System.out.println("No se encontraron amigos");
                    } else {
                        int i = 0;
                        for (User amigo : resp) {
                            i++;
                            System.out.println(i + ".- " + amigo.getNombre());
                        }
                    }
                } else {
                    System.out.println(objm.getObject().toString());
                    Mensaje m = (Mensaje) objm.getObject();
                    System.out.println(m.getMensaje());
                }
            } else {
                System.out.println("El mensaje llego null");
            }
        } catch (JMSException ex) {
            ex.printStackTrace();
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

    public static void main(String[] args) {
        try {
            String user = "perla";
            String dir = "USA";
            User u = new User(user, "User" + user, dir);
            Cliente c = new Cliente(u);
            c.start();
        } catch (Exception ex) {
            Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}