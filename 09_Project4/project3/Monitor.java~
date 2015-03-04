/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project3;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.BorderFactory;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class Monitor extends ApplicationFrame {

    public static final int SUBPLOT_COUNT = 4;
    private DynamicTimeSeriesCollection[] datasets;//0-cpu 1--mem 2--proccpu 3--procmem
    private Timer timer;
    private static final String TITLE = "CPU and Memory Usage";
    private static final float MINMAX = 100;//y
    private static final int COUNT = 30;//x
    private static final int SLOW = 1000;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ArrayList<String> timelist0 = new ArrayList<String>();
    ArrayList<String> timelist1 = new ArrayList<String>();
    ArrayList<String> timelist2 = new ArrayList<String>();

    public Monitor(final String title, final String IP, final String PortNo, final String syncMethod) {
        super(title);
        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new DateAxis("Time"));
        this.datasets = new DynamicTimeSeriesCollection[SUBPLOT_COUNT];
        String name;
        for (int i = 0; i < SUBPLOT_COUNT; i++) {


            this.datasets[i] = new DynamicTimeSeriesCollection(1, COUNT, new Second());
            if (i == 0) {
                name = "Overall CPU";
            } else if (i == 1) {
                name = "Overall Mem";
            } else if (i == 2) {
                name = "Process CPU";
            } else {
                name = "Process Mem";
            }
            datasets[i].setTimeBase(new Second());
            datasets[i].addSeries(initData(), 0, name);
            final NumberAxis rangeAxis = new NumberAxis(name);
            rangeAxis.setRange(0, MINMAX);
            final XYPlot subplot = new XYPlot(
                    this.datasets[i], null, rangeAxis, new StandardXYItemRenderer());
            plot.add(subplot);
        }
        final JFreeChart chart = new JFreeChart(TITLE, plot);
        final ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(30000.0);  // 30 seconds

        final JPanel content = new JPanel(new BorderLayout());

        final ChartPanel chartPanel = new ChartPanel(chart);
        content.add(chartPanel);

        chartPanel.setPreferredSize(new java.awt.Dimension(500, 470));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(content);

        //update

        timer = new Timer(SLOW, new ActionListener() {//Creates a Timer that will notify its listeners every delay milliseconds.

            //float[] newData = new float[1];
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (syncMethod.equals("1")) {
                        String received = new Consumer(IP, PortNo).StatClient();

                        if (!received.equals("No data") && received != null) {

                            String data = checkData(received);
                            if (data.equals("NoOutput")) {
                                System.out.println("Self Synchronization");
                            } else {
                                float[] floatcpu = new float[1];
                                float[] floatmem = new float[1];
                                float[] floatcpuP = new float[1];
                                float[] floatmemP = new float[1];

                                floatcpu[0] = (Float.valueOf(data.split("\t")[0].trim()).floatValue());
                                floatmem[0] = (Float.valueOf(data.split("\t")[1].trim()).floatValue());
                                floatcpuP[0] = (Float.valueOf(data.split("\t")[2].trim()).floatValue());
                                floatmemP[0] = (Float.valueOf(data.split("\t")[3].trim()).floatValue());

                                datasets[0].advanceTime();//Adjust the array offset as needed when a new time-period is added:
                                datasets[0].appendData(floatcpu);
                                datasets[1].advanceTime();//Adjust the array offset as needed when a new time-period is added:
                                datasets[1].appendData(floatmem);
                                datasets[2].advanceTime();//Adjust the array offset as needed when a new time-period is added:
                                datasets[2].appendData(floatcpuP);
                                datasets[3].advanceTime();//Adjust the array offset as needed when a new time-period is added:
                                datasets[3].appendData(floatmemP);
                            }
                        }
                    } else if (syncMethod.equals("2")) {
                        HashMap<String, ArrayList<String>> syncUID = new HashMap<String, ArrayList<String>>();

                        long t = System.currentTimeMillis();
                        long end = t + 1000;
                        while (System.currentTimeMillis() < end) {
                            String received = new Consumer(IP, PortNo).StatClient();
                            if (!received.equals("No data") && received != null) {
                                String macaddress = received.split("\t")[5];
                                String overallcpu = received.split("\t")[1].replaceAll("%", "");
                                String overallmem = received.split("\t")[2];
                                String procpu = received.split("\t")[3].replaceAll("%", "");
                                String promem = received.split("\t")[4];

                                if (syncUID.containsKey(macaddress)) {
                                    ArrayList<String> sameID = syncUID.get(macaddress);
                                    sameID.add(overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                                    syncUID.put(macaddress, sameID);

                                } else {
                                    ArrayList<String> sameID = new ArrayList<String>();
                                    sameID.add(overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                                    syncUID.put(macaddress, sameID);
                                }

                            }
                        }

                        if (syncUID.isEmpty()) {
                            System.err.println("No data received in this period");
                        } else {
                            Iterator<String> iterator = syncUID.keySet().iterator();
                            ArrayList<String> nodes = new ArrayList<String>();
                            while (iterator.hasNext()) {
                                String UID = (String) iterator.next();
                                ArrayList<String> sameID = syncUID.get(UID);
                                Double outputCPU = 0.0;
                                Double outputMEM = 0.0;
                                Double outputCPUP = 0.0;
                                Double outputMEMP = 0.0;
                                for (int i = 0; i < sameID.size(); i++) {
                                    outputCPU += Double.valueOf(sameID.get(i).split("\t")[0]);
                                    outputMEM += Double.valueOf(sameID.get(i).split("\t")[1]);
                                    outputCPUP += Double.valueOf(sameID.get(i).split("\t")[2]);
                                    outputMEMP += Double.valueOf(sameID.get(i).split("\t")[3]);
                                }
                                nodes.add(String.valueOf(outputCPU / sameID.size()) + "\t" + String.valueOf(outputMEM / sameID.size()) + "\t"
                                        + String.valueOf(outputCPUP / sameID.size()) + "\t" + String.valueOf(outputMEMP / sameID.size()));
                            }
                            Double finaloutputCPU = 0.0;
                            Double finaloutputMEM = 0.0;
                            Double finaloutputCPUP = 0.0;
                            Double finaloutputMEMP = 0.0;
                            for (int j = 0; j < nodes.size(); j++) {
                                finaloutputCPU += Double.valueOf(nodes.get(j).split("\t")[0]);
                                finaloutputMEM += Double.valueOf(nodes.get(j).split("\t")[1]);
                                finaloutputCPUP += Double.valueOf(nodes.get(j).split("\t")[2]);
                                finaloutputMEMP += Double.valueOf(nodes.get(j).split("\t")[3]);
                            }
                            float[] floatcpu = new float[1];
                            float[] floatmem = new float[1];
                            float[] floatcpuP = new float[1];
                            float[] floatmemP = new float[1];

                            floatcpu[0] = (Float.valueOf(String.valueOf(finaloutputCPU / nodes.size())).floatValue());
                            floatmem[0] = (Float.valueOf(String.valueOf(finaloutputMEM / nodes.size())).floatValue());
                            floatcpuP[0] = (Float.valueOf(String.valueOf(finaloutputCPUP / nodes.size())).floatValue());
                            floatmemP[0] = (Float.valueOf(String.valueOf(finaloutputMEMP / nodes.size())).floatValue());

                            datasets[0].advanceTime();//Adjust the array offset as needed when a new time-period is added:
                            datasets[0].appendData(floatcpu);
                            datasets[1].advanceTime();//Adjust the array offset as needed when a new time-period is added:
                            datasets[1].appendData(floatmem);
                            datasets[2].advanceTime();//Adjust the array offset as needed when a new time-period is added:
                            datasets[2].appendData(floatcpuP);
                            datasets[3].advanceTime();//Adjust the array offset as needed when a new time-period is added:
                            datasets[3].appendData(floatmemP);
                        }

                    }
                } catch (Exception ew) {
                    ew.printStackTrace();
                }

            }
        });

    }

    public String checkData(String data) {
        String result = "NoOutput";
        try {

            String time = data.split("\t")[0];
            java.util.Date parsedDate = dateFormat.parse(time);
            java.sql.Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
            String overallcpu = data.split("\t")[1].replaceAll("%", "");
            String overallmem = data.split("\t")[2];
            String procpu = data.split("\t")[3].replaceAll("%", "");
            String promem = data.split("\t")[4];

            if (timelist0.isEmpty() && timelist1.isEmpty() && timelist2.isEmpty()) {
                //System.out.println("test");
                timelist0.add(time + "\t" + overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                //System.out.println(timelist0);
            }

            if (!timelist0.isEmpty() && timelist1.isEmpty() && timelist2.isEmpty()) {
                String flag = "lessthan2second";
                for (int i = 0; i < timelist0.size(); i++) {
                    String time0 = timelist0.get(i).split("\t")[0];
                    java.util.Date parsedDate0 = dateFormat.parse(time0);
                    java.sql.Timestamp timestamp0 = new java.sql.Timestamp(parsedDate0.getTime());
                    if (timestamp.getTime() != timestamp0.getTime()) {
                        flag = "morethan2second";
                    }
                }
                if (flag.equals("lessthan2second")) {
                    timelist0.add(time + "\t" + overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                } else if (flag.equals("morethan2second")) {
                    timelist1.add(time + "\t" + overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                }
            }

            if (!timelist0.isEmpty() && !timelist1.isEmpty() && timelist2.isEmpty()) {
                String flag = "lessthan2second";
                String flag2 = "lessthan2second";
                for (int i = 0; i < timelist0.size(); i++) {
                    String time0 = timelist0.get(i).split("\t")[0];
                    java.util.Date parsedDate0 = dateFormat.parse(time0);
                    java.sql.Timestamp timestamp0 = new java.sql.Timestamp(parsedDate0.getTime());
                    if (timestamp.getTime() != timestamp0.getTime()) {
                        flag = "morethan2second";
                    }
                }
                if (flag.equals("lessthan2second")) {
                    timelist0.add(time + "\t" + overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                } else if (flag.equals("morethan2second")) {

                    for (int i = 0; i < timelist1.size(); i++) {
                        String time1 = timelist1.get(i).split("\t")[0];
                        java.util.Date parsedDate1 = dateFormat.parse(time1);
                        java.sql.Timestamp timestamp1 = new java.sql.Timestamp(parsedDate1.getTime());
                        if (timestamp.getTime() != timestamp1.getTime()) {
                            flag2 = "morethan2second";
                        }
                    }
                    if (flag2.equals("lessthan2second")) {
                        timelist1.add(time + "\t" + overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                    } else if (flag2.equals("morethan2second")) {
                        timelist2.add(time + "\t" + overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                        Double outputCPU = 0.0;
                        Double outputMEM = 0.0;
                        Double outputCPUP = 0.0;
                        Double outputMEMP = 0.0;
                        for (int i = 0; i < timelist0.size(); i++) {
                            outputCPU += Double.valueOf(timelist0.get(i).split("\t")[1]);
                            outputMEM += Double.valueOf(timelist0.get(i).split("\t")[2]);
                            outputCPUP += Double.valueOf(timelist0.get(i).split("\t")[3]);
                            outputMEMP += Double.valueOf(timelist0.get(i).split("\t")[4]);
                        }
                        result = Double.toString(outputCPU / timelist0.size()) + "\t" + Double.toString(outputMEM / timelist0.size()) + "\t" + Double.toString(outputCPUP / timelist0.size()) + "\t" + Double.toString(outputMEMP / timelist0.size());
                        timelist0.clear();

                    }
                }

            } else if (timelist0.isEmpty() && !timelist1.isEmpty() && !timelist2.isEmpty()) {
                String flag = "lessthan2second";
                String flag2 = "lessthan2second";
                for (int i = 0; i < timelist1.size(); i++) {
                    String time1 = timelist1.get(i).split("\t")[0];
                    java.util.Date parsedDate1 = dateFormat.parse(time1);
                    java.sql.Timestamp timestamp1 = new java.sql.Timestamp(parsedDate1.getTime());
                    if (timestamp.getTime() != timestamp1.getTime()) {
                        flag = "morethan2second";
                    }
                }
                if (flag.equals("lessthan2second")) {
                    timelist1.add(time + "\t" + overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                } else if (flag.equals("morethan2second")) {

                    for (int i = 0; i < timelist2.size(); i++) {
                        String time2 = timelist2.get(i).split("\t")[0];
                        java.util.Date parsedDate2 = dateFormat.parse(time2);
                        java.sql.Timestamp timestamp2 = new java.sql.Timestamp(parsedDate2.getTime());
                        if (timestamp.getTime() != timestamp2.getTime()) {
                            flag2 = "morethan2second";
                        }
                    }
                    if (flag2.equals("lessthan2second")) {
                        timelist2.add(time + "\t" + overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                    } else if (flag2.equals("morethan2second")) {
                        timelist0.add(time + "\t" + overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                        Double outputCPU = 0.0;
                        Double outputMEM = 0.0;
                        Double outputCPUP = 0.0;
                        Double outputMEMP = 0.0;
                        for (int i = 0; i < timelist1.size(); i++) {
                            outputCPU += Double.valueOf(timelist1.get(i).split("\t")[1]);
                            outputMEM += Double.valueOf(timelist1.get(i).split("\t")[2]);
                            outputCPUP += Double.valueOf(timelist1.get(i).split("\t")[3]);
                            outputMEMP += Double.valueOf(timelist1.get(i).split("\t")[4]);
                        }
                        result = Double.toString(outputCPU / timelist1.size()) + "\t" + Double.toString(outputMEM / timelist1.size()) + "\t" + Double.toString(outputCPUP / timelist1.size()) + "\t" + Double.toString(outputMEMP / timelist1.size());
                        timelist1.clear();

                    }
                }

            } else if (!timelist0.isEmpty() && timelist1.isEmpty() && !timelist2.isEmpty()) {
                String flag = "lessthan2second";
                String flag2 = "lessthan2second";
                for (int i = 0; i < timelist2.size(); i++) {
                    String time2 = timelist2.get(i).split("\t")[0];
                    java.util.Date parsedDate2 = dateFormat.parse(time2);
                    java.sql.Timestamp timestamp2 = new java.sql.Timestamp(parsedDate2.getTime());
                    if (timestamp.getTime() != timestamp2.getTime()) {
                        flag = "morethan2second";
                    }
                }
                if (flag.equals("lessthan2second")) {
                    timelist2.add(time + "\t" + overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                } else if (flag.equals("morethan2second")) {

                    for (int i = 0; i < timelist0.size(); i++) {
                        String time0 = timelist0.get(i).split("\t")[0];
                        java.util.Date parsedDate0 = dateFormat.parse(time0);
                        java.sql.Timestamp timestamp0 = new java.sql.Timestamp(parsedDate0.getTime());
                        if (timestamp.getTime() != timestamp0.getTime()) {
                            flag2 = "morethan2second";
                        }
                    }
                    if (flag2.equals("lessthan2second")) {
                        timelist0.add(time + "\t" + overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                    } else if (flag2.equals("morethan2second")) {
                        timelist1.add(time + "\t" + overallcpu + "\t" + overallmem + "\t" + procpu + "\t" + promem);
                        Double outputCPU = 0.0;
                        Double outputMEM = 0.0;
                        Double outputCPUP = 0.0;
                        Double outputMEMP = 0.0;
                        for (int i = 0; i < timelist2.size(); i++) {
                            outputCPU += Double.valueOf(timelist2.get(i).split("\t")[1]);
                            outputMEM += Double.valueOf(timelist2.get(i).split("\t")[2]);
                            outputCPUP += Double.valueOf(timelist2.get(i).split("\t")[3]);
                            outputMEMP += Double.valueOf(timelist2.get(i).split("\t")[4]);
                        }
                        result = Double.toString(outputCPU / timelist2.size()) + "\t" + Double.toString(outputMEM / timelist2.size()) + "\t" + Double.toString(outputCPUP / timelist2.size()) + "\t" + Double.toString(outputMEMP / timelist2.size());
                        timelist2.clear();

                    }
                }

            }



        } catch (Exception es) {
            es.printStackTrace();
        } finally {
            return result;
        }
    }

    private float[] initData() {
        float[] data = new float[COUNT];
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
        return data;
    }

    public void start() {
        timer.start();
    }

    public static void main(final String[] args) {
        System.out.println("*********************************************");
        System.out.println("* The Project 3 in B534 class  *");
        System.out.println("* Resource Monitoring System   *");
        System.out.println("*********************************************");

        final String IP = args[0];//"129.79.49.181";//
        final String PortNo = args[1];//"61617";//
        final String syncMethod = args[2];//"2";//1--timestamp buffer or 2--uid 


        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                Monitor monitor = new Monitor(TITLE, IP, PortNo, syncMethod);
                monitor.pack();
                RefineryUtilities.centerFrameOnScreen(monitor);
                monitor.setVisible(true);
                monitor.start();
            }
        });
    }
}
