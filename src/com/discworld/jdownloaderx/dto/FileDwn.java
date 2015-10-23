package com.discworld.jdownloaderx.dto;

public class FileDwn extends CFile
{
   private int iStatus;
   
   public FileDwn()
   {
      super();
      iStatus = 0;
   }
   
   public FileDwn(String sName, String sURL, int iStatus)
   {
      super(sName, sURL);
      this.iStatus = iStatus;
   }
   
   public FileDwn(CFile oFile)
   {
      super(oFile.getName(), oFile.getURL());
      iStatus = 0;
   }

   public int getStatus()
   {
      return iStatus;
   }

   public void setStatus(int iStatus)
   {
      this.iStatus = iStatus;
   }

}
