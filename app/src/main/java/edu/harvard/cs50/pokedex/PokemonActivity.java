package edu.harvard.cs50.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;

public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private String url;
    private RequestQueue requestQueue;
    private ImageView imageView;
    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    public String imageP;
    private TextView info;
    private static int pokeID = 0;
    private Button BB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        info = findViewById(R.id.infoTextView);
        BB = findViewById(R.id.button);

        imageView = findViewById(R.id.pokeimage);

        load();
       // new DownloadSpriteTask().execute(new PokemonActivity().imageP); // you need to get the url!
    }

    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");



        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    /////////////////////////////////////////////////////////////////////////////////////////////////
                    pokeID = response.getInt("id");
                    Log.d(LOG_TAG, " int is :" + pokeID);
                    String myurl = "https://pokeapi.co/api/v2/pokemon-species/"+ pokeID + "/";
                    Log.d(LOG_TAG, " url changed to:" + myurl);


                    final JsonObjectRequest request2 = new JsonObjectRequest(Request.Method.GET, myurl, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray infoEntries = response.getJSONArray("flavor_text_entries");
                                for(int i = 1; i < infoEntries.length(); i++){
                                    JSONObject entry = infoEntries.getJSONObject(i); // [0]
                                    String lang = entry.getJSONObject("language").getString("name");
                                    if(lang.equals("en")){
                                        String pokeinfo = entry.getString("flavor_text");
                                        Log.d(LOG_TAG, " first is " + pokeinfo);
                                        pokeinfo = pokeinfo.replaceAll("[\\n\\t]", " ");
                                        info.setText(pokeinfo);
                                        Log.d(LOG_TAG, " final is " + pokeinfo);
                                        break;
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e("cs50", "Pokemon info error", e);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("cs50", "Pokemon information details error", error);
                        }
                    });

                    requestQueue.add(request2);

                    ///////////////////////////////////////////////////////////////////////////////////////////////

                    nameTextView.setText(response.getString("name"));
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));

                    //image loaded
                    JSONObject obj = response.getJSONObject("sprites");
                    imageP = obj.getString("front_default");
                    Log.v(LOG_TAG, " text changed to:" + imageP);
                    new DownloadSpriteTask().execute(imageP);

                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        } else if (slot == 2) {
                            type2TextView.setText(type);
                        }

                        String N = response.getString("name");
                        String save = getPreferences(Context.MODE_PRIVATE).getString(N, "Catch");
                        BB.setText(save);
                        if(save.equals("Catch"))
                            BB.setBackgroundColor(Color.GREEN);
                        else
                            BB.setBackgroundColor(Color.RED);

                    }
                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });


        //request 2


        requestQueue.add(request);


    }
    /////

    public void toggleCatch(View view) {
        // gotta catch 'em all
        Button Cbutton = findViewById(R.id.button);
        String name = (String) nameTextView.getText();
        Log.d(LOG_TAG, " name is to:" + name);

        String r = "Release";
        String c = "Catch";
        String e = (String) Cbutton.getText();
        if (e.equals(r)) {
            getPreferences(Context.MODE_PRIVATE).edit().putString(name, "Catch").commit();
            Cbutton.setText("Catch");
            Cbutton.setBackgroundColor(Color.GREEN);
            //Cbutton.setBackgroundColor(Color.GREEN);
        } else if (e.equals(c)) {
            getPreferences(Context.MODE_PRIVATE).edit().putString(name, "Release").commit();
            //Cbutton.setBackgroundColor(Color.RED);
            Cbutton.setText("Release");
            Cbutton.setBackgroundColor(Color.RED);
        }
    }

    ///loading image class
    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // load the bitmap into the ImageView!
            imageView.setImageBitmap(bitmap);
        }
    }

}//final




