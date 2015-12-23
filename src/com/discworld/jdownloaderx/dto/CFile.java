package com.discworld.jdownloaderx.dto;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"sName","sURL","sParameter1","sParameter2","sParameter3"})
@XmlRootElement(name = "file")


public class CFile
{
   @Override
   public boolean equals(Object obj)
   {
      return ((CFile) obj).getName().equals(sName) && ((CFile) obj).getURL().equals(sURL);
   }

   @XmlElement(name = "name", required = true)
   private String sName;
   @XmlElement(name = "url", required = true)
   private String sURL;
   @XmlTransient
   private int iStatus;
   @XmlElement(name = "parameter1", required = true)
   protected String sParameter1 = null;
   @XmlElement(name = "parameter2", required = true)
   protected String sParameter2 = null;
   @XmlElement(name = "parameter3", required = true)
   protected String sParameter3 = null;
   
   
   public CFile()
   {
      sName = "";
      sURL = "";
   }
   
   public CFile(String sName, String sURL)
   {
      this.sName = sName;
      this.sURL = sURL;
   }
   
   public String getName()
   {
      return sName;
   }

   public void setName(String sName)
   {
      this.sName = sName;
   }

   public String getURL()
   {
      return sURL;
   }

   public void setURL(String sURL)
   {
      this.sURL = sURL;
   }

   public void setStatus(int iStatus)
   {
      this.iStatus = iStatus;
   }
   
   public int getStatus()
   {
      return iStatus;
   }

}
