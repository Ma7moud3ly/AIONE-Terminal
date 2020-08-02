package com.ma7moud3ly.aione_terminal;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ma7moud3ly.aione_terminal.Scripts.Script;

public class MainActivity extends AppCompatActivity {


    public static final String scripts_path = Environment.getExternalStorageDirectory() + "/AIONE-Terminal";
    public static String data_dir = "";

    public static ArrayList<Script> script_list = new ArrayList<>();

    public static void init_list(final Context c) {
        script_list.clear();
        script_list.add(new Script("shell script", ".sh", "sh"));
        script_list.add(new Script("text file", ".txt", ""));
        script_list.add(new Script("something else", ".txt", ""));
        data_dir = c.getApplicationInfo().dataDir;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        isStoragePermissionGranted();
        init_list(this);
        final Intent terminal_intent = new Intent(this, TerminalActivity.class);
        final Intent script_intent = new Intent(this, ScriptsActivity.class);
        final Intent settings_intent = new Intent(this, SettingsActivity.class);
        GridView gridview = findViewById(R.id.gridview);
        gridview.setAdapter(new baseAdapter(this));
        final Context context = this;
        final Intent intent = new Intent(context, EditorActivity.class);
        gridview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0://open terminal
                        startActivity(terminal_intent);
                        break;
                    case 1://open editor activity
                        new_script(context, intent);
                        break;
                    case 2://show scripts activity
                        startActivity(script_intent);
                        break;
                    case 3://open setting activity
                        startActivity(settings_intent);
                        break;
                }
            }
        });

    }

    private boolean first_time() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("first_time", true)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("first_time", false);
            editor.commit();
            return true;
        }
        return false;
    }

    public void new_script(final Context context, final Intent intent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final EditText title = new EditText(context);
        title.setText("untitled");
        title.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        title.setHint("script title");
        title.setSelectAllOnFocus(true);

        final EditText ext = new EditText(context);
        ext.setVisibility(View.GONE);
        ext.setHint("script extension");
        ext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);

        final Spinner list = type(context);
        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (list.getSelectedItem().toString().equals("something else")) {
                    ext.setVisibility(View.VISIBLE);
                    ext.setFocusable(true);
                } else ext.setVisibility(View.GONE);
                ext.setText(script_list.get(list.getSelectedItemPosition()).ext);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(title);
        layout.addView(ext);
        layout.addView(list);

        builder.setView(layout);
        builder.setMessage("write script name");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Open", null);
        final AlertDialog dialog = builder.create();
        dialog.show();

        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = title.getText().toString().trim() + ext.getText().toString().trim();
                String full_path = scripts_path + "/" + name;
                try {
                    File script = new File(full_path);
                    if (!script.exists()) {
                        script.createNewFile();
                    }
                    Intent editor_intent = intent;
                    editor_intent.putExtra("name", name);
                    editor_intent.putExtra("path", full_path);
                    dialog.dismiss();
                    context.startActivity(editor_intent);
                } catch (Exception ee) {
                    Toast.makeText(context, "invalid script name", Toast.LENGTH_LONG).show();
                    title.selectAll();
                    ee.printStackTrace();
                }
            }
        });

    }

    private Spinner type(Context context) {
        Spinner spinner = new Spinner(context);
        int n = script_list.size();
        String types[] = new String[n];
        for (int i = 0; i < n; i++) types[i] = script_list.get(i).type;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, types);
        spinner.setAdapter(adapter);
        return spinner;
    }

    private class baseAdapter extends BaseAdapter {

        private final Integer[] imgs = {R.drawable.terminal, R.drawable.editor, R.drawable.scripts, R.drawable.settings};
        //private final Integer[] imgs = {R.drawable.terminal2, R.drawable.editor2, R.drawable.scripts2, R.drawable.settings2};
        //private final Integer[] imgs = {R.drawable.terminal3, R.drawable.editor3, R.drawable.scripts3, R.drawable.settings3};

        private final String[] txts = {"Terminal", "Editor", "Scripts", "Settings"};

        private Bitmap[] bitmap = new Bitmap[imgs.length];
        private Context context;
        private LayoutInflater layoutInflater;

        baseAdapter(Context c) {
            context = c;
            layoutInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return bitmap.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return bitmap[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View grid;
            if (convertView == null) {
                grid = new View(context);
                grid = layoutInflater.inflate(R.layout.gridlayout, null);
            } else {
                grid = convertView;
            }

            ImageView imageView = grid.findViewById(R.id.image);
            Bitmap btmp = BitmapFactory.decodeResource(context.getResources(), imgs[position]);
            imageView.setImageBitmap(btmp);
            TextView textView = grid.findViewById(R.id.text);
            textView.setText(txts[position]);
            return grid;
        }

    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.
                        WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "You can store and retrieve scripts now :)", Toast.LENGTH_LONG).show();
            File dir = new File(scripts_path);
            if (!dir.exists()) dir.mkdirs();
        } else {
            Toast.makeText(getApplicationContext(), "You must enable the external storage permission" +
                    " to store and retrieve scripts", Toast.LENGTH_LONG).show();
            finish();
        }
    }

}
