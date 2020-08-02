package com.ma7moud3ly.aione_terminal;

import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class Interface {
    private EditText input;
    private TextView result;
    private ScrollView scroll;
    private TextView directory;
    private final String CLEAR = "\u001B[2J\u001B[H";
    private String run, path;
    private HashMap<String, String> old_set = new HashMap<>();
    public static String pwd = "/";

    public Interface(TextView result, EditText input, ScrollView scroll, TextView directory) {
        this.result = result;
        this.input = input;
        this.scroll = scroll;
        this.directory = directory;
        pwd = directory.getText().toString();
        init_environment(false);
    }

    public Interface(TextView result, EditText input, ScrollView scroll, TextView directory, String run, String path) {
        this.result = result;
        this.input = input;
        this.scroll = scroll;
        this.directory = directory;
        pwd = directory.getText().toString();
        this.run = run;
        this.path = path;
        init_environment(true);
    }

    private void EvalScript() {
        final String[] commands = {"sh", "-c", preprocessing(run + " " + path)};
        new ShellCommands(commands, response).run();
    }

    public void EvalShellCommand(String command) {
        command = modify(command);
        command = preprocessing(command);
        String[] commands = {"sh", "-c", command};
        new ShellCommands(commands, response).run();
    }

    private String modify(String code) {
        if (code.startsWith("pip")) {
            code = code.replace("pip3.8", "pip");
            code = code.replace("pip3", "pip");
            code = code.replace("pip", "pip --cache-dir=$TEMP");
        }
        return code;
    }

    private class Set {
        private ArrayList<String[]> set = new ArrayList<>();

        public void add(String s1, String s2, String s3) {
            String arr[] = new String[]{s1, s2, s3};
            set.add(arr);
        }

        public String toString() {
            String s = "";
            for (String[] arr : set) s += arr[0] + arr[1] + arr[2] + ";";
            return s;
        }

    }

    private void init_environment(boolean onscript) {
        Set set = new Set();
        set.add("sd", "=", "/sdcard");
        set.add("dir", "=", MainActivity.data_dir);
        set.add("bootstrap", "=", "$dir/bootstrap");
        set.add("scripts", "=", MainActivity.scripts_path);
        set.add("pwd", "=", pwd.startsWith("/") ? pwd : "/");
        set.add("cd", " ", "$pwd");
        set.add("temp", "=", "$dir/bootstrap/temp");
        set.add("path", "=", "$PATH:$dir:$dir/bootstrap/bin");
        set.add("PATH", "=", "$path");
        set.add("TEMP", "=", "$temp");
        set.add("export LD_LIBRARY_PATH", "=", "$dir/bootstrap/lib");
        set.add("export TMPDIR", "=", "$dir/bootstrap/tmp");

        set.add("echo", " ", "\"" + SS + "$(set)" + TT + "\"");
        String[] commands = {"sh", "-c", set.toString()};
        new ShellCommands(commands, response, onscript).run();
    }

    final public static String SS = "@#";
    final public static String TT = "#@";

    private String preprocessing(String command) {
        Set pre = new Set();
        Set tail = new Set();
        for (String key : old_set.keySet()) pre.add(key, "=", old_set.get(key));
        pre.add("export LD_LIBRARY_PATH", "=", "$dir/bootstrap/lib");
        pre.add("export TMPDIR", "=", "$dir/bootstrap/tmp");
        pre.add("PATH", "=", "$path");
        pre.add("TEMP", "=", "$temp");
        pre.add("cd", " ", "$pwd");
        tail.add("pwd", "=", "$(pwd)");
        tail.add("path", "=", "$PATH");
        tail.add("temp", "=", "$TEMP");
        tail.add("echo", " ", "\"" + SS + "$(set)" + TT + "\"");
        return pre.toString() + command + ";" + tail.toString();
    }

    private void clear() {
        result.post(new Runnable() {
            @Override
            public void run() {
                input.setText("");
                result.setText("");
            }
        });
    }

    private String hasClear(String result) {
        if (result.contains(CLEAR)) {
            clear();
            result = result.replace(CLEAR, "");
        }
        return result;
    }


    private ShellResponse response = new ShellResponse() {
        @Override
        public void onSuccess(String s) {
            s = hasClear(s);
            final String ss = s;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    result.append(ss);
                    scroll.fullScroll(View.FOCUS_DOWN);
                }
            });
        }

        @Override
        public void onError(String s) {
            if (s.isEmpty()) return;
            s = hasClear(s);
            final String ss = s.replace("\n", "<br>");
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    result.append("\n");
                    result.append(Html.fromHtml("<font color=\"red\">" + ss + "</font>"));
                    scroll.fullScroll(View.FOCUS_DOWN);
                }
            });
        }

        @Override
        public void set(HashMap<String, String> s) {
            if (s.size() != 0 && s.containsKey("pwd")) {
                old_set = s;
                String p = old_set.get("pwd");
                if (p.startsWith("/")) {
                    pwd = p;
                    directory.post(new Runnable() {
                        @Override
                        public void run() {
                            directory.setText(pwd);
                        }
                    });
                }
            }
        }

        @Override
        public void onScript() {
            EvalScript();
        }
    };

    private String[] GetCommands(EditText input) {
        String cmd = input.getText().toString().trim();
        ArrayList<String> args = new ArrayList<>(Arrays.asList(cmd.split(" ")));
        while (args.remove("")) ;
        return args.toArray(new String[args.size()]);
    }

    private String[] GetCommands(String input) {
        String cmd = input.trim();
        ArrayList<String> args = new ArrayList<>(Arrays.asList(cmd.split(" ")));
        while (args.remove("")) ;
        return args.toArray(new String[args.size()]);
    }
}
