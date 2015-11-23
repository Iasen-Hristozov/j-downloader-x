package com.discworld.jdownloaderx.test;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.plugins.Chitanka;
import com.discworld.jdownloaderx.plugins.ZamundaSe;

public class PluginTest extends JFrame implements ActionListener, IDownloader
{
   JButton btnTest;
   private JPanel contentPane;

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
               PluginTest frame = new PluginTest();
               frame.setVisible(true);
            } catch(Exception e)
            {
               e.printStackTrace();
            }
         }
      });
   }

   /**
    * Create the frame.
    */
   public PluginTest()
   {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(100, 100, 450, 300);
      contentPane = new JPanel();
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      contentPane.setLayout(new BorderLayout(0, 0));
      setContentPane(contentPane);
      
      btnTest = new JButton("test");
      btnTest.addActionListener(this);
      contentPane.add(btnTest, BorderLayout.NORTH);
   }
   
   @Override
   public void actionPerformed(ActionEvent e)
   {
      Object oSource = e.getSource();
   
      if(oSource == btnTest)
         startPlugin();
         
   }
   
   void startPlugin()
   {
//      File f = new File("Download\\ddd" + File.separator+ "ggg" + File.separator+ "bbb.txt");
//      try
//      {
//         f.getParentFile().mkdirs();
//         f.createNewFile();
//
////         FileOutputStream outputStream = new FileOutputStream("c:\\aaa" + File.separator+ "bbb.txt");
//         FileOutputStream outputStream = new FileOutputStream(f);
//         outputStream.write("test".getBytes());
//      } catch(FileNotFoundException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      } catch(IOException e)
//      {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//      }
      
      ZamundaSe oZamundaSePlugin = new ZamundaSe(this);
      
      oZamundaSePlugin.vParseUrl("http://zelka.org/details.php?id=455785");
//      oZamundaSePlugin.vParseUrl("http://zelka.org/details.php?id=455831");
//      oZamundaSePlugin.vParseUrl("http://zelka.org/details.php?id=454728");
      
//      ChitankaPlugin oChitankaPlugin = new ChitankaPlugin(this);
//      
//      oChitankaPlugin.vParseUrl("http://chitanka.info/text/35878-ohljuv-po-strymnoto");
   }

   @Override
   public void onHttpParseDone(ArrayList<CFile> alFilesFnd)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public boolean isStarted()
   {
      return true;
   }

   @Override
   public void setFileProgress(CFile oFile, int progress)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void deleteFile(CFile oFile)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void saveFiles()
   {
      // TODO Auto-generated method stub
      
   }   

}
