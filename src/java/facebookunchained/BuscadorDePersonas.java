/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facebookunchained;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

/**
 *
 * @author Juancho
 */
public class BuscadorDePersonas implements MessageListener, ExceptionListener {

    public void start() throws Exception {
        // get the initial context
        InitialContext ctx = new InitialContext();

        // lookup the queue connection factory
        QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.
                lookup("fuc/connectionFactory");

        // create a queue connection
        QueueConnection BPConn = connFactory.createQueueConnection();

        // create a queue session
        QueueSession BPSession = BPConn.createQueueSession(false,
                Session.AUTO_ACKNOWLEDGE);
        
        // create the queue objec
        Queue queue = BPSession.createQueue("fuc/QBuscadorDePersonas");
        
        // create a queue receiver
        QueueReceiver messageReceiver = BPSession.createReceiver(queue);

        // set an asynchronous message listener
        MessageHandler asyncReceiver = new MessageHandler();
        messageReceiver.setMessageListener(asyncReceiver);

        // set an asynchronous exception listener on the connection
        BPConn.setExceptionListener(asyncReceiver);

        // start the connection
        BPConn.start();

        // wait for messages
        System.out.print("waiting for messages");
        boolean escuchar = true;
        while (escuchar) {
            Thread.sleep(1000);
            System.out.print(".");
        }
        System.out.println();

        // close the queue connection
        BPConn.close();
    }

    /**
     * This method is called asynchronously by JMS when a message arrives at the
     * queue. Client applications must not throw any exceptions in the onMessage
     * method.
     *
     * @param message A JMS message.
     */
    public void onMessage(Message message) {
        TextMessage msg = (TextMessage) message;
        try {
            System.out.println("received: " + msg.getText());
            //ir y buscar a la BD y ver si existe
            //regresar un mensaje de acuerdo al resultado
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
            new MessageHandler().start();
        } catch (Exception ex) {
            Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
