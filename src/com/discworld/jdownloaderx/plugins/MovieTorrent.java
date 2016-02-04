package com.discworld.jdownloaderx.plugins;

import javax.xml.bind.annotation.XmlRootElement;

import com.discworld.jdownloaderx.dto.CFile;

@XmlRootElement(name = "movie_torrent")
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

   /*
package com.discworld.jdownloaderx.plugins;

import com.discworld.jdownloaderx.dto.CFile;

public class MovieTorrent extends CFile
{
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
      this.sParameter1 = sInfo;
      this.sParameter2 = sMagnet;
   }   
   
   public String getInfo()
   {
      return sParameter1;
   }

   public void setInfo(String sInfo)
   {
      this.sParameter1 = sInfo;
   }

   public String getMagnet()
   {
      return sParameter2;
   }

   public void setMagnet(String sMagnet)
   {
      this.sParameter2 = sMagnet;
   }

}
 
    */
   
}
