package com.ma7moud3ly.aione_terminal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ScriptsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter myRecyclerAdapter;
    private Spinner script_type;
    private ArrayList<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scripts);
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        myRecyclerAdapter = new MyRecyclerAdapter(list);
        recyclerView.setAdapter(myRecyclerAdapter);
        script_type();
    }

    private void script_type() {
        script_type = findViewById(R.id.script_type);
        int n = MainActivity.script_list.size();
        ArrayList<String> types = new ArrayList<>();
        types.add("All Files");
        for (int i = 0; i < n - 1; i++) types.add(MainActivity.script_list.get(i).type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        script_type.setAdapter(adapter);

        script_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int item_pos = script_type.getSelectedItemPosition();
                String item = script_type.getSelectedItem().toString();
                setSettings(item_pos);
                String ext = "";
                if (item.equals("All Files")) ext = "*";
                else ext = MainActivity.script_list.get(item_pos - 1).ext;
                getFileList(ext);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        int item = getSettings();
        script_type.setSelection(item);
        getFileList(script_type.getSelectedItem().toString());
    }

    private void getFileList(String filter) {
        boolean all_files = filter.equals("*");
        list.clear();
        File f = new File(MainActivity.scripts_path);
        if (!f.exists()) {
            Toast.makeText(this, "there are no scripts", Toast.LENGTH_LONG).show();
            finish();
        }

        final String files[] = f.list();
        if (files == null || files.length == 0) {
            Toast.makeText(this, "there are no scripts", Toast.LENGTH_LONG).show();
            finish();
        } else {
            for (int i = 0; i < files.length; i++) {
                String name = files[i];
                if (!all_files && !name.endsWith(filter)) continue;
                list.add(name);
            }
        }
        myRecyclerAdapter.notifyDataSetChanged();
    }

    private void openFile(String name) {
        Intent editor_intent = new Intent(this, EditorActivity.class);
        editor_intent.putExtra("name", name);
        editor_intent.putExtra("path", MainActivity.scripts_path + "/" + name);
        Toast.makeText(getApplicationContext(), "open " + name, Toast.LENGTH_SHORT).show();
        startActivity(editor_intent);
    }

    private void renameScript(final String name) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        input.setText(name);
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        ad.setView(input);
        ad.setMessage("write new script name");
        ad.setNegativeButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String scriptName = input.getText().toString().trim();
                if (scriptName.isEmpty()) renameScript(name);
                else {
                    Scripts.rename(MainActivity.scripts_path + "/" + name, MainActivity.scripts_path + "/" + scriptName);
                    list.set(list.indexOf(name), scriptName);
                    myRecyclerAdapter.notifyDataSetChanged();
                }
            }
        });
        ad.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        ad.show();
    }

    private void deleteScript(String name) {
        Scripts.delete(MainActivity.scripts_path + "/" + name);
        list.remove(name);
        myRecyclerAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(), name + " deleted", Toast.LENGTH_SHORT).show();
    }

    private void runScript(String name) {
        String path = MainActivity.scripts_path + "/" + name;
        Editor ed = new Editor(path, name);
        ed.runScript(this);
    }

    private void shareScript(String name) {
        String path = MainActivity.scripts_path + "/" + name;
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        Uri uri = Uri.fromFile(new File(path));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("text/*");
        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.app_name)));
    }


    private class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder> {
        private ArrayList<String> list;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView file_name;
            public ImageButton rename_script, delete_script, run_script, share_script;

            public MyViewHolder(View view) {
                super(view);
                file_name = view.findViewById(R.id.file_name);
                run_script = view.findViewById(R.id.run_script);
                share_script = view.findViewById(R.id.share_script);
                delete_script = view.findViewById(R.id.delete_script);
                rename_script = view.findViewById(R.id.rename_script);
            }
        }

        public MyRecyclerAdapter(ArrayList<String> list) {
            this.list = list;
        }

        @Override
        public MyRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_item, parent, false);
            return new MyRecyclerAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyRecyclerAdapter.MyViewHolder holder, int position) {
            final String name = list.get(position);
            holder.file_name.setText(name);
            holder.file_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openFile(name);
                }
            });
            holder.rename_script.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    renameScript(name);
                }
            });
            holder.delete_script.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteScript(name);
                }
            });
            holder.run_script.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    runScript(name);
                }
            });
            holder.share_script.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shareScript(name);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

    }

    private int getSettings() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt("item_type", 0);
    }

    private void setSettings(int i) {
        SharedPreferences.Editor sharedPrefEditor = getPreferences(Context.MODE_PRIVATE).edit();
        sharedPrefEditor.putInt("item_type", i);
        sharedPrefEditor.commit();
    }


}
