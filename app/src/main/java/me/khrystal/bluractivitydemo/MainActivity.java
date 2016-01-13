package me.khrystal.bluractivitydemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mButton;
    EditDigestWindow edWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button)findViewById(R.id.btn);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn:
                showMoreWindow(v);
                break;
        }
    }

    private void showMoreWindow(View view) {
        if (null == edWindow) {
            edWindow = new EditDigestWindow(this);
            edWindow.init();
        }
        edWindow.showMoreWindow(view,100);
    }
}
