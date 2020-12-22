package com.discworld.jdownloaderx;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
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
   private static final String APP_NAME = "JDownloaderX ";

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
   
   private boolean isStarted = false;
   
   private JButton btnRemove,
                   btnStart,
                   btnSearch;

   private JFrame frame;

   private JTextField txtURL;
   
   private ClipboardListener clipboardListener;
   
   private JTable tblFilesDwn;
   
   private JScrollPane spFilesDwn;
   
   FileDownloadTableModel fileDownloadTableModel;
   
   Settings settings;

   Vector<CFile> downloadFiles,
                 downloadFilesQueue;
   
   private JPanel pnlFilesDwnStatus;
   private JLabel lblFilesDwn;
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
            } 
            catch(Exception e)
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
      super(APP_NAME + (sVersion != null ? sVersion : "" ));
      
//      logger = Logger.getLogger(JDownloaderX.class);
      
      initialize();
      
      downloadFilesQueue = new Vector<CFile>();
      
      //===============================================================
      // Loading plugins
      
      DownloaderPassClass.setDownloader(this);
      
      new File(PLUGIN_FOLDER).mkdirs();
      
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
                  ArrayList<String> alURLs = plugin.getURLsFromContent(sContent);
                  txtURL.setText(String.join(",", alURLs));
                  for(String sURL : alURLs)
                     plugin.parseUrl(sURL);
               }
            }
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

   /**
    * Initialize the contents of the frame.
    */
   private void initialize()
   {
      downloadFiles = new Vector<CFile>();
      fileDownloadTableModel = new FileDownloadTableModel(downloadFiles);
      
      frame = new JFrame(APP_NAME + (sVersion != null ? sVersion : "" ));
      frame.setBounds(100, 100, 348, 319);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      JPanel pnlButtons = new JPanel();
      frame.getContentPane().add(pnlButtons, BorderLayout.NORTH);
      pnlButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      
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
      
      JPanel panel = new JPanel(false);
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      frame.getContentPane().add(panel, BorderLayout.CENTER);
   
      JPanel pnlSearch = new JPanel();
      pnlSearch.setMaximumSize(new Dimension(32767, 30));
      pnlSearch.setMinimumSize(new Dimension(10, 30));
      panel.add(pnlSearch);
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
      
      lblFilesDwnSel = new JLabel(" ");
      tblFilesDwn = new JTable();
      ListSelectionModel listSelectionModel = tblFilesDwn.getSelectionModel();
      listSelectionModel.addListSelectionListener(new SharedListSelectionHandler(lblFilesDwnSel));
      tblFilesDwn.setSelectionModel(listSelectionModel);
      tblFilesDwn.setModel(fileDownloadTableModel);
      tblFilesDwn.getColumn("Progress").setCellRenderer(new ProgressCellRender());
   
      spFilesDwn = new JScrollPane();
      spFilesDwn.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      panel.add(spFilesDwn);
      
      spFilesDwn.setViewportView(tblFilesDwn);
      
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
               remove();            
         }
         
         @Override
         public void keyPressed(KeyEvent e)
         {
            
         }
      };
      tblFilesDwn.addKeyListener(klDelete);
   }

   private void loadPlugins(final File pluginFolder) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
   {
      for (final File file : pluginFolder.listFiles()) 
      {
         if (file.isDirectory()) 
         {
             loadPlugins(file);
         } 
         else 
         {
            if(!file.getName().endsWith(PLUGIN_SUFFIX))
               continue;
             
            ClassLoader classLoader = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() });
            classLoader.loadClass(FileUtils.getClassName(file.getAbsolutePath())).newInstance();
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
      catch(JAXBException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }      
   }

   @SuppressWarnings("unchecked")
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

   private void updateFilesDwnTable()
   {
      fileDownloadTableModel.fireTableDataChanged();
      lblFilesDwn.setText(String.valueOf(downloadFiles.size()));      
   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();
   
      if(source == btnSearch)
         search();
      else if(source == btnRemove)
         remove();
      else if(source == btnStart)
         startStopDownload();
   }

   @Override
   public void onHttpParseDone(ArrayList<CFile> alFilesFound)
   {
      addFilesFound(alFilesFound);
   }

   @Override
   public void checkContetsVsPlugins(String sPath, String sContents)
   {
      addFilesFound(PluginFactory.checkContetWithPlugins(sPath, sContents));
   }

   private void addFilesFound(ArrayList<CFile> alFilesFound)
   {
      boolean hasNewFiles = false;
      for(CFile file : alFilesFound)
         hasNewFiles = _checkAddFileToList(file);
      
      if(hasNewFiles)
      {
         _saveFiles();
         updateFilesDwnTable();
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

   @Override
   public void deleteFileFromQueue(CFile file)
   {
      _deleteFileFromQueue(file);
   }

   @Override
   public void saveFilesList()
   {
      _saveFiles();
   }

   private void search()
   {
      try
      {
         parseURL(txtURL.getText());
      } catch(IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   private void remove()
   {
      int[] tiRowNdxs;
      tiRowNdxs = tblFilesDwn.getSelectedRows();
      
      if(tiRowNdxs.length == 0)
         return;

      for(int i = tiRowNdxs.length-1; i >= 0; i--)
         downloadFiles.remove(tiRowNdxs[i]);

      _saveFiles();

      updateFilesDwnTable();
   }
   
   private void startStopDownload()
   {
      _toggleButton();
      
      if(_isStarted())
      {
         new DownloadThread().execute();
      }
   }
   
   private void parseURL(String sURL) throws IOException
   {
      Matcher matcher = ptnDomain.matcher(sURL);
      if(matcher.find())
      {
         Plugin plugin = PluginFactory.getPlugin(matcher.group(DOMAIN_GROUP));
         if(plugin != null)
            plugin.parseUrl(sURL);
      }
   }
   
   private synchronized boolean _checkAddFileToList(CFile file)
   {
      boolean hasNewFiles = false;
      
      if(!downloadFiles.contains(file))
      {
         downloadFiles.add(file);
         hasNewFiles = true;
      }
      return hasNewFiles;
   }

   private synchronized void _toggleButton()
   {
      setIsStarted(!_isStarted());
      btnStart.setIcon(new ImageIcon(JDownloaderX.class.getResource(_isStarted() ? "/icons/stop.png" : "/icons/play.png")));
   }

   private synchronized boolean _isStarted()
   {
      return isStarted;
   }

   private synchronized void _setFileProgress(CFile file, int progress)
   {
      fileDownloadTableModel.updateStatus(file, progress);
   }

   private synchronized void _deleteFile(CFile file)
   {
//      logger.info("Remove " + oFile.getURL());
      downloadFiles.remove(file);
      updateFilesDwnTable();
      lblFilesDwn.setText(String.valueOf(downloadFiles.size()));
      _deleteFileFromQueue(file);
   }
   
   private synchronized void _addFileToQueue(CFile file)
   {
      downloadFilesQueue.add(file);
   }

   private synchronized void _deleteFileFromQueue(CFile file)
   {
//      logger.info("Remove from queue " + oFile.getURL());
      downloadFilesQueue.remove(file);
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
      } 
      catch (JAXBException e) 
      {
         e.printStackTrace();
      }      
   }

   private synchronized void setIsStarted(boolean isStarted)
   {
      this.isStarted = isStarted; 
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

   private class DownloadThread extends SwingWorker<Void, Void> 
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
                  _toggleButton();
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
                  CFile file = downloadFiles.get(i);
                  if(!downloadFilesQueue.contains(file))
                  {
                     _addFileToQueue(file);
//                     logger.info("Add file " + oFile.getURL());
                     Matcher matcher = ptnDomain.matcher(file.getURL());
                     if(matcher.find())
                     {
                        Plugin plugin = PluginFactory.getPlugin(matcher.group(DOMAIN_GROUP));
                        if(plugin != null)
                           plugin.downloadFile(file, settings.sDownloadFolder);
                     }
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
      JLabel label;
      public SharedListSelectionHandler(JLabel label)
      {
         this.label = label; 
      }
      
      @Override
      public void valueChanged(ListSelectionEvent e) 
      {
         int count = 0;
          ListSelectionModel lsm = (ListSelectionModel)e.getSource();

          if (lsm.isSelectionEmpty()) 
          {
             label.setText(" ");
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
              label.setText(count + " of");
          }
      }
   }
}
