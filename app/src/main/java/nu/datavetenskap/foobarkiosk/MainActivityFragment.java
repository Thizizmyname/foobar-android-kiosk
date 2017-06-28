package nu.datavetenskap.foobarkiosk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import nu.datavetenskap.foobarkiosk.Settings.BuildConfig;
import nu.datavetenskap.foobarkiosk.models.Product;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    @Bind(R.id.btn_click) Button _btn;
    @Bind(R.id.text_view) TextView _text;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.bind(this, view);




        // Instantiate the Volley RequestQueue
        final RequestQueue queue = Volley.newRequestQueue(getContext());
        final String url = "http://10.0.2.2:8000/api/products/";

        _btn.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                setText(response);
                                //_text.setText("hej");
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("VolleyError", error.toString());
                            }
                        }) {


                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", BuildConfig.FOOBAR_API_TOKEN);
                        return headers;
                    }
                };
                queue.add(stringRequest);
            }
        });


        return view;
    }

    void setText(String str) {

        Gson gson = new Gson();
        Product[] products = gson.fromJson(str, Product[].class);
        String string = "Result: ";
        for (Product p : products) {
            string += p.getName() + ", ";
        }

        _text.setText(string);

    }



}
