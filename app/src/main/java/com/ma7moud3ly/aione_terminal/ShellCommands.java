package com.ma7moud3ly.aione_terminal;


import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

public class ShellCommands {
    private String[] commands;
    private Process process;
    private ShellResponse response;
    private boolean onscript = false;

    public ShellCommands(String[] commands, ShellResponse response) {
        this.commands = commands;
        this.response = response;
    }

    public ShellCommands(String[] commands, ShellResponse response, boolean onscript) {
        this.commands = commands;
        this.response = response;
        this.onscript = onscript;
    }

    public void run() {
        try {
            process = Runtime.getRuntime().exec(commands);
            //stream(process.getInputStream(), process.getOutputStream());
            inputstream(process.getInputStream());
            errStream(process.getErrorStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void eval(boolean sync) {
        try {
            process = Runtime.getRuntime().exec(commands);
            readInputStream(process.getInputStream(), false, sync);
            readInputStream(process.getErrorStream(), true, sync);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void inputstream(final InputStream inputStream) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder tail = new StringBuilder();
                    String line;
                    boolean reach_tail = false;
                    while ((line = r.readLine()) != null) {
                        if (line.contains(Interface.SS)) {
                            reach_tail = true;
                            int i;
                            if ((i = line.indexOf(Interface.SS)) != 0) {
                                String result = line.substring(0, i);
                                String subtail = line.substring(i);
                                tail.append(subtail + "\n");
                                response.onSuccess(result + "\n");
                                continue;
                            }
                        }
                        if (line.endsWith(Interface.TT)) {
                            tail.append(line);
                            break;
                        }
                        if (!reach_tail) {
                            if (response != null) response.onSuccess(line + "\n");
                        } else {
                            tail.append(line + "\n");
                        }
                    }
                    if (reach_tail) after_processing(tail.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    void errStream(final InputStream inputStream) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = r.readLine()) != null) {
                        if (response != null) response.onError(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    void readInputStream(final InputStream inputStream, final boolean err, boolean sync) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) sb.append(line + "\n");
                    if (sb != null && response != null) {
                        if (!err) response.onSuccess(sb.toString());
                        else response.onError(sb.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        if (sync) thread.run();
        else thread.start();
    }

    void stream(final InputStream inputStream, final OutputStream outputStream) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(outputStream));
                    w.write("env");
                    w.flush();
                    String line;
                    while ((line = r.readLine()) != null) {
                        w.write("ls /sdcard");
                        w.newLine();
                        Log.i("SHELL", line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }


    private void after_processing(String tail) {
        try {
            int i1 = tail.lastIndexOf(Interface.SS);
            int i2 = tail.lastIndexOf(Interface.TT);
            if (i1 != -1 && i2 != -1 && i1 < i2) tail = tail.substring(i1 + 2, i2);
            tail = tail.trim();
            int i;
            String last_var = "USER_ID=";
            if ((i = tail.indexOf(last_var)) != -1) tail = tail.substring(i + last_var.length());
            String[] vars = tail.split("\n");
            HashMap<String, String> set = new HashMap<>();
            for (String key : vars) {
                if ((i = key.indexOf("=")) != -1) {
                    String val = key.substring(i + 1);
                    key = key.substring(0, i);
                    set.put(key, val);
                }
            }
            if (set != null && response != null) {
                response.set(set);
                if (onscript) response.onScript();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isProcessCompleted() {
        try {
            int i = process.exitValue();
            if (i == 0) return true;
        } catch (Exception e) {
        }
        return false;
    }
}

