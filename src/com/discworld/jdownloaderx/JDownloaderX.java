package com.discworld.jdownloaderx;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

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
import com.discworld.jdownloaderx.dto.ChitankaPlugin;
import com.discworld.jdownloaderx.dto.ClipboardListener;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.dto.JABXList;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.ScrollPaneConstants;

public class JDownloaderX extends JFrame implements ActionListener, IDownloader
{
   /**
    * 
    */
   private static final long serialVersionUID = -8419017423255693399L;

   public final static int     PNL_NDX_DWN = 0,
                               PNL_NDX_FND = 1,
                               MAX_DWN = 2;
   
   private final static String DOWNLOAD_FLD = "Download",
                               FILE_LIST = "files.xml";
   
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
   
   private ClipboardListener oClipboardListener;
   
   private JTable tblFilesDwn;
   
   private JScrollPane spFilesDwn;
   
   FileURLsTableModel oFileURLsTableModel;
   
   FileDownloadTableModel oFileDownloadTableModel;

   Vector<CFile> vFilesDwn,
                 vFilesFnd,
                 vFilesCur;
   
   ChitankaPlugin oChitankaPlugin = new ChitankaPlugin(this);
   
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
   
   public class ProgressCellRender extends JProgressBar implements TableCellRenderer 
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

   /**
    * Create the application.
    */
   public JDownloaderX()
   {
      super("JDownloaderX " + (sVersion != null ? sVersion : "" ));
      
      initialize();
      
      vFilesCur = new Vector<CFile>();
      
      Runnable checkContent = new Runnable()
      {
         
         @Override
         public void run()
         {
            String sContent = oClipboardListener.getContent();
            
            if(sContent.contains(ChitankaPlugin.DOMAIN))
            {
               ArrayList<String> alURLs = ChitankaPlugin.parseClipboard(sContent);
               String.join(",", alURLs);
               txtURL.setText(String.join(",", alURLs));
               try
               {
                  for(String sURL : alURLs)
                     vParseURL(sURL);
               } catch(IOException e)
               {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
         }
      };      
      
      oClipboardListener = new ClipboardListener(checkContent);
      oClipboardListener.itisNotEnough();
      oClipboardListener.start();
      
      loadFiles();
      oFileDownloadTableModel.setValues(vFilesDwn);
      oFileDownloadTableModel.fireTableDataChanged();
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
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      
      spFilesDwn = new JScrollPane();
      spFilesDwn.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      panel.add(spFilesDwn);
      
      tblFilesDwn = new JTable();
      vFilesDwn = new Vector<CFile>();
      oFileDownloadTableModel = new FileDownloadTableModel(vFilesDwn);
      tblFilesDwn.setModel(oFileDownloadTableModel);
      tblFilesDwn.getColumn("Progress").setCellRenderer(new ProgressCellRender());
      
      spFilesDwn.setViewportView(tblFilesDwn);
      
      tabbedPane.addTab("Downloads", null, panel, null);
      tabbedPane.setMnemonicAt(0, KeyEvent.VK_2);      
      
      tabbedPane.setTabPlacement(JTabbedPane.TOP);
      
      panel = new JPanel();
      tabbedPane.addTab("Link Grabber", null, panel, null);
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      
      JPanel pnlSearch = new JPanel();
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
      
      tblFilesUrl = new JTable();
      
      vFilesFnd = new Vector<CFile>();
      oFileURLsTableModel = new FileURLsTableModel(vFilesFnd);
      tblFilesUrl.setModel(oFileURLsTableModel);
      
      JScrollPane spFilesUrl = new JScrollPane(tblFilesUrl);
      spFilesUrl.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      
//      JScrollBar sb = spFilesUrl.getVerticalScrollBar();
//      sb.setPreferredSize(new Dimension(50, 0));

      // Put it to the left.
//      spFilesUrl.remove(sb);
      panel.add(spFilesUrl);
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
      for(CFile oFile : vFilesFnd)
      {
         if(!vFilesDwn.contains(oFile))
            vFilesDwn.add(oFile);
      }
      
      _saveFiles();
    
      oFileDownloadTableModel.fireTableDataChanged();
      tabbedPane.setSelectedIndex(PNL_NDX_DWN);
      vFilesFnd.removeAllElements();
      oFileURLsTableModel.fireTableDataChanged();
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
               vFilesDwn.remove(tiRowNdxs[i]);
            oFileDownloadTableModel.fireTableDataChanged();
            
            _saveFiles();
         break;
         
         case PNL_NDX_FND:
            tiRowNdxs = tblFilesUrl.getSelectedRows();
            
            if(tiRowNdxs.length == 0)
               break;
            
//            for(int i = 0; i < tiRowNdxs.length; i++)
            for(int i = tiRowNdxs.length-1; i >= 0; i--)
               vFilesFnd.remove(tiRowNdxs[i]);
            oFileURLsTableModel.fireTableDataChanged();
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
      if(sURL.contains(ChitankaPlugin.DOMAIN))
      {
         oChitankaPlugin.vParseUrl(sURL);
      }
   }


   
   private synchronized void _saveFiles()
   {
      try 
      {
         File file = new File(FILE_LIST);
         JAXBContext jaxbContext = JAXBContext.newInstance(JABXList.class, CFile.class);
         Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    
         JABXList<CFile> Files = new JABXList<CFile>(vFilesDwn);
         
         // output pretty printed
         jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

         jaxbMarshaller.marshal(Files, file);
         jaxbMarshaller.marshal(Files, System.out);
      } 
      catch (JAXBException e) 
      {
         e.printStackTrace();
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

         vFilesDwn.clear();
         
         vFilesDwn = new Vector<CFile>(Files.getValues());
      } 
      catch (JAXBException e) 
      {
         e.printStackTrace();
      }
   }

   private class downloadThread extends SwingWorker<Void, Void> 
   {
      @Override
      protected Void doInBackground() throws Exception
      {
         try
         {
            while(_isStarted())
            {
               if(vFilesDwn.size() == 0)
               {
                  vToggleButton();
                  setIsStarted(false);
                  break;
               }
               
               for(CFile oFile: vFilesDwn)
               {
                  if(!vFilesCur.contains(oFile))
                  {
                     addFile(oFile);
                     oChitankaPlugin.downloadFile(oFile, DOWNLOAD_FLD);
                  }
                  if(vFilesCur.size() >= MAX_DWN)
                     break;
                  
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
   
   public synchronized boolean _isStarted()
   {
      return bIsStarted;
   }
   
   private synchronized void setIsStarted(boolean bIsStarted)
   {
      this.bIsStarted = bIsStarted; 
   }
   
   private synchronized void _setFileProgress(CFile oFile, int progress)
   {
      oFileDownloadTableModel.updateStatus(oFile, progress);
   }
   
   private synchronized void _deleteFile(CFile oFile)
   {
      vFilesDwn.remove(oFile);
      oFileDownloadTableModel.setValues(vFilesDwn);
      oFileDownloadTableModel.fireTableDataChanged();
      vFilesCur.remove(oFile);
   }

   private synchronized void addFile(CFile oFile)
   {
      vFilesCur.add(oFile);
   }

   @Override
   public void onHttpParseDone(ArrayList<CFile> alFilesFnd)
   {
      vFilesFnd.addAll(alFilesFnd);

      oFileURLsTableModel.fireTableDataChanged();
      
      tabbedPane.setSelectedIndex(PNL_NDX_FND);
      
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
   public void deleteFile(CFile oFile)
   {
      _deleteFile(oFile);
   }

   @Override
   public void saveFiles()
   {
      _saveFiles();
      
   }

}
