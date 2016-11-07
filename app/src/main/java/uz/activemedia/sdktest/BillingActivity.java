/**
 * Copyright 2014 AnjLab
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uz.activemedia.sdktest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import uz.ssd.androidbozor.services.billing.v1.BillingProcessor;
import uz.ssd.androidbozor.services.billing.v1.IBillingHandler;
import uz.ssd.androidbozor.services.billing.v1.SkuDetails;
import uz.ssd.androidbozor.services.billing.v1.TransactionDetails;


public class BillingActivity extends Activity {
    // SAMPLE APP CONSTANTS
    private static final String LOG_TAG         = "billing";

    // PRODUCT IDS
    private static final String PRODUCT_ID      = "product_500_coin";

    /**
     * BASE64 encoded public key for the application which is generated at Developer Console.
     * This is used peer-to-peer data validation
     */
    private static final String BASE64_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDO1bmjYUv9aso2oYEyWaDaJugmAd825/FOyJ71kXSFgxSo12LB3OiqMFfjsQvDbxyYlaQlCqhpM42QQBWJrp3uRfelb4PaZ5sfVxPipkiM+4B8gYKYeC44JYW8HBEtFswBpzHMVmOzJ9FE55utYg6cCkhjz9ZKx1bmOjUxcOpHLQIDAQAB";

    /**
     * Purchase responses will be encrypted and cached on device, change encryption salt
     */
    private static final byte[] ENCRYPT_SALT = new byte[]{-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64, 89};

    private boolean readyToPurchase = false;
    private BillingProcessor bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        bp = new BillingProcessor(this, BASE64_PUBLIC_KEY, ENCRYPT_SALT, new BillingHandler());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTextViews();
    }

    @Override
    public void onDestroy() {
        /**
         * Always call bp.release() to release resources owned by BillingProcessor
         */
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * Always call handleActivityResult to handle activity results which is started by BillingProcessor
         */
        if (bp != null) {
            bp.handleActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateTextViews() {
        TextView text = (TextView) findViewById(R.id.productIdTextView);
        text.setText(String.format("%s is%s purchased", PRODUCT_ID, bp.isPurchased(PRODUCT_ID) ? "" : " not"));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void onClick(View v) {
        if (!readyToPurchase) {
            showToast("Billing not initialized.");
            return;
        }
        switch (v.getId()) {
            case R.id.purchaseButton:

                /**
                 * Observe onProductPurchased of IBillingHandler object.
                 */
                bp.purchase(this, PRODUCT_ID);
                break;
            case R.id.productDetailsButton:

                /**
                 * Observe onSkuDetailsAccepted of IBillingHandler object.
                 */
                bp.getPurchaseListingDetails(PRODUCT_ID);
                break;
            case R.id.consumeButton:

                /**
                 * Observe onProductConsumed of IBillingHandler object.
                 */
                bp.consumePurchase(PRODUCT_ID);
            default:
                break;
        }
    }

    private class BillingHandler implements IBillingHandler{
        /**
         * BillingProcessor connected to billing service and ready to handle purchase requests,
         */
        @Override
        public void onBillingInitialized() {
            showToast("onBillingInitialized");
            readyToPurchase = true;
            updateTextViews();
        }

        /**
         * BillingProcessor failed to connect/execute.
         * @param errorCode Error code
         *                  1 - Purchase process cancelled by user
         *                  2 - Billing server unavailable
         *                  3 - Billing service unavailable
         *                  4 - Item unavailable
         *                  6 - Fatal error while communicating with API
         *                  7 - Item already owned
         *                  8 - Item not owned
         *                  100 - Failed while loading owned purchases
         *                  101 - Failed communicating with billing service
         *                  102 - Invalid signature
         *                  110 - Other errors
         *                  113 - AndroidBozor service not installed
         * @param error
         */
        @Override
        public void onBillingError(int errorCode, Throwable error) {
            showToast("onBillingError: " + Integer.toString(errorCode));
        }


        /**
         * Triggered after successfully purchase of product, started by bp.purchase(this, productId)
         * @param productId - Product SKU
         * @param details - TransactionDetails Object
         */
        @Override
        public void onProductPurchased(String productId, TransactionDetails details) {
            showToast("onProductPurchased: " + productId);
            updateTextViews();

            if (productId.equals(PRODUCT_ID)) {
                //your logic
            }
        }

        /**
         * Triggered after purchase history restored. All information about owned products are
         * retrieved and stored locally
         */
        @Override
        public void onPurchaseHistoryRestored() {
            showToast("onPurchaseHistoryRestored");
            for (String sku : bp.listOwnedProducts())
                Log.d(LOG_TAG, "Owned Managed Product: " + sku);
            updateTextViews();
        }

        /**
         * Triggered after bp.consume(productId) performed.
         */

        @Override
        public void onProductConsumed(boolean isSuccess, String productId) {
            if (isSuccess) {
                showToast(productId + " consumed successfully");
            } else {
                showToast(productId + " could not be consumed");
            }
            updateTextViews();

            if (productId.equals(PRODUCT_ID)) {
                //put your logic
            }
        }


        /**
         * Triggered after bp.getPurchaseListingDetails(productId) performed.
         * @param skuDetails  List of SkuDetails
         * @param billingErrorCode Billing response code
         *                         0 - OK
         *                         2 - Service unavailable
         *                         6 - Error
         */
        @Override
        public void onSkuDetailsAccepted(List<SkuDetails> skuDetails, int billingErrorCode) {
            for (SkuDetails d : skuDetails) {
                showToast(d.toString());
            }
        }
    }
}
