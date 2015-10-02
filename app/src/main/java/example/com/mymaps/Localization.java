package example.com.mymaps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Localization extends ActionBarActivity implements View.OnClickListener {

    public static Context context;
    private static final String TAG = "LOCALIZATION";
    private static final int ACTIVITY_EXTRACT_LOCATION=1;
    private String serverIPString;
    private EditText ipEditText;
    private String serverPortString;
    private EditText portEditText;

    private Button buttonConnect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.localization);
        Localization.context=getApplicationContext();
        buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(this);

        ipEditText = (EditText) findViewById(R.id.serverIP);
        portEditText = (EditText) findViewById(R.id.serverPort);


    }
    public void onClick(View view)
    {
        if (view.getId() == R.id.buttonConnect) {
          //  Toast.makeText(getApplicationContext(), "Button was clicked", Toast.LENGTH_LONG).show();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();


            serverIPString = ipEditText.getText().toString();
            editor.putString("serverIP", serverIPString);
            serverPortString = portEditText.getText().toString();
            editor.putString("serverPort", serverPortString);
            editor.commit();

            Intent i = new Intent(this, ExtractLocation.class);
            startActivityForResult(i, ACTIVITY_EXTRACT_LOCATION);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        //   Bundle extras = intent.getExtras();
        switch(requestCode)
        {
            case ACTIVITY_EXTRACT_LOCATION:

                Log.d("TAG", "Return from ExtractLocation");
                break;
        }
    }


        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
