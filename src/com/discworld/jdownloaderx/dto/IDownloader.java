package com.discworld.jdownloaderx.dto;

import java.util.ArrayList;

public interface IDownloader
{
   abstract public void onHttpParseDone(ArrayList<CFile> alFilesFnd);
   abstract public boolean isStarted();
   abstract public void setFileProgress(CFile oFile, int progress);
   abstract public void deleteFileFromLists(CFile oFile);
   abstract public void deleteFileFromQueue(CFile oFile);
   abstract public void saveFilesList();
   abstract public void checkContetsVsPlugins(String sPath, String sContents);
}
