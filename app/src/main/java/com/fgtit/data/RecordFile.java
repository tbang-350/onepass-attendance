package com.fgtit.data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class RecordFile {

    public static void CreateFile(String fileName) {
        new File(fileName);
    }


    public static void AppendToFile(String fileName, RecordItem rs) {
        try {
            RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);

            byte[] content = new byte[140];
            System.arraycopy(rs.id.getBytes(), 0, content, 0, rs.id.getBytes().length);
            System.arraycopy(rs.name.getBytes(), 0, content, 16, rs.name.getBytes().length);
            System.arraycopy(rs.datetime.getBytes(), 0, content, 32, rs.datetime.getBytes().length);
            System.arraycopy(rs.lat.getBytes(), 0, content, 64, rs.lat.getBytes().length);
            System.arraycopy(rs.lng.getBytes(), 0, content, 80, rs.lng.getBytes().length);
            System.arraycopy(rs.type.getBytes(), 0, content, 96, rs.type.getBytes().length);

            System.arraycopy(rs.type.getBytes(), 0, content, 100, rs.worktype.getBytes().length);
            System.arraycopy(rs.type.getBytes(), 0, content, 108, rs.linetype.getBytes().length);
            System.arraycopy(rs.type.getBytes(), 0, content, 116, rs.depttype.getBytes().length);
            System.arraycopy(rs.device.getBytes(), 0, content, 124, rs.device.getBytes().length);

            randomFile.write(content);
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void AppendToFileCsv(String fileName, RecordItem rs) {
        try {
            RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);

            byte[] content = new byte[140];
            System.arraycopy(rs.id.getBytes(), 0, content, 0, rs.id.getBytes().length);
            System.arraycopy(rs.name.getBytes(), 0, content, 16, rs.name.getBytes().length);
            System.arraycopy(rs.datetime.getBytes(), 0, content, 32, rs.datetime.getBytes().length);
            System.arraycopy(rs.time.getBytes(), 0, content, 64, rs.time.getBytes().length);
            System.arraycopy(rs.lat.getBytes(), 0, content, 80, rs.lat.getBytes().length);
            System.arraycopy(rs.lng.getBytes(), 0, content, 96, rs.lng.getBytes().length);

            System.arraycopy(rs.type.getBytes(), 0, content, 100, rs.worktype.getBytes().length);
            System.arraycopy(rs.type.getBytes(), 0, content, 108, rs.linetype.getBytes().length);
            System.arraycopy(rs.type.getBytes(), 0, content, 116, rs.depttype.getBytes().length);
            System.arraycopy(rs.device.getBytes(), 0, content, 124, rs.device.getBytes().length);

            byte[] contentGbks = new String(content).getBytes("GBK");
            randomFile.write(contentGbks);
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<RecordItem> ReadFromFile(String fileName) {
        ArrayList<RecordItem> list = new ArrayList<RecordItem>();
        try {
            RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
            long fileLength = randomFile.length();
            long count = fileLength / 140;
            for (long i = 0; i < count; i++) {
                byte[] content = new byte[140];
                randomFile.read(content);
                RecordItem rc = new RecordItem();

                rc.id = new String(content, 0, 16);
                rc.id = rc.id.replaceAll("\\s", "").trim();
                rc.name = new String(content, 16, 16);
                rc.name = rc.name.replaceAll("\\s", "").trim();
                rc.datetime = new String(content, 32, 32);
                rc.datetime = rc.datetime.replaceAll("\\s", "").trim();
                rc.lat = new String(content, 64, 16);
                rc.lat = rc.lat.replaceAll("\\s", "").trim();
                rc.lng = new String(content, 80, 16);
                rc.lng = rc.lng.replaceAll("\\s", "").trim();
                rc.type = new String(content, 96, 4);
                rc.type = rc.type.replaceAll("\\s", "").trim();
                rc.worktype = new String(content, 100, 8);
                rc.worktype = rc.worktype.replaceAll("\\s", "").trim();
                rc.linetype = new String(content, 108, 8);
                rc.linetype = rc.linetype.replaceAll("\\s", "").trim();
                rc.depttype = new String(content, 116, 8);
                rc.depttype = rc.depttype.replaceAll("\\s", "").trim();

                rc.device = new String(content, 124, 16);
                rc.device = rc.device.replaceAll("\\s", "").trim();

                list.add(rc);
            }
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean IsFileExists(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            return true;
        }
        return false;
    }

    public static void DeleteFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            f.delete();
        }
    }

    public static void ReCreate(String filename) {
        DeleteFile(filename);
        CreateFile(filename);
    }
}
