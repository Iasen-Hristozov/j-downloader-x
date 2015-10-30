package com.discworld.jdownloaderx.dto;

import javax.swing.SwingWorker;

import com.discworld.jdownloaderx.UnzipUtility;

public class ExtractFile extends SwingWorker<Void, Void>
{
   String zipFilePath, destDirectory;
   
   UnzipUtility oUnzipUtility;
   
   public ExtractFile(String zipFilePath, String destDirectory)
   {
      this.zipFilePath = zipFilePath;
      this.destDirectory = destDirectory;
      oUnzipUtility = new UnzipUtility();
   }
   
   @Override
   protected Void doInBackground() throws Exception
   {
      oUnzipUtility.unzip(zipFilePath, destDirectory);
      return null;
   }
}
