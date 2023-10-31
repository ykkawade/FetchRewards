package com.example.fetch;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity {
    String url = "https://fetch-hiring.s3.amazonaws.com/hiring.json";
    JSONArray jsonArray;
    Button fetchDataButton;
    TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchDataButton = findViewById(R.id.fetchDataButton);
        tableLayout = findViewById(R.id.table);

        fetchDataButton.setOnClickListener(view -> {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {

                try {
                    jsonArray = FetchData.fetchJsonFromUrl(url);
                } catch (JSONException | IOException e) {
                    throw new RuntimeException(e);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Map<Integer, List<Item>> items = new HashMap<>();
                        if (jsonArray.length() != 0) {
                            try {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = (JSONObject) jsonArray.get(i);
                                    if (!object.getString("name").isEmpty() && !object.getString("name").equals("null")) {
                                        Item item = new Item(object.getInt("id"), object.getInt("listId"), object.getString("name"));
                                        int listId = item.getListId();
                                        items.computeIfAbsent(listId, new Function<Integer, List<Item>>() {
                                            @Override
                                            public List<Item> apply(Integer k) {
                                                return new ArrayList<>();
                                            }
                                        }).add(item);
                                    }
                                }

                                for (List<Item> itemList : items.values()) {
                                    itemList.sort(Comparator.comparing(Item::getName, (name1, name2) -> {
                                        int intVal1 = extractIntegerValue(name1);
                                        int intVal2 = extractIntegerValue(name2);
                                        return Integer.compare(intVal1, intVal2);
                                    }));
                                }

                                createTableHeaders();
                                items.entrySet()
                                        .stream()
                                        .sorted(Map.Entry.comparingByKey())
                                        .forEach(entry -> {
                                            int listId = entry.getKey();
                                            List<Item> itemList = entry.getValue();

                                            for (Item item : itemList) {
                                                createTableRows(listId, item.getId(), item.getName());
                                            }
                                        });
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }

                    private void createTableRows(int listId, int id, String name) {
                        TableRow row = new TableRow(MainActivity.this);
                        GradientDrawable gd = new GradientDrawable();
                        gd.setColor(Color.parseColor("#E8F5E9")); // Changes this drawbale to use a single color instead of a gradient
                        gd.setStroke(3, 0xFF000000);

                        TextView tv0 = new TextView(MainActivity.this);
                        tv0.setText(String.valueOf(listId));
                        tv0.setTextColor(Color.BLACK);
                        tv0.setGravity(Gravity.CENTER);
                        tv0.setTextSize(30);
                        tv0.setBackground(gd);
                        row.addView(tv0);

                        TextView tv1 = new TextView(MainActivity.this);
                        tv1.setText(String.valueOf(id));
                        tv1.setTextColor(Color.BLACK);
                        tv1.setGravity(Gravity.CENTER);
                        tv1.setTextSize(30);
                        tv1.setBackground(gd);
                        row.addView(tv1);

                        TextView tv2 = new TextView(MainActivity.this);
                        tv2.setText(name);
                        tv2.setTextColor(Color.BLACK);
                        tv2.setGravity(Gravity.CENTER);
                        tv2.setTextSize(30);
                        tv2.setBackground(gd);
                        row.addView(tv2);

                        tableLayout.addView(row);
                    }

                    private void createTableHeaders() {

                        TableRow row0 = new TableRow(MainActivity.this);
                        GradientDrawable gd = new GradientDrawable();
                        gd.setColor(Color.parseColor("#81C784")); // Changes this drawbale to use a single color instead of a gradient
                        gd.setStroke(3, 0xFF000000);

                        TextView tv0 = new TextView(MainActivity.this);
                        tv0.setText(R.string.itemId);
                        tv0.setTextColor(Color.BLACK);
                        tv0.setGravity(Gravity.CENTER);
                        tv0.setTextSize(30);
                        tv0.setBackground(gd);
                        row0.addView(tv0);

                        TextView tv1 = new TextView(MainActivity.this);
                        tv1.setText(R.string.id);
                        tv1.setTextColor(Color.BLACK);
                        tv1.setGravity(Gravity.CENTER);
                        tv1.setBackgroundColor(Color.LTGRAY);
                        tv1.setTextSize(30);
                        tv1.setBackground(gd);
                        row0.addView(tv1);

                        TextView tv2 = new TextView(MainActivity.this);
                        tv2.setText(R.string.name);
                        tv2.setTextColor(Color.BLACK);
                        tv2.setGravity(Gravity.CENTER);
                        tv2.setTextSize(30);
                        tv2.setBackgroundColor(Color.LTGRAY);
                        tv2.setBackground(gd);
                        row0.addView(tv2);

                        tableLayout.addView(row0);
                    }

                    private int extractIntegerValue(String str) {
                        String[] parts = str.split(" ");
                        for (String part : parts) {
                            try {
                                return Integer.parseInt(part);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        return 0;
                    }
                });
            });
            fetchDataButton.setEnabled(false);
        });
    }
}