import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import javax.swing.DefaultListModel;

/**
 * 
 * @author Fatema Shabbir 19201960
 */
public class NewsFeedPage extends javax.swing.JFrame {
    
    public static HashMap<String, PluginInterface> newsMap; //to hold all the plugin names
    MapCreator mapCreator = new MapCreator(); //Inits the has map creator class
    List<String> list = new ArrayList<String>(); //holds the list of the headlines
    List<String> updatingList = new ArrayList<String>(); //holds the currently updating sources
    Map<String, Timer> taskMap = new HashMap<String, Timer>(); //holds the tasks
    Scheduler s = new Scheduler(); //Creates an object of the scheduler
    private final Object mutex = new Object(); //Used for synchronization of list
    private final Object mutexUpdate = new Object(); //Used for synchronization of updatingList
    private final Object mutexTasks = new Object(); //Used for synchronization of taskMap
    private final Object mutexSchedule = new Object(); //Used for synchronization
    
    private static String[] args;

    /**
     * Creates new form NewsFeedPage
     */
    public NewsFeedPage() {
        
        if(args != null)
        {
            initComponents();
            //Code adapted from https://www.sourcecodester.com/tutorials/java/7078/display-current-date-java-gui.html
            //Accessed 22/10/17
            javax.swing.Timer t = new javax.swing.Timer(1000, new DateListener());
            t.start();

            Collections.synchronizedList (list);
            Collections.synchronizedList (updatingList);
            Collections.synchronizedMap(taskMap);
            startTasks("all", 0);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     * All code in here is auto-gnerated by the Netbeans IDE
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        listNews = new javax.swing.JList<>();
        lblDate = new javax.swing.JLabel();
        btnUpdate = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        listUpdates = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setViewportView(listNews);

        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        jScrollPane2.setViewportView(listUpdates);

        jLabel1.setText("Currently Downloading From:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                            .addComponent(btnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(27, 27, 27)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1024, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(lblDate, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(74, 74, 74)
                        .addComponent(btnUpdate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
                        .addComponent(btnCancel)
                        .addGap(60, 60, 60)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Action performed when the update button is clicked
     */
    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        
        //Runs a fresh instance of every plugin via timer so that it does not affect the periodic updates
        for (String key : newsMap.keySet()) {
            
            PluginInterface pi = newsMap.get(key);
            final Callable<List<String>> c = (Callable<List<String>>) newsMap.get(key);
            if(!updatingList.contains(pi.getName()))
            {
                TimerTask timerTask = new TimerTask(){  
                    @Override
                    public void run() 
                    {                    
                        PluginInterface pi = newsMap.get(key);
                        updateCurrentList(pi.getName().toString(), "add");

                        try 
                        {
                            List<String> temp;
                            temp = (List<String>) c.call();
                            list(temp, pi.getName());
                        } 
                        catch (Exception ex) {
                            System.err.println("Exception in timer task: " + ex.getMessage());
                        }
                        
                        updateCurrentList(pi.getName().toString(), "remove");
                    }
                };
                
                synchronized(mutexSchedule)
                {
                    Timer t = new Timer();
                    t.schedule(timerTask, 0);
                }
            }
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    /**
     * Action performed when the cancel button is clicked
     */
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
            
        //cancels the currently running downloads and then starts it again
        if(updatingList.size() > 0)
        {
            for(int x = 0; x < updatingList.size(); x++)
            {
                if(taskMap.containsKey(updatingList.get(x)))
                {
                    taskMap.get(updatingList.get(x)).cancel();
                    taskMap.get(updatingList.get(x)).purge();
                    
                    synchronized(mutexUpdate)
                    {
                        updatingList.remove(x);
                    }
                    synchronized(mutexTasks)
                    {
                        taskMap.remove(updatingList.get(x));
                    }
                }

                try{
                    String key = getKey(updatingList.get(x));
                    //startTasks(updatingList.get(x), newsMap.get(key).getUpdateFrequency());
                }
                catch (Exception e)
                {
                    //System.err.println("Caught a fun execption");
                }
            }
        }
    }//GEN-LAST:event_btnCancelActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //All code in this section is autogenerated by the Netbeans IDE
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NewsFeedPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NewsFeedPage().setVisible(true);
            }
        });
        //End auto-generated code
        
        NewsFeedPage.args = args;
        
    }
    
    /**
     * This method adds and removes from the list as required after it is passed the updated list and the source
     */
    private synchronized void list(List<String> listNew, String url) {
        DefaultListModel<String> model = new DefaultListModel<String>();
        boolean add = true;
        
        //Loop to check if there are new elements and add any new ones
        for (String element : listNew) 
        {
            for(int x=0; x<list.size(); x++)
            {

                String listText = list.get(x).substring(0, list.get(x).lastIndexOf("(") - 1);
                String elemText = element.substring(0, element.lastIndexOf("(") - 1);
                if(listText.equalsIgnoreCase(elemText))
                {
                    add = false;
                }
            }
            
            if(add == true)
            {
                synchronized(mutex)
                {
                    list.add(element);
                }
            }
            else
            {
                add = true;
            }
        }
        
        //Loop to remove any elements from main list that aren't in the new list
        for(int x=0; x<list.size(); x++) {
            boolean matched = false;
            String listText = list.get(x).substring(0, list.get(x).lastIndexOf("(") - 1);
            for(int y=0; y<listNew.size(); y++) {
                String newText = listNew.get(y).substring(0, listNew.get(y).lastIndexOf("(") - 1);
                if (listText.equalsIgnoreCase(newText))
                {
                    matched = true;
                    break;
                }
            }
            
            if(matched == false)
            {
                if(listText.contains(url))
                {
                    synchronized(mutex)
                    {
                        list.remove(x);
                    }
                }
            }
        }
        
        for (String heading : list) {
            model.add(0, heading);
        }
        
        listNews.setModel(model);
    }
    
    /**
     * This method starts all the tasks
     * It creates timer tasks for each source and schedules them via the scheduler
     * The timer variable is stored in a list for when cancel is clicked
     */
    private void startTasks(String task, long delay) {
        newsMap = mapCreator.createHashMap(args);
        PluginInterface pi;
        
        if(task.equalsIgnoreCase("all")) {
            for (String key : newsMap.keySet()) {

                //Code adapted from: https://stackoverflow.com/questions/438312/how-to-schedule-a-callable-to-run-on-a-specific-time
                //Accessed 24/10/7
                final Callable<List<String>> c = (Callable<List<String>>) newsMap.get(key);
                TimerTask timerTask = new TimerTask(){

                    @Override
                    public void run() 
                    {                    
                        PluginInterface pi = newsMap.get(key);
                        updateCurrentList(pi.getName().toString(), "add");

                        try 
                        {
                            List<String> temp;
                            temp = (List<String>) c.call();
                            list(temp, pi.getName());
                        } 
                        catch (Exception ex) {
                            System.err.println("Exception in timer task: " + ex.getMessage());
                            startTasks(task, delay);
                        }
                        updateCurrentList(pi.getName().toString(), "remove");
                    }
                };

                pi = newsMap.get(key);
                try 
                {   
                    synchronized(mutexTasks)
                    {
                        taskMap.put(pi.getName(), s.schedule((pi.getUpdateFrequency()*60000), timerTask, delay));
                    }
                } catch (InterruptedException ex) {
                    System.err.println("Exception in scheduling: " + ex.getMessage());
                    startTasks(task, delay);
                }
            }
        }
        else
        {
            //Code adapted from: https://stackoverflow.com/questions/438312/how-to-schedule-a-callable-to-run-on-a-specific-time
            //Accessed 24/10/7
            final Callable<List<String>> c = (Callable<List<String>>) newsMap.get(getKey(task));
            TimerTask timerTask = new TimerTask(){

                @Override
                public void run() 
                {                    
                    PluginInterface pi = newsMap.get(getKey(task));
                    updateCurrentList(pi.getName().toString(), "add");
                    
                    try 
                    {
                        List<String> temp;
                        temp = (List<String>) c.call();
                        list(temp, pi.getName());
                    } 
                    catch (Exception ex) {
                        System.err.println("Exception in timer task: " + ex.getMessage());
                        startTasks(getKey(task), delay);
                    }
                    updateCurrentList(pi.getName().toString(), "remove");
                }
            };

            pi = newsMap.get(getKey(task));
            try 
            {  
                synchronized(mutexTasks)
                {
                    taskMap.put(pi.getName(), s.schedule((pi.getUpdateFrequency()*600), timerTask, delay));
                }
            } 
            catch (InterruptedException ex) {
                System.err.println("Exception in scheduling: " + ex.getMessage());
                startTasks(getKey(task), delay);
            }
        }
    }
    
    /**
     * This method take the plugin name and compares it to the hashmap to give the proper key
     * @param task
     * @return 
     */
    private String getKey(String task) {
        String key = null;
        for (String value : newsMap.keySet()) {
            if(value.contains(task))
            {
                key = value;
                break;
            }
        }
        return key;
    }
    
    /**
     * This method updates the list of the currently downloading sources
     */
    public synchronized void updateCurrentList(String url, String op) 
    {
        DefaultListModel<String> modelUpdates = new DefaultListModel<String>();
        
        if (op.equalsIgnoreCase("add"))
        {
            synchronized(mutexUpdate)
            {
                updatingList.add(url);
            }
            
        }
        else if (op.equalsIgnoreCase("remove"))
        {
            synchronized(mutexUpdate)
            {
                updatingList.remove(url);
            }
        }
        
        for (String source : updatingList) {
            modelUpdates.addElement(source);
        }
        
        listUpdates.setModel(modelUpdates);
    }
    
    /**
     * Nested class to implement action listener and constantly update the datetime via the timer
     */
    class DateListener implements ActionListener {
        @Override
    	public void actionPerformed(ActionEvent e) {
            //Code adapted from https://www.mkyong.com/java/java-how-to-get-current-date-time-date-and-calender/
            //Accessed 22/10/17
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
            LocalDateTime now = LocalDateTime.now();
            lblDate.setText(dtf.format(now));
 
    	}
    }
    
    /**
     * Code is generated by the Netbeans IDE
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblDate;
    private javax.swing.JList<String> listNews;
    private javax.swing.JList<String> listUpdates;
    // End of variables declaration//GEN-END:variables
}
