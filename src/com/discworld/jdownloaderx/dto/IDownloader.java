package com.discworld.jdownloaderx.dto;

import java.util.ArrayList;

public interface IDownloader
{
   abstract public void onHttpParseDone(ArrayList<CFile> alFilesFnd);
//   abstract public void onBackgroudDownload();
//   abstract public void onDownloadDone();
   abstract public boolean isStarted();
   abstract public void setFileProgress(CFile oFile, int progress);
//   abstract public void addFile(CFile oFile);
   abstract public void deleteFile(CFile oFile);
   abstract public void saveFiles();
//   abstract public void updateFilesFndTable();
}
