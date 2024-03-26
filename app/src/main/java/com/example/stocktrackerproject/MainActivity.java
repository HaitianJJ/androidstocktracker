package com.example.stocktrackerproject;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.animation.ObjectAnimator;
import android.graphics.Color;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    TextView stockPriceTextView, percentTextView, companyText;
    EditText symbol;
    Button updateButton;
    String symbolString;

    private RequestQueue requestQueue;
    double previousPrice = 0.0;
    String PROFILE_END_POINT= "stock/profile2?symbol=";
    String QUOTE_END_POINT = "quote?symbol=";
    private static final String BASE_URL = "https://finnhub.io/api/v1/";
    private static final String API_KEY = "cnec1phr01qmudhm7m1gcnec1phr01qmudhm7m20";   // API key here
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stockPriceTextView = findViewById(R.id.stockPriceTextView);
        percentTextView = findViewById(R.id.percentChangeTextView);
        companyText = findViewById(R.id.companyTextView);

        updateButton = findViewById(R.id.updateButton);
        symbol = findViewById(R.id.Ticker);
        requestQueue = Volley.newRequestQueue(this);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                symbolString = symbol.getText().toString();
                if(!symbolString.isEmpty())
                {
                    fetchStockPrice();
                    fetchStockName();
                }
                else {
                    Toast.makeText(MainActivity.this, "Please enter a valid symbol", Toast.LENGTH_SHORT).show();
                }
            }
        });

        timer = new Timer();
        symbolString = symbol.getText().toString();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(!symbolString.isEmpty())
                {
                    fetchStockPrice();
                }
            }
        }, 0, 2*1000);

    }

    //https://finnhub.io/api/v1/quote?symbol=AAPL&token=cnec1phr01qmudhm7m1gcnec1phr01qmudhm7m20
    private void fetchStockPrice() {
        String symbolString = symbol.getText().toString();
        String QuoteApiUrl = BASE_URL+ QUOTE_END_POINT + symbolString + "&token=" +API_KEY;
        String ProfileApiUrl = BASE_URL+ PROFILE_END_POINT + symbolString + "&token=" +API_KEY;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, QuoteApiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String stockPrice = response.getString("c");
                            String percentChange = response.getString("dp");


                            double stockPriceValue = Double.parseDouble(stockPrice);
                            updateStockData(stockPriceValue, "Percent Change: " + percentChange);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("ERROR", "Error parsing JSON: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error.networkResponse!=null&& error.networkResponse.data!=null)
                        {
                            String responeBody = new String(error.networkResponse.data);
                            Log.e("API_ERROR", "Error Response: " + responeBody);
                        }
                        Log.e("API_ERROR", "Error fetching stock price: " + error.getMessage());
                    }
                });
        requestQueue.add(request);

    }

    private void fetchStockName() {
        String symbolString = symbol.getText().toString();
        String ProfileApiUrl = BASE_URL + PROFILE_END_POINT + symbolString + "&token=" +API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ProfileApiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String companyText = response.getString("name");
                            updateStockName("Company: " + companyText);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("ERROR", "Error parsing JSON: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error.networkResponse!=null&& error.networkResponse.data!=null)
                        {
                            String responeBody = new String(error.networkResponse.data);
                            Log.e("API_ERROR", "Error Response: " + responeBody);
                        }
                        Log.e("API_ERROR", "Error fetching stock price: " + error.getMessage());
                    }
                });
        requestQueue.add(request);

    }


    private void updateStockData(double currentPrice, String exchange) {
        double instantaneousChange = currentPrice - previousPrice;
        previousPrice = currentPrice;
        handler.post(new Runnable() {
            @Override
            public void run() {
                startBlinkAnimation(stockPriceTextView);
                String formattedPrice = String.format("%2f", currentPrice);
                stockPriceTextView.setText(formattedPrice);
                int textColor = (instantaneousChange>=0.00) ? Color.GREEN : Color.RED;
                stockPriceTextView.setTextColor(textColor);
                percentTextView.setText(exchange);

            }
        });

    }
    private void updateStockName(String company) {
        handler.post(new Runnable() {
            @Override
            public void run() {

                companyText.setText(company);

            }
        });

    }


    private void startBlinkAnimation(final TextView textView)
    {
        ObjectAnimator blink = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f, 1f);
        blink.setDuration(200);
        blink.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timer!=null)
        {
            timer.cancel();
            timer.purge();
        }
    }



}
