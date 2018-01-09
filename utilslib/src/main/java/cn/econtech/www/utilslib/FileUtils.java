package cn.econtech.www.utilslib;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    public static  void writeStringToFile(final String str, final String fileName, final boolean toAppend) {
        String filePath =  fileName;
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new java.io.FileWriter(filePath, toAppend));
            out.write(str);  //Replace with the string
            //you are trying to write
        }
        catch (IOException e)
        {
            System.out.println("Exception " + e.toString());
        }
        finally
        {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public static void writeByteToFile(final byte [] logMsg, final String fileName, final boolean append) {
        String filePath = fileName;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath, append);
            fos.write(logMsg);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fos) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] readByte(final String fileName) {
        FileInputStream fileInputStream = null;
        File file = new File(fileName);
        byte[] bytesArray = new byte[(int) file.length()];
        try {
            fileInputStream = new FileInputStream(file);
            int rbl = fileInputStream.read(bytesArray);
            if (rbl != bytesArray.length) {
                System.err.println("error in reading file : " + fileName);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytesArray;
    }
    public static void main(String[] args) {
        System.out.println("FileUtils");
    }
}
