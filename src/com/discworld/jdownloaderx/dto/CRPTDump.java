package com.discworld.jdownloaderx.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "rpt_dump")
public class CRPTDump extends CFile
{
   @XmlElement(name = "mp4", required = true)
   private String sMp4;
   @XmlElement(name = "url_enc", required = true)
   private String sURLEnc;

   public CRPTDump()
   {
      super();
   }

   public CRPTDump(String sName, String sURL, String sMp4, String sURLEnc)
   {
      super(sName, sURL);
      this.sMp4 = sMp4;
      this.sURLEnc = sURLEnc;
   }
   
   public String getMp4()
   {
      return sMp4;
   }

   public void setMp4(String sMp4)
   {
      this.sMp4 = sMp4;
   }

   public String getURLEnc()
   {
      return sURLEnc;
   }

   public void setURLEnc(String sURLEnc)
   {
      this.sURLEnc = sURLEnc;
   } 

}
