package com.zpi.komunikator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ContentFrameLayout;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.AsyncTask;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private EditText editText;
    private MyAdapter myAdapter;
    private RecyclerView recyclerView;
    private List<MyMessage> messages;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editText.getText().toString();
                if (message.length() > 0) {
                    editText.setText("");
                    hideKeyboard();
                    new SendMessageTask().execute(message);
                    Log.d("TAG", "executed");
                }
            }
        });

        messages = new ArrayList<>();
        myAdapter = new MyAdapter(messages);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new DownloadMessagesTask().execute();
                    }
                });
            }
        }, 0, 10000);
    }


    public int convertDipToPixels(float dips) {
        return (int) (dips * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        timer = null;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void bindViews() {
        button = findViewById(R.id.button);
        editText = findViewById(R.id.edit_text);
        recyclerView = findViewById(R.id.recycler);
    }

    private class DownloadMessagesTask extends AsyncTask<Void, Void, List<MyMessage>> {

        @Override
        protected List<MyMessage> doInBackground(Void... voids) {
            URL service = null;
            try {
                service = new URL("http://e-biuro.net/android10/messages/");
                HttpURLConnection connection = (HttpURLConnection)
                        service.openConnection();
                connection.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8")
                );
                String json = reader.readLine();
                JSONObject jObject = new JSONObject(json);

                String availableItems = jObject.getString("AvailableItems");
                JSONArray messages = jObject.getJSONArray("Messages");

                List<MyMessage> messageList = new ArrayList<>();
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject o = (JSONObject) messages.get(i);
                    MyMessage m = new MyMessage(o.getInt("id"), o.getString("message"));
                    messageList.add(m);
                }
                return messageList;
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MyMessage> myMessages) {
            if (myMessages != null){

                for (MyMessage m : myMessages) {
                    if (!messages.contains(m)) {
                        messages.add(m);
                    }
                }
                myAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        }

    }

    private static class SendMessageTask extends AsyncTask<String, Void, Boolean> {


        @Override
        protected Boolean doInBackground(String... strings) {
            boolean success = true;

            String message = strings[0];

            URL service = null;
            try {
                String encodedMessage = URLEncoder.encode(message, "utf-8");
                service = new URL("http://e-biuro.net/android10/messages/" + encodedMessage);
                HttpURLConnection connection = (HttpURLConnection)
                        service.openConnection();
                connection.setRequestMethod("PUT");
                connection.getInputStream();

            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }

            return success;
        }

    }

    class MyMessage {
        public String message;
        public int id;

        MyMessage(int id, String message) {
            this.id = id;
            this.message = message;
        }

        @Override
        public String toString() {
            return "[" + id + " | " + message + "]";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MyMessage) {
                MyMessage other = (MyMessage) obj;
                return id == other.id;
            } else return false;
        }
    }
}
