package com.ma7moud3ly.aione_terminal;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class TerminalActivity extends AppCompatActivity {
    private TextView output;
    private EditText input;
    private ScrollView scroll;
    private TextView cursor;
    private TextView directory;
    private View terminal_btns;
    public static int history_index;
    private ArrayList<String> history = new ArrayList<>();
    private Cursor dbCursor;
    public static SQLiteDatabase settingsDB;
    private int font_size;
    private boolean isDark = true;
    private String current_directory = "";
    private Interface mInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getEditorSettings();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terminal);
        if (MainActivity.script_list.isEmpty()) MainActivity.init_list(this);
        init_terminal();
        script();
    }

    private boolean keyboardShown(View rootView) {
        final int softKeyboardHeight = 100;
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        int heightDiff = rootView.getBottom() - r.bottom;
        return heightDiff > softKeyboardHeight * dm.density;
    }

    private void script() {
        Intent intent = getIntent();
        if (intent.hasExtra("path")) {
            String run = intent.getStringExtra("run");
            String path = intent.getStringExtra("path");
            output.append("$ " + path + "\n");
            new Interface(output, input, scroll, directory, run, path);
        }
    }

    private void init_terminal() {
        LinearLayout layout = findViewById(R.id.terminal_layout);
        if (isDark) layout.setBackgroundColor(getResources().getColor(android.R.color.black));
        else layout.setBackgroundColor(getResources().getColor(android.R.color.white));
        cursor = findViewById(R.id.cursor);
        input = findViewById(R.id.input);
        scroll = findViewById(R.id.scroll);
        terminal_btns = findViewById(R.id.terminal_btns);
        directory = findViewById(R.id.directory);
        directory.setText(current_directory);
        directory.setTextIsSelectable(true);
        directory.setHorizontallyScrolling(true);
        directory.setHorizontalScrollBarEnabled(true);
        output = findViewById(R.id.output);
        output.setTextIsSelectable(true);
        //output.setHorizontallyScrolling(true);
        //output.setHorizontalScrollBarEnabled(true);
        output.setTextSize(TypedValue.COMPLEX_UNIT_DIP, font_size);
        input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, font_size);
        cursor.setTextSize(TypedValue.COMPLEX_UNIT_DIP, font_size);

        input.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (keyboardShown(input.getRootView())) {
                    terminal_btns.setVisibility(View.GONE);
                    ////directory.setVisibility(View.GONE);
                } else {
                    terminal_btns.setVisibility(View.VISIBLE);
                    ////directory.setVisibility(View.VISIBLE);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            output.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    scroll.requestDisallowInterceptTouchEvent(true);
                }
            });
        }
        mInterface = new Interface(output, input, scroll, directory);
    }

    @Override
    protected void onResume() {
        dbCursor = settingsDB.rawQuery("select * from 'history'", null);
        while (dbCursor.moveToNext()) history.add(dbCursor.getString(0));
        history_index = history.size() - 1;
        super.onResume();
    }

    public void insert(View v) {
        input.getText().insert(input.getSelectionEnd(), ((TextView) v).getText());
    }

    public void termBtn(View v) {
        switch (v.getId()) {
            case R.id.termClear:
                clear();
                break;
            case R.id.termBackward:
                backward();
                break;
            case R.id.termForward:
                forward();
                break;
            case R.id.termTrans:
                readInput();
                break;
            case R.id.termZoomIn:
                zoom(true);
                break;
            case R.id.termZoomOut:
                zoom(false);
                break;
            case R.id.termDarkMode:
                isDark = !isDark;
                super.recreate();
                break;
        }

    }

    private void backward() {
        if (history_index == history.size())
            history_index--;
        if (history_index >= 0 && history_index < history.size()) {
            input.setText(history.get(history_index));
            if (history_index > 0) history_index--;
        }
    }

    private void forward() {
        if (history_index == 0)
            history_index++;
        if (history_index <= history.size() - 1 && history_index >= 0) {
            input.setText(history.get(history_index));
            history_index++;
        }
    }

    private void zoom(boolean in) {
        if (in && font_size < 40) font_size += 5;
        else if (!in && font_size > 5) font_size -= 5;
        cursor.setTextSize(TypedValue.COMPLEX_UNIT_DIP, font_size);
        input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, font_size);
        output.setTextSize(TypedValue.COMPLEX_UNIT_DIP, font_size);
    }

    public void clear() {
        output.setText("");
        history_index = history.size() - 1;
        input.setText("");
    }


    private void getEditorSettings() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        font_size = sharedPref.getInt("font_size", 12);
        isDark = sharedPref.getBoolean("dark_mode", true);
        current_directory = sharedPref.getString("pwd", "/");
        settingsDB = openOrCreateDatabase("settings", MODE_PRIVATE, null);
        settingsDB.execSQL("CREATE TABLE IF NOT EXISTS history(value VARCHAR);");

        if (isDark) super.setTheme(R.style.AppThemeDark);
        else super.setTheme(R.style.AppThemeLight);
    }

    private void setEditorSettings() {
        SharedPreferences.Editor sharedPref = getPreferences(Context.MODE_PRIVATE).edit();
        sharedPref.putInt("font_size", font_size);
        sharedPref.putBoolean("dark_mode", isDark);
        sharedPref.putString("pwd", Interface.pwd);
        sharedPref.commit();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == 66) {
            readInput();
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onDestroy() {
        setEditorSettings();
        super.onDestroy();
    }

    public void readInput() {
        final String code = input.getText().toString();
        if (code.trim().isEmpty()) return;

        if (code.equals("cls") || code.equals("clear")) {
            clear();
            return;
        }
        if (code.equals("quit") || code.equals("exit")) {
            finish();
        }
        input.setText("");
        output.append("\n");
        output.append(Html.fromHtml("<font color=\"#15ab0d\">$ " + code + "</font>"));
        output.append("\n");
        history.add(code);
        try {
            settingsDB.execSQL("INSERT INTO history(value) VALUES('" + code + "')");
        } catch (Exception e) {
            e.printStackTrace();
        }
        history_index = history.size() - 1;
        //hideKeyboard(this);
        mInterface.EvalShellCommand(code);
        setEditorSettings();
    }

    private void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        setEditorSettings();
        finish();
        //super.onBackPressed();
    }

}
