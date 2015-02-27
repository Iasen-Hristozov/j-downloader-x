package com.discworld.jdownloaderx.dto;

public class BookDwn extends Book
{
   private int iStatus;
   
   public BookDwn()
   {
      super();
      iStatus = 0;
   }
   
   public BookDwn(String sName, String sURL, int iStatus)
   {
      super(sName, sURL);
      this.iStatus = iStatus;
   }
   
   public BookDwn(Book oBook)
   {
      super(oBook.getName(), oBook.getURL());
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
