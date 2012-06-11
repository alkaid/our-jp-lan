package com.alkaid.ojpl.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class UnZipUtil {
	/**
	 * 解压zip文件
	 * @param archive 被解压文件路径+名称
	 * @param decompressDir
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ZipException
	 */
    public void readByApacheZipFile(String archive, String decompressDir)   
            throws IOException, FileNotFoundException, ZipException {
    	BufferedInputStream bi;   
        ZipFile zf = new ZipFile(new File(archive));
        File f = new File(decompressDir);
        Enumeration e = zf.entries();   
        while (e.hasMoreElements()) {   
            ZipEntry ze2 = (ZipEntry) e.nextElement();   
            String entryName = ze2.getName();   
            entryName=entryName.replace("\\", "/");
            String path = decompressDir + "/" + entryName;   
            if (ze2.isDirectory()) {   
                File decompressDirFile = new File(path);   //创建目录
                if (!decompressDirFile.exists()) {   
                    decompressDirFile.mkdirs();   
                }   
            } else {   
                String fileDir = path.substring(0, path.lastIndexOf("/"));   
                File fileDirFile = new File(fileDir);   
                if (!fileDirFile.exists()) {   
                    fileDirFile.mkdirs();   
                }   
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(   
                		decompressDir + "/" + entryName));   
  
                bi = new BufferedInputStream(zf.getInputStream(ze2));   
                byte[] readContent = new byte[1024];   
                int readCount = bi.read(readContent);   
                while (readCount != -1) {   
                    bos.write(readContent, 0, readCount);   
                    readCount = bi.read(readContent);   
                }   
                bos.close();   
            }   
        }   
        zf.close();   
        IOUtil.delFileDir(archive);
    }
    
}
