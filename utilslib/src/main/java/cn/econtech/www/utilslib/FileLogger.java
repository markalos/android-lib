package cn.econtech.www.utilslib;

import android.os.Environment;

import java.io.File;

/**
 * Created by Admin-01 on 2017/12/28.
 */

public final class FileLogger {

    private final static String DEFAULT_FILE = "LOG.txt";

    private static final String defaultFolder;

    static {
        defaultFolder = Environment.getExternalStorageDirectory() +
                File.separator +
                FileLogger.class.getName();
        defaultFolderConform();
    }

    private static void defaultFolderConform() {
        File theFolder = new File(defaultFolder);
        if (!theFolder.exists()) {
            System.out.println("creating directory: " + theFolder.getName());
            try {
                theFolder.mkdir();

            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }
    }

    private static String getAbsolutePath(String fileName) {
        return defaultFolder + File.separator + fileName;
    }

    private static class LoggerHolder {
        private final static FileLogger logger = new FileLogger();
    }

    private FileLogger() {
        //no instance
    }

    public static FileLogger getInstance() {
        return LoggerHolder.logger;
    }

    public void logMsg(final String msg, final  String fileName, final  boolean toAppend) {
        FileUtils.writeStringToFile(msg, getAbsolutePath(fileName), toAppend);
    }

    public void  logMsg(final String msg, final  String fileName) {
        logMsg(msg, fileName, false);
    }

    public void logMsg(final String msg) {
        logMsg(msg, DEFAULT_FILE, false);
    }

    public void logMsg(final byte [] msg, final  String fileName, final  boolean toAppend) {
        FileUtils.writeByteToFile(msg, fileName, toAppend);
    }

    public void logMsg(final byte [] msg, final  String fileName) {
        FileUtils.writeByteToFile(msg, fileName, false);
    }


}
