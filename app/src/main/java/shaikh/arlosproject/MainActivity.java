package shaikh.arlosproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    public static String MY_PREFS = "MY_PREFS";
    private SharedPreferences mySharedPreferences;
    int prefMode = Activity.MODE_PRIVATE;
    EditText Username, password;
    LinearLayout newser;
    Button login;
    boolean doubleBackToExitPressedOnce = false;
    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS = 1;
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        MainActivity.this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        setContentView(R.layout.activity_main);


        Username = (EditText) findViewById(R.id.email);
        password= (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.btnlogin);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              String  UserName= Username.getText().toString();
               String Password = password.getText().toString();

                new UserLoginTask().execute(UserName, Password);
            }
        });

    }

    public class UserLoginTask extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }


        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                //url = new URL("http://192.168.2.2:80/pro/login.inc.php");
                url = new URL("http://ishook.com/users/login/login_json");
                //url = new URL("http://www.sd-constructions.com/bhushan/login.inc.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        // .appendQueryParameter("Email", params[0])
                        .appendQueryParameter("UserName", params[0])
                        .appendQueryParameter("Password", params[1]);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return(result.toString());

                }else{

                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            pdLoading.dismiss();
            String loginstatus = null;
            String session_id = null;
            String Profile = null;
            String login_error_msg = null;
            String list_Post = null;
            String Stats = null;
            String medialist = null;
            String Uid;


            if (result != "exception") {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    loginstatus = jsonObject.getString("is_login");
                    session_id = jsonObject.getString("session_id");
                    Log.d("impssh",session_id);
                    Profile = jsonObject.getString("user_profile");
                    JSONObject object = new JSONObject(Profile);
                    Uid = object.getString("user_id");

                    SharedPreferences sharedPreferences = getSharedPreferences(getPackageName() + Constants.PREF_FILE_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();


                    editor.putString(Constants.KEY_SESSION, session_id);
                    editor.putString(Constants.KEY_USERID, Uid);


                    editor.apply();


                    if (loginstatus.equals("true")) {

                        //Toast.makeText(getApplicationContext(), session_id, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), UserActivity.class));

                    } else {
                        Toast.makeText(getApplicationContext(), "Username and Password did not match", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Username and Password did not match", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }else {                Toast.makeText(getApplicationContext(), "Check your Connection!", Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        protected void onCancelled() {

        }
    }


}
