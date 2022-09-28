package me.lebob.cryptoboxsealfortasker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import me.lebob.cryptoboxsealfortasker.utils.Constants;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.v(Constants.LOG_TAG, "MainActivity::onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}
