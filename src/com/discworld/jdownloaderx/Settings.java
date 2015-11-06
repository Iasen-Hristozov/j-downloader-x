package com.discworld.jdownloaderx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"sDownloadFolder","iMaxSimConn"})
@XmlRootElement(name = "settings")


public class Settings
{
   @XmlElement(name = "download_folder", required = true)
   public String sDownloadFolder = "Download";
   @XmlElement(name = "max_sim_conn", required = true)
   public int iMaxSimConn = 2;
}
