package com.discworld.jdownloaderx;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import java.awt.Component;

import javax.swing.ImageIcon;

import java.awt.FlowLayout;

import javax.swing.JTextField;

import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.ClipboardListener;
import com.discworld.jdownloaderx.dto.DownloaderPassClass;
import com.discworld.jdownloaderx.dto.FileUtils;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.JABXList;
import com.discworld.jdownloaderx.dto.Plugin;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ScrollPaneConstants;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Dimension;

public class JDownloaderX extends JFrame implements ActionListener, IDownloader
{
   /**
    * 
    */
   private static final long serialVersionUID = -8419017423255693399L;

//   Logger logger;

   public final static int     PNL_NDX_DWN = 0,
                               PNL_NDX_FND = 1;
   
   private final static String FILE_LIST = "files.xml",
                               SETTINGS = "settings.xml",
                               PLUGIN_FOLDER = "plugins",
                               PLUGIN_SUFFIX = ".jar";
   
   private final static Pattern ptnDomain = Pattern.compile("^(?:.*:\\/\\/)?([^:\\/]*).*$"); // Group 1
//   private final static Pattern ptnDomain = Pattern.compile("^((http[s]?|ftp):\\/)?\\/?([^:\\/\\s]+)((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$"); // Group 3
   private final static int DOMAIN_GROUP = 1; 
   
   private static String sVersion;
   
   private boolean bIsStarted = false;
   
   private JButton btnAdd,
                   btnRemove,
                   btnStart,
                   btnSearch;

   private JFrame frame;
   
   JTabbedPane tabbedPane;

   private JTextField txtURL;
   
   private JTable tblFilesUrl;
   
   private ClipboardListener clipboardListener;
   
   private JTable tblFilesDwn;
   
   private JScrollPane spFilesDwn;
   
   FileURLsTableModel fileURLsTableModel;
   
   FileDownloadTableModel fileDownloadTableModel;
   
   Settings settings;

   Vector<CFile> downloadFiles,
                 foundFiles,
                 downloadFilesQueue;
   
   private static ArrayList<Plugin> alPlugins = new ArrayList<Plugin>();
   private JPanel pnlFilesDwnStatus;
   private JLabel lblFilesDwn;
   private JPanel pnlFilesFndStatus;
   private JPanel panel_2;
   private JLabel lblFilesFnd;
   private JLabel lblFilesFndSel;
   private JLabel lblFilesDwnSel;
   
   /**
    * Launch the application.
    */
   public static void main(String[] args)
   {
      EventQueue.invokeLater(new Runnable()
      {
         public void run()
         {
            try
            {
               Package p = this.getClass().getPackage();
               sVersion = p.getImplementationVersion();

               
               JDownloaderX window = new JDownloaderX();
               window.frame.setVisible(true);
               
               
            } catch(Exception e)
            {
               e.printStackTrace();
            }
         }
      });
   }
   
   /**
    * Create the application.
    */
   public JDownloaderX()
   {
      super("JDownloaderX " + (sVersion != null ? sVersion : "" ));
      
//      logger = Logger.getLogger(JDownloaderX.class);
      
      initialize();
      
      downloadFilesQueue = new Vector<CFile>();
      
      //===============================================================
      // Loading plugins
      
      DownloaderPassClass.setDownloader(this);
      
      new File(PLUGIN_FOLDER).mkdirs();
      
//      Policy.setPolicy(new PluginPolicy());
//      System.setSecurityManager(new SecurityManager());
      
//      alPlugins = new ArrayList<Plugin>();
      
      final File fPluginFolder = new File(System.getProperty("user.dir") + "\\" + PLUGIN_FOLDER);
      
      try
      {
         loadPlugins(fPluginFolder);
      } 
      catch(InstantiationException | IllegalAccessException | ClassNotFoundException | IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }                  
      
      Runnable checkContent = new Runnable()
      {
         @Override
         public void run()
         {
            String sContent = clipboardListener.getContent();
            if(sContent == null)
               return;
            
            Matcher matcher = ptnDomain.matcher(sContent);
            while(matcher.find())
            {
               Plugin plugin = PluginFactory.getPlugin(matcher.group(DOMAIN_GROUP));
               if(plugin != null) 
               {
                  ArrayList<String> alURLs = plugin.parseContent(sContent);
                  txtURL.setText(String.join(",", alURLs));
                  for(String sURL : alURLs)
                     plugin.parseUrl(sURL);
               }
            }
            
//            for(Plugin oPlugin: alPlugins)
//            {
//               if(oPlugin.isMine(sContent))
//               {
//                  ArrayList<String> alURLs = oPlugin.parseContent(sContent);
//                  txtURL.setText(String.join(",", alURLs));
//                  
//                  for(String sURL : alURLs)
//                     oPlugin.parseUrl(sURL);
//                  break;
//               }
//            }      
         }
      };      
      
      clipboardListener = new ClipboardListener(checkContent);
      clipboardListener.itisNotEnough();
      clipboardListener.start();
      
      loadSettings();
      
      loadFiles();
      
      fileDownloadTableModel.setValues(downloadFiles);
      updateFilesDwnTable();
      

   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
      Object oSource = e.getSource();
   
      if(oSource == btnSearch)
         vSearch();
      else if(oSource == btnAdd)
         vAdd();
      else if(oSource == btnRemove)
         vRemove();
      else if(oSource == btnStart)
         vStartStop();
   }

   @Override
   public void onHttpParseDone(ArrayList<CFile> alFilesFnd)
   {
//      vFilesFnd.addAll(alFilesFnd);
      boolean isNew = false;
      for(CFile oFile : alFilesFnd)
         if(!foundFiles.contains(oFile))
         {
            foundFiles.add(oFile);
            isNew = true;
         }
   
      
      if(isNew)
      {
         updateFilesFndTable();
         
         tabbedPane.setSelectedIndex(PNL_NDX_FND);
      }
   }

   @Override
   public boolean isStarted()
   {
      return _isStarted();
   }

   @Override
   public void setFileProgress(CFile oFile, int progress)
   {
      _setFileProgress(oFile, progress);
   }

   @Override
   public void deleteFileFromLists(CFile oFile)
   {
      _deleteFile(oFile);
   }

   /**
    * Initialize the contents of the frame.
    */
   private void initialize()
   {
      frame = new JFrame("JDownloaderX " + (sVersion != null ? sVersion : "" ));
      frame.setBounds(100, 100, 348, 319);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      JPanel pnlButtons = new JPanel();
      frame.getContentPane().add(pnlButtons, BorderLayout.NORTH);
      
      btnAdd = new JButton("");
      btnAdd.setAlignmentX(Component.CENTER_ALIGNMENT);
      btnAdd.setToolTipText("Add");
      btnAdd.setIcon(new ImageIcon(JDownloaderX.class.getResource("/icons/1421707472_add.png")));
      btnAdd.addActionListener(this);
      pnlButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      pnlButtons.add(btnAdd);
      
      btnStart = new JButton("");
      btnStart.setAlignmentX(Component.CENTER_ALIGNMENT);
      btnStart.setToolTipText("Start");
      btnStart.setIcon(new ImageIcon(JDownloaderX.class.getResource("/icons/play.png")));
      btnStart.addActionListener(this);
      pnlButtons.add(btnStart);
      
      btnRemove = new JButton("");
      btnRemove.setAlignmentX(Component.CENTER_ALIGNMENT);
      btnRemove.setToolTipText("Remove");
      btnRemove.setIcon(new ImageIcon(JDownloaderX.class.getResource("/icons/1421707488_delete.png")));
      btnRemove.addActionListener(this);
      pnlButtons.add(btnRemove);
      
      tabbedPane = new JTabbedPane(JTabbedPane.TOP);
      frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
      
      JPanel panel = new JPanel(false);
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      
      spFilesDwn = new JScrollPane();
      spFilesDwn.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      panel.add(spFilesDwn);
      
      lblFilesDwnSel = new JLabel(" ");
      tblFilesDwn = new JTable();
      ListSelectionModel listSelectionModel = tblFilesDwn.getSelectionModel();
      listSelectionModel.addListSelectionListener(new SharedListSelectionHandler(lblFilesDwnSel));
      tblFilesDwn.setSelectionModel(listSelectionModel);
      downloadFiles = new Vector<CFile>();
      fileDownloadTableModel = new FileDownloadTableModel(downloadFiles);
      tblFilesDwn.setModel(fileDownloadTableModel);
      tblFilesDwn.getColumn("Progress").setCellRenderer(new ProgressCellRender());
      
      spFilesDwn.setViewportView(tblFilesDwn);
      
      tabbedPane.addTab("Downloads", null, panel, null);
      
      pnlFilesDwnStatus = new JPanel();
      pnlFilesDwnStatus.setMaximumSize(new Dimension(32767, 30));
      FlowLayout fl_pnlFilesDwnStatus = (FlowLayout) pnlFilesDwnStatus.getLayout();
      fl_pnlFilesDwnStatus.setAlignment(FlowLayout.RIGHT);
      pnlFilesDwnStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
      panel.add(pnlFilesDwnStatus);
      
      lblFilesDwnSel.setHorizontalAlignment(SwingConstants.RIGHT);
      lblFilesDwnSel.setFont(new Font("Tahoma", Font.PLAIN, 11));
      pnlFilesDwnStatus.add(lblFilesDwnSel);
      
      lblFilesDwn = new JLabel("0");
      lblFilesDwn.setFont(new Font("Tahoma", Font.PLAIN, 11));
      pnlFilesDwnStatus.add(lblFilesDwn);
      tabbedPane.setMnemonicAt(0, KeyEvent.VK_2);      
      
      tabbedPane.setTabPlacement(JTabbedPane.TOP);
      
      panel_2 = new JPanel();
      tabbedPane.addTab("Link Grabber", null, panel_2, null);
      panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
      
      JPanel pnlSearch = new JPanel();
      pnlSearch.setMaximumSize(new Dimension(32767, 30));
      pnlSearch.setMinimumSize(new Dimension(10, 30));
      panel_2.add(pnlSearch);
      pnlSearch.setLayout(new BorderLayout(0, 0));
      
      txtURL = new JTextField();
      txtURL.setHorizontalAlignment(SwingConstants.LEFT);
      pnlSearch.add(txtURL, BorderLayout.CENTER);
      txtURL.setFont(new Font("Courier New", Font.PLAIN, 13));
      txtURL.setColumns(10);
      
      btnSearch = new JButton("");
      btnSearch.setIcon(new ImageIcon(JDownloaderX.class.getResource("/icons/Search.png")));
      btnSearch.setToolTipText("Search");
      btnSearch.setAlignmentX(0.5f);
      btnSearch.addActionListener(this);
      pnlSearch.add(btnSearch, BorderLayout.EAST);
      
      lblFilesFndSel = new JLabel();
      lblFilesFndSel.setText(" ");
      tblFilesUrl = new JTable();
      listSelectionModel = tblFilesUrl.getSelectionModel();
      listSelectionModel.addListSelectionListener(new SharedListSelectionHandler(lblFilesFndSel));
      tblFilesUrl.setSelectionModel(listSelectionModel);
      foundFiles = new Vector<CFile>();
      fileURLsTableModel = new FileURLsTableModel(foundFiles);
      tblFilesUrl.setModel(fileURLsTableModel);
      
      JScrollPane spFilesUrl = new JScrollPane(tblFilesUrl);
      spFilesUrl.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      
      panel_2.add(spFilesUrl);
      
      pnlFilesFndStatus = new JPanel();
      pnlFilesFndStatus.setMaximumSize(new Dimension(32767, 30));
      FlowLayout flowLayout = (FlowLayout) pnlFilesFndStatus.getLayout();
      flowLayout.setAlignment(FlowLayout.RIGHT);
      pnlFilesFndStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

      panel_2.add(pnlFilesFndStatus);
      
      lblFilesFndSel.setHorizontalAlignment(SwingConstants.RIGHT);
      lblFilesFndSel.setFont(new Font("Tahoma", Font.PLAIN, 11));
      pnlFilesFndStatus.add(lblFilesFndSel);
      
      lblFilesFnd = new JLabel("0");
      lblFilesFnd.setHorizontalTextPosition(SwingConstants.LEFT);
      lblFilesFnd.setHorizontalAlignment(SwingConstants.LEFT);
      lblFilesFnd.setFont(new Font("Tahoma", Font.PLAIN, 11));
      pnlFilesFndStatus.add(lblFilesFnd);
      
      KeyListener klDelete = new KeyListener()
      {
         
         @Override
         public void keyTyped(KeyEvent e)
         {
         }
         
         @Override
         public void keyReleased(KeyEvent e)
         {
            if(e.getKeyCode() == KeyEvent.VK_DELETE)
               vRemove();            
         }
         
         @Override
         public void keyPressed(KeyEvent e)
         {
            
         }
      };
      
      tblFilesDwn.addKeyListener(klDelete);
      tblFilesUrl.addKeyListener(klDelete);
   }

   @Override
   public void saveFilesList()
   {
      _saveFiles();
      
   }

   private void loadPlugins(final File fPluginFolder) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
   {
      for (final File fEntry : fPluginFolder.listFiles()) 
      {
         if (fEntry.isDirectory()) 
         {
             loadPlugins(fEntry);
         } 
         else 
         {
//             System.out.println(fEntry.getName());
             
            if(!fEntry.getName().endsWith(PLUGIN_SUFFIX))
               continue;
             
            ClassLoader oClassLoader = URLClassLoader.newInstance(new URL[] { fEntry.toURL() });
            Plugin plugin = (Plugin) oClassLoader.loadClass(FileUtils.getClassName(fEntry.getAbsolutePath())).newInstance();
            alPlugins.add(plugin);
         }
      }      
   }

   private void loadSettings()
   {
      try
      {
         JAXBContext jaxbContext = JAXBContext.newInstance(Settings.class);
         
         File file = new File(SETTINGS);
         if(file.exists())
         {
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            settings = (Settings)jaxbUnmarshaller.unmarshal(file);
         }
         else
         {
            settings = new Settings();
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(settings, file);
         }
         
      } 
      catch(JAXBException e1)
      {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }      
   }
   
   private void loadFiles()
   {
      try 
      {
         File file = new File(FILE_LIST);
         if(!file.exists())
            return;
         JAXBContext jaxbContext = JAXBContext.newInstance(JABXList.class, CFile.class);
         Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
         JABXList<CFile> Files = (JABXList<CFile>)jaxbUnmarshaller.unmarshal(file);
   
         downloadFiles.clear();
         
         downloadFiles = new Vector<CFile>(Files.getValues());
      } 
      catch (JAXBException e) 
      {
         e.printStackTrace();
      }
   }

   private void vSearch()
   {
      try
      {
         vParseURL(txtURL.getText());
      } catch(IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   private void vAdd()
   {
      for(CFile oFile : foundFiles)
      {
         if(!downloadFiles.contains(oFile))
            downloadFiles.add(oFile);
      }

      _saveFiles();
    
      updateFilesDwnTable();
      tabbedPane.setSelectedIndex(PNL_NDX_DWN);

      foundFiles.removeAllElements();
      updateFilesFndTable();
   }
   
   private void vRemove()
   {
      int[] tiRowNdxs;
      switch(tabbedPane.getSelectedIndex())
      {
         case PNL_NDX_DWN:
            tiRowNdxs = tblFilesDwn.getSelectedRows();
            
            if(tiRowNdxs.length == 0)
               break;
            
//            for(int i = 0; i < tiRowNdxs.length; i++)
            for(int i = tiRowNdxs.length-1; i >= 0; i--)
               downloadFiles.remove(tiRowNdxs[i]);
            updateFilesDwnTable();
            
            _saveFiles();
         break;
         
         case PNL_NDX_FND:
            tiRowNdxs = tblFilesUrl.getSelectedRows();
            
            if(tiRowNdxs.length == 0)
               break;
            
//            for(int i = 0; i < tiRowNdxs.length; i++)
            for(int i = tiRowNdxs.length-1; i >= 0; i--)
               foundFiles.remove(tiRowNdxs[i]);
            updateFilesFndTable();
         break;
      }      
   }
   
   private void vStartStop()
   {
      vToggleButton();
      
      if(_isStarted())
      {
         new downloadThread().execute();
      }
   }
   
   private synchronized void vToggleButton()
   {
      setIsStarted(!_isStarted());
      btnStart.setIcon(new ImageIcon(JDownloaderX.class.getResource(_isStarted() ? "/icons/stop.png" : "/icons/play.png")));
   }
   
   private void vParseURL(String sURL) throws IOException
   {
      Matcher matcher = ptnDomain.matcher(sURL);
      if(matcher.find())
      {
//         String s = matcher.group(1);
         Plugin plugin = PluginFactory.getPlugin(matcher.group(DOMAIN_GROUP));
         if(plugin != null)
            plugin.parseUrl(sURL);
      }
      
//      for(Plugin oPlugin: alPlugins)
//      {
//         if(oPlugin.isMine(sURL))
//         {
//            oPlugin.parseUrl(sURL);
//            break;
//         }
//      }      
   }
   
   public synchronized boolean _isStarted()
   {
      return bIsStarted;
   }

   private synchronized void _setFileProgress(CFile oFile, int progress)
   {
      fileDownloadTableModel.updateStatus(oFile, progress);
   }

   private synchronized void _deleteFile(CFile oFile)
   {
//      logger.info("Remove " + oFile.getURL());
      downloadFiles.remove(oFile);
      updateFilesDwnTable();
      lblFilesDwn.setText(String.valueOf(downloadFiles.size()));
      _deleteFileFromQueue(oFile);
   }
   
   private synchronized void _deleteFileFromQueue(CFile oFile)
   {
//      logger.info("Remove from queue " + oFile.getURL());
      downloadFilesQueue.remove(oFile);
   }

   private synchronized void _saveFiles()
   {
      try 
      {
         File file = new File(FILE_LIST);
         JAXBContext jaxbContext = JAXBContext.newInstance(JABXList.class, CFile.class);
         Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
   
         JABXList<CFile> Files = new JABXList<CFile>(downloadFiles);
         
         // output pretty printed
         jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
   
         jaxbMarshaller.marshal(Files, file);
//         jaxbMarshaller.marshal(Files, System.out);
      } 
      catch (JAXBException e) 
      {
         e.printStackTrace();
      }      
   }

   private synchronized void setIsStarted(boolean bIsStarted)
   {
      this.bIsStarted = bIsStarted; 
   }

   private synchronized void addFile(CFile oFile)
   {
      downloadFilesQueue.add(oFile);
   }
   
   private class ProgressCellRender extends JProgressBar implements TableCellRenderer 
      {
         /**
          * 
          */
         private static final long serialVersionUID = -2555436479986175987L;
   
         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
         {
             int progress = 0;
             if (value instanceof Float) 
             {
                 progress = Math.round(((Float) value) * 100f);
             } 
             else if (value instanceof Integer) 
             {
                 progress = (Integer) value;
             }
             setValue(progress);
             return this;
         }
     }

   private class downloadThread extends SwingWorker<Void, Void> 
   {
      @Override
      protected Void doInBackground() throws Exception
      {
         try
         {
            int i = 0;
            while(_isStarted())
            {
               if(downloadFiles.size() == 0)
               {
                  vToggleButton();
                  setIsStarted(false);
                  break;
               }
               
//               logger.info("Queue size " + vFilesCur.size() + " List size " + vFilesDwn.size() + " i " + i);
               if(downloadFilesQueue.size() >= settings.iMaxSimConn)
               {
                  Thread.sleep(100);
                  continue;
               }
               if(i < downloadFiles.size())
               {
                  CFile oFile = downloadFiles.get(i);
                  if(!downloadFilesQueue.contains(oFile))
                  {
                     addFile(oFile);
//                     logger.info("Add file " + oFile.getURL());
                     Matcher matcher = ptnDomain.matcher(oFile.getURL());
                     if(matcher.find())
                     {
                        Plugin plugin = PluginFactory.getPlugin(matcher.group(DOMAIN_GROUP));
                        if(plugin != null)
                           plugin.downloadFile(oFile, settings.sDownloadFolder);
                     }
                     
//                     for(Plugin oPlugin: alPlugins)
//                     {
//                        if(oPlugin.isMine(oFile.getURL()))
//                        {
//                           oPlugin.downloadFile(oFile, oSettings.sDownloadFolder);
//                           break;
//                        }
//                     }
                  }
                  i++;
               }
               else
               {
                  i = 0;
               }
               
               Thread.sleep(100);
            }
         } 
         catch(InterruptedException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         return null;
      }
   }
   
   class SharedListSelectionHandler implements ListSelectionListener 
   {
      JLabel oLabel;
      public SharedListSelectionHandler(JLabel oLabel)
      {
         this.oLabel = oLabel; 
      }
      
      @Override
      public void valueChanged(ListSelectionEvent e) 
      {
         int count = 0;
          ListSelectionModel lsm = (ListSelectionModel)e.getSource();

          if (lsm.isSelectionEmpty()) 
          {
             oLabel.setText(" ");
          } 
          else 
          {
              // Find out which indexes are selected.
              int minIndex = lsm.getMinSelectionIndex();
              int maxIndex = lsm.getMaxSelectionIndex();
              count = 0;
              for (int i = minIndex; i <= maxIndex; i++) 
              {
                  if(lsm.isSelectedIndex(i)) 
                     count++;
              }
              oLabel.setText(count + " of");
          }
      }
  }
   
   private void updateFilesFndTable()
   {
      fileURLsTableModel.fireTableDataChanged();
      lblFilesFnd.setText(String.valueOf(foundFiles.size()));
   }
   
   private void updateFilesDwnTable()
   {
      fileDownloadTableModel.fireTableDataChanged();
      lblFilesDwn.setText(String.valueOf(downloadFiles.size()));      
   }

   @Override
   public void deleteFileFromQueue(CFile oFile)
   {
      _deleteFileFromQueue(oFile);
   }

   private HashMap<String, Plugin> m_RegisteredProducts = new HashMap<String, Plugin>();
   
   public void registerProduct(String pluginID, Plugin p)    
   {
      m_RegisteredProducts.put(pluginID, p);
   }
   
   public Plugin createProduct(String pluginID)
   {
//      ((Product)m_RegisteredProducts.get(pluginID)).createProduct();
      return (Plugin) m_RegisteredProducts.get(pluginID);
   }

   @Override
   public void checkContetsVsPlugins(String sPath, String sContents)
   {
      Plugin plugin = PluginFactory.getPlugin("www.easternspirit.org");
//      plugin.checkContetWithPlugin(sPath, sContents);
      foundFiles.addAll(plugin.checkContetWithPlugin(sPath, sContents));
      plugin = PluginFactory.getPlugin("bukvi.bg");
      foundFiles.addAll(plugin.checkContetWithPlugin(sPath, sContents));
      plugin = PluginFactory.getPlugin("www.addic7ed.com");
      foundFiles.addAll(plugin.checkContetWithPlugin(sPath, sContents));
      plugin = PluginFactory.getPlugin("subs.sab.bz");
      foundFiles.addAll(plugin.checkContetWithPlugin(sPath, sContents));
      plugin = PluginFactory.getPlugin("subsunacs.net");
      foundFiles.addAll(plugin.checkContetWithPlugin(sPath, sContents));
      
      updateFilesFndTable();
   }
}
