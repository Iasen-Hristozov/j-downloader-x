package com.discworld.jdownloaderx.test;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.discworld.jdownloaderx.dto.CFile;
import com.discworld.jdownloaderx.dto.IDownloader;
import com.discworld.jdownloaderx.plugins.ZamundaSe;

public class test_plugin 
{
   
   public static void main(String[] args)
   {
      IDownloader oDownloader =  new IDownloader()
      {
         
         @Override
         public void setFileProgress(CFile oFile, int progress)
         {
            // TODO Auto-generated method stub
            
         }
         
         @Override
         public void saveFiles()
         {
            // TODO Auto-generated method stub
            
         }
         
         @Override
         public void onHttpParseDone(ArrayList<CFile> alFilesFnd)
         {
            // TODO Auto-generated method stub
            
         }
         
         @Override
         public boolean isStarted()
         {
            // TODO Auto-generated method stub
            return true;
         }
         
         @Override
         public void deleteFile(CFile oFile)
         {
            // TODO Auto-generated method stub
            
         }
      };
      
      
      ZamundaSe oZamundaSePlugin = new ZamundaSe(oDownloader);
      
      oZamundaSePlugin.vParseUrl("http://zelka.org/details.php?id=454728");
      
//      (new HTTPParser()).execute();
      
      class Task extends SwingWorker<String, Void>
      {

         @Override
         protected String doInBackground() throws Exception
         {
            System.out.print("Swing Worker");
            return null;
         }
         
      };
      
      Task oTask = new Task();
      oTask.execute();
      try
      {
         oTask.get();
      } catch(InterruptedException | ExecutionException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   


}
