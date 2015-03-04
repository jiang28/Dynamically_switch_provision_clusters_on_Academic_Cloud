/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project3;

import org.hyperic.sigar.*;
import org.hyperic.sigar.ptql.ProcessFinder;
import org.hyperic.sigar.NetInterfaceConfig.*;
import java.text.*;
import java.util.Date;
import java.lang.Runnable;
import java.lang.Thread;
import org.apache.activemq.ActiveMQConnectionFactory;
import java.io.*;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.text.SimpleDateFormat;

public class ProcCpuMem {

    public static void main(String[] args) {

        System.out.println("*********************************************");
        System.out.println("* The Project 3 in B534 class  *");
        System.out.println("* Resource Monitoring System   *");
        System.out.println("*********************************************");
 
        String IP = args[0];//"129.79.49.181";//////
        String PortNo = args[1];//"61617";//////
        String procname = args[2];//"vlc";//;//
        thread(new Producer(IP, PortNo, procname), false);


    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public static class Producer implements Runnable {

        private String IP, PortNo, procname;

        public Producer(String IP, String PortNo, String procname) {
            this.IP = IP;
            this.PortNo = PortNo;
            this.procname = procname;
        }

        public void run() {

            try {
                
                Thread.sleep(1000);//wait MPI start running

                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + IP + ":" + PortNo);

                //create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();

                //create a session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                //create destionation (Topic or Queue)
                Destination destination = session.createQueue("G02_SysReUtilization");

                //create a MessageProducer from session to topic
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                long timeToLive = 10000; // in milliseconds 
                producer.setTimeToLive(timeToLive);
                DecimalFormat df = new DecimalFormat("##.#");

                while (true) {
                    //create a Messages
                    Sigar sigar = new Sigar();
                    long totalmem = sigar.getMem().getTotal();

                    long[] pid = new ProcessFinder(sigar).find("State.Name.eq=" + procname);
                    if (pid.length == 0) {
                        session.close();
                        connection.close();
                        System.exit(0);
                    }

                    double procCPUpercentage = 0.0;
                    Runtime rt = Runtime.getRuntime();
                    BufferedReader in = null;
                    for (int i = 0; i < pid.length; i++) {
                        String[] cmd = {
                            "/bin/sh",
                            "-c",
                            "ps -p " + pid[i] + " -o %cpu | tail -1"
                        };
                        Process p = rt.exec(cmd);
                        in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String result = null;
                        result = in.readLine();

                        if (result != null) {
                            //System.out.println(result);
                            procCPUpercentage += Double.valueOf(result.replaceAll("\\s", ""));
                        }
                    }


                    Double overallUsedCpuPercent = 0.0;
                    int i;
                    CpuPerc[] cpulist = sigar.getCpuPercList();
                    for (i = 0; i < cpulist.length; i++) {
                        //System.out.println("CPU-" + (i + 1) + "-info: " + cpulist[i]);
                        overallUsedCpuPercent += cpulist[i].getCombined();
                    }
                    String overallUsedMemPercent = df.format(sigar.getMem().getUsedPercent());
                    System.out.println("OverallCPUUsage: " + CpuPerc.format(overallUsedCpuPercent / i) + " Avg of " + i + " cores \nOverallMemoryUsage: " + overallUsedMemPercent + "% of " + totalmem / 1024000 + "MB");

                    //overall query processes
                    String cpuPercentage = df.format(procCPUpercentage / i) + "%";
                    //String cpuPercentage = CpuPerc.format(sigar.getMultiProcCpu("State.Name.eq=" + procname).getPercent()/i);
                    double doublememPercentage = sigar.getMultiProcMem("State.Name.eq=" + procname).getResident();
                    String memPercentage = df.format(doublememPercentage * 100 / totalmem);
                    int pronumber = sigar.getMultiProcCpu("State.Name.eq=" + procname).getProcesses();
                    System.out.println("Sum of " + pronumber + " " + procname + " processes: \n" + cpuPercentage + " over " + i + " cores \n" + memPercentage + "% of " + totalmem / 102400 + "MB");

                    //get mac or  ip address
                    String address = sigar.getNetInterfaceConfig().getHwaddr();
                    //String address = java.net.InetAddress.getLocalHost().getHostAddress();

                    //get time
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());
                    String time = formatter.format(curDate);

                    //Send msg  cpu corenumber mem totalmem
                    String result = time + "\t" + CpuPerc.format(overallUsedCpuPercent / i) + "\t" + overallUsedMemPercent + "\t" + cpuPercentage + "\t" + memPercentage + "\t" + address;
                    TextMessage message = session.createTextMessage(result);
                    System.out.println("Send message: " + message.hashCode() + " : " + Thread.currentThread().getName() + " : " + result);

                    producer.send(message);
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

        }
    }
}
