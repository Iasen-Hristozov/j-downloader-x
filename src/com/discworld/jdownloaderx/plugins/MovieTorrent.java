package com.discworld.jdownloaderx.plugins;

import com.discworld.jdownloaderx.dto.CFile;

public class MovieTorrent extends CFile
{
   private  String sInfo,
                   sMagnet;
   public MovieTorrent()
   {
      super();
   }

   public MovieTorrent(String sName, String sURL)
   {
      super(sName, sURL);
   }   

   public MovieTorrent(String sName, String sURL, String sInfo, String sMagnet)
   {
      super(sName, sURL);
      this.sInfo = sInfo;
      this.sMagnet = sMagnet;
   }   
   
   public String getInfo()
   {
      return sInfo;
   }

   public void setInfo(String sInfo)
   {
      this.sInfo = sInfo;
   }

   public String getMagnet()
   {
      return sMagnet;
   }

   public void setMagnet(String sMagnet)
   {
      this.sMagnet = sMagnet;
   }

}
