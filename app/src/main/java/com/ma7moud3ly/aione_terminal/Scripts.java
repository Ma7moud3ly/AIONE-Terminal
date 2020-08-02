package com.ma7moud3ly.aione_terminal;

import android.os.Environment;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class Scripts {

    public static boolean delete(String name) {
        File file = new File(name);
        if (!file.exists()) return false;
        try {
            file.delete();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void rename(String name, String new_name) {
        File file = new File(name);
        File new_file = new File(new_name);
        if (!file.exists()) return;
        try {
            file.renameTo(new_file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String read(String name) {
        File file = new File(name);
        if (!file.exists())
            return "not found";
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            byte[] byt = new byte[dis.available()];
            dis.readFully(byt);
            dis.close();
            String content = new String(byt, 0, byt.length);
            content = de_modify(content);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
            return e.getStackTrace().toString();
        }
    }


    public static boolean write(String name, String data) {
        data = modify(data);
        File file = new File(name);
        try {
            if (!file.exists())
                file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.append(data);
            writer.flush();
            writer.close();
            out.close();
            return true;
        } catch (Exception e) {
            file.delete();
            e.printStackTrace();
            return false;
        }
    }

    public static boolean nonEmptyStr(String str) {
        if (!str.equals(null) && str.trim().length() > 0)
            return true; // return true for non empty string
        return false;

    }

    public static boolean emptyStr(String str) {
        return str.equals(null) || str.trim().length() > 0;
    }


    public static String modify(String code) {
        code = code.replace("$scripts", MainActivity.scripts_path);
        code = code.replace("$sd", Environment.getExternalStorageDirectory().toString());
        code = code.replace("$dir", MainActivity.data_dir);
        code = code.replace("$sbin", "/system/bin");
        return code;
    }

    public static String de_modify(String code) {
        code = code.replace(MainActivity.scripts_path, "$scripts");
        code = code.replace(Environment.getExternalStorageDirectory().toString(), "$sd");
        code = code.replace(MainActivity.data_dir, "$dir");
        code = code.replace("/system/bin", "$sbin");
        return code;
    }


    static public class Script {
        public String ext, type, run;

        public Script(String type, String ext, String run) {
            this.type = type;
            this.ext = ext;
            this.run = run;
        }
    }

}
