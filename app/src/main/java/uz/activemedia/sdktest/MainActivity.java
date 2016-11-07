package uz.activemedia.sdktest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onButtonClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnLicense:
                intent = new Intent(MainActivity.this, LicensingActivity.class);
                startActivity(intent);
                break;

            case R.id.btnBilling:
                intent = new Intent(MainActivity.this, BillingActivity.class);
                startActivity(intent);
                break;
        }
    }
}
