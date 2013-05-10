/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facebookunchained;

import java.util.Random;
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

public class ThreadCliente extends Thread implements MessageListener, ExceptionListener {

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

    public ThreadCliente(User u) {
        try {
            this.u = u;
            // create a queue connection
            CConn = connectionFactory.createConnection();

            // create a queue session
            CSession = CConn.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            this.start();
        } catch (JMSException ex) {
            Logger.getLogger(ThreadCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void run() {
        try {
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
            messageReceiver.setMessageListener(this);

            // set an asynchronous exception listener on the connection
            CConn.setExceptionListener(this);

            // start the connection
            CConn.start();

            //Manda inicio de sesion
            Mensaje mensaje = new Mensaje(u, 3);
            objMessage.setObject(mensaje);
            messageProducer.send(objMessage);
            // wait for messages
            boolean escuchar = true;
            Random random = new Random();
            int contador = 0;
            long startTime = System.currentTimeMillis();
            while (escuchar) {

                s = "mensaje de " + u.getNombre();
                objMessage = CSession.createObjectMessage();
                //Creamos el mensaje
                //Tipo 1 es un mensaje normal
                mensaje = new Mensaje(u, s, u.getAmigos(), 1);
                objMessage.setObject(mensaje);
                messageProducer.send(objMessage);
                Thread.sleep(random.nextInt(500) + 100);
                contador++;
                if (u.getAmigos() != null && u.getAmigos().size() <= 20) {
                    if (random.nextDouble() > .5) {
                        String nombre = String.valueOf(random.nextInt(30) + 1);
                        objMessage = CSession.createObjectMessage();
                        //Creamos la solicitud de amigo
                        mensaje = new Mensaje(u, s, u.getAmigos(), 4);
                        objMessage.setObject(mensaje);
                        messageProducer.send(objMessage);
                    }
                }
                if (contador >= 50) {
                    escuchar = false;
                }

                /*
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
                 * */

            }
            long elapsed = System.currentTimeMillis() - startTime;
            Thread.sleep(10000);
            System.out.println("El Thread : " + u.getNombre() + " Tardo : "+ (elapsed/1000) + " segs");
            // close the queue connection
            messageReceiver.close();
            messageProducer.close();
            CSession.close();
            CConn.close();
            this.stop();
            
        } catch (InterruptedException ex) {
            Logger.getLogger(ThreadCliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JMSException ex) {
            Logger.getLogger(ThreadCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                        //Modificado para que agregue cuando lo regresen
                        System.out.println("agregando a : " + resp.getFirst().getNombre());
                        u.addAmigo(resp.getFirst().getNombre(), resp.getFirst().getCola(), resp.getFirst().getDiccionario());
                        /*
                         int i = 0;
                         for (User amigo : resp) {
                         i++;
                         System.out.println(i + ".- " + amigo.getNombre());
                         }
                         * */
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

            for (int i = 1; i <= 50; i++) {
                User u1 = new User(String.valueOf(i), "User" + String.valueOf(i), "AMERICA");
                ThreadCliente c1 = new ThreadCliente(u1);

                User u2 = new User(String.valueOf(i + 50), "User" + String.valueOf(i + 10), "EUROPA");
                ThreadCliente c2 = new ThreadCliente(u2);

                User u3 = new User(String.valueOf(i + 100), "User" + String.valueOf(i + 20), "ASIA");
                ThreadCliente c3 = new ThreadCliente(u3);

            }

        } catch (Exception ex) {
            Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}