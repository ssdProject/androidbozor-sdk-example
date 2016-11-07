# androidbozor-sdk-example

This library `androidbozorsdk-release.aar` consists of helper classes which provides
License validation (LicenseChecker) and In-app Purchase gateway (BillingProcessor)

# App Licensing

AndroidBozor offers a Licensing service that lets you enforce licensing for applications
that you publish on AndroidBoor as paid apps. With this service and provided library your 
application can query AndroidBozor at runtime to obtain license status for current device, 
than allow or disallow further use based license status.

# In-app Purchases

AndroidBozor provides In-app Billing service that lets you sell digital content from inside your applications. 
You can use the service to sell a virtual content such as game levels or points, premium services and features, and more. 
You can use In-app Billing to sell products as 

* Standard in-app products (one-time billing, all products are consumable)
* Subscriptions (recurring billing, subscriptions has not implemented yet on this version of the library)

AndroidBozor mobile application will handle all checkout process while purchasing application or in-app products. 

# Global Integration 
* Your project should build against Android 3.0 SDK (API level 11) at least.
* Add this `androidbozorsdk-release.aar` library to your project
    - Copy library into application module's `libs` folder of the projects
    - Update dependency of application module's `build.gradle`
```groovy
    dependencies {
       compile(name:'androidbozorsdk-release', ext:'aar')
    }
```
    - Update repository of project's `build.gradle`
```groovy
    allprojects {
            repositories {
                jcenter()
                flatDir{
                    dirs 'libs'
                }
            }
    }
```

## License Validator
Create instance of `LicenseChecker` class and implement callback in your Activity source code. Constructor will take 4 parameters:
- Context of current activity
- Your Base64 encoded License key from AndroidBozor Developer Console. This will be used to verify purchase signatures. 
- Encryption salt. This will be used to encrypt cached license response on device
- ILicenseHandler Interface implementation to handle validation results and errors
See `LicensingActivity` of the example application for more information.

```java
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
```

## In-app Purchases
Create instance of `BillingProcessor` class and implement callback in your activity source code. Constructor will take 4 parameters:
- Context of current activity
- Your Base64 encoded License key from AndroidBozor Developer Console. This will be used to verify purchase signatures. 
- Encryption salt. This will be used to encrypt cached license response on device
- IBillingHandler Interface implementation to handle purchase results and errors
See `BillingActivity` of the example application for more information.
```java
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
```