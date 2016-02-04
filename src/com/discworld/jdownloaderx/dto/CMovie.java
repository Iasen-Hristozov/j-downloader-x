package com.discworld.jdownloaderx.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "movie")
public class CMovie extends CFile
{
   @XmlElement(name = "magnet", required = true)
   private String sMagnet;
   @XmlElement(name = "info", required = true)
   private String sInfo;

   public CMovie()
   {
      
   }
   
   public CMovie(String sName, String sURL, String sMagnet, String sInfo)
   {
      super(sName, sURL);
      this.sMagnet = sMagnet;
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

   public String getInfo()
   {
      return sInfo;
   }

   public void setInfo(String sInfo)
   {
      this.sInfo = sInfo;
   } 

}
