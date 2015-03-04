/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project3;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class Consumer {

    private static String IP, PortNo;

    public Consumer(String IP, String PortNo) {
        this.IP = IP;
        this.PortNo = PortNo;
    }

    public static String StatClient () {

        String text = "No data";
        try {

            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + IP + ":" + PortNo);

            //create a Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();
            //connection.setExceptionListener(this);
            //create a session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            //create destionation (Topic or Queue)
            Destination destination = session.createQueue("G02_SysReUtilization");

            //create consumer from session to topic
            MessageConsumer consumer = session.createConsumer(destination);

            //wait for message
            Message message = consumer.receive(1000);

            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                text = textMessage.getText();
                System.out.println("Received: " + "\n" + text);
                /*
                 * String[] data=text.split("\t");//address
                 * overallUsedCpuPercent coreNo. overallUsedMemPercent totalmem;
                 * String ipAddress=data[0]; if(nodesIP==null){
                 *
                 * }
                 * String overallCPU=data[1].replaceAll("%", ""); String
                 * cpuCoreNo=data[2]; String overallMem=data[3]; String
                 * totalmem=data[4];
                 */

            }
            consumer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return text;
        }

    }
}
