package uz.activemedia.sdktest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import uz.ssd.androidbozor.services.licensing.v1.ILicenseHandler;
import uz.ssd.androidbozor.services.licensing.v1.LicenseChecker;


public class LicensingActivity extends AppCompatActivity {

    /**
     * BASE64 encoded public key for the application which is generated at Developer Console.
     * This is used peer-to-peer data validation
     */
    private static final String BASE64_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDO1bmjYUv9aso2oYEyWaDaJugmAd825/FOyJ71kXSFgxSo12LB3OiqMFfjsQvDbxyYlaQlCqhpM42QQBWJrp3uRfelb4PaZ5sfVxPipkiM+4B8gYKYeC44JYW8HBEtFswBpzHMVmOzJ9FE55utYg6cCkhjz9ZKx1bmOjUxcOpHLQIDAQAB";

    /**
     * License validation results will be encrypted and cached on device, change encryption salt
     */
    private static final byte[] ENCRYPT_SALT = new byte[]{-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64, 89};

    private LicenseChecker mChecker;
    private TextView       textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_license);

        textView = (TextView) findViewById(R.id.textView);

        Button mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChecker.validateLicense();
            }
        });

        mChecker = new LicenseChecker(this, BASE64_PUBLIC_KEY, ENCRYPT_SALT, new LicenseHandler());
        mChecker.validateLicense();
    }

    protected void onDestroy() {
        super.onDestroy();

        /**
         * Always call onDestroy to close IPC connections and stop threads in LicenseChecker
         */
        mChecker.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /**
         * Always call handleActivityResult to handle activity results which is started by LicenseChecker
         */

        if (!mChecker.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private class LicenseHandler implements ILicenseHandler {

        /**
         * @param errorCode Error code accepted from licensing service.
         *                  1 - Licensed App
         *                  3 - Retry License check (if some connection or service error occurs,
         *                  app considered as licensed temporarily )
         */
        @Override
        public void onAllowedLicense(int errorCode) {
            textView.setText("Allowed  " + errorCode);
        }

        /**
         * @param errorCode  Error code accepted from licensing service.
         *                   2 - Not licensed app (not licensed or invalid response from service)
         *                   3 - Retry license check
         * @param canUseDemo Flag shows app can be used for demo period
         * @return true - Dialogs will be handled by LicenseChecker
         * false - You should handle error response
         */
        @Override
        public boolean onDisallowedLicense(int errorCode, boolean canUseDemo) {
            textView.setText("Disallowed " + errorCode);
            return true;
        }

        /**
         * @param errorCode Error code accepted from licensing service.
         *                  101 - Error contacting the license server
         *                  102 - Invalid package name
         *                  104 - Application is not found in AndroidBozor
         *                  105 - License server failure
         *                  108 - User is not authorized in AndroidBozor service
         *                  120 - AndroidBozor application is not installed on device
         * @return true - Error response will be handled by LicenseChecker
         * false - You should handle error response
         */
        @Override
        public boolean onLicenseCheckError(int errorCode) {
            textView.setText("Error " + errorCode);
            return true;
        }

        /**
         * License is allowed for demo period
         */
        @Override
        public void onAllowedDemo() {
            textView.setText("Demo Allowed");
        }

        @Override
        public Activity getActivity() {
            return LicensingActivity.this;
        }
    }
}
