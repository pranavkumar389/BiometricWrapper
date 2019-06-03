package cordova.plugin.biometric;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.cordova.CallbackContext;

import org.json.JSONException;
import org.json.JSONObject;

import com.sec.biometric.license.SecBiometricLicenseManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import static cordova.plugin.biometric.CommonVar.PID_XML;
import static cordova.plugin.biometric.CommonVar.activityCallbackContext;

public class IRISActivity extends Activity {

    private static String LICENSE_KEY = "C25BF4FFBEEB2EB67A93009B676ECD7ACE9EC0C744C3D28E00CF7D8F30BBDD3A1637EE3CD773E45018D4D12FC680816871D118F8C236D1A6BBCDF34A403F3EDC";

    Context mContext;
    SecBiometricLicenseManager mLicenseMgr;
    KeyStore p12;
    AutoCompleteTextView uidTxtVu, demoTxtVu;
    TextView uidTitle;
    X509Certificate uidaiCert;
    Button AuthBtn;
    public boolean isCapturinginProgress = false;
    TextView eyeuiInfo;
    Properties uidPrefs;
    int uidOp = 0;
    public final int UID_OP_AUTH = 1;
    private static final String TAG = "IRISActivity";

    private String dname = "";
    private Date certExpiryDate;

    static WebView webView;
    static Activity mainActivity;

    int NUM_EYE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Toast.makeText(IRISActivity.this, "Initialising...", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        mLicenseMgr = SecBiometricLicenseManager.getInstance(mContext);

        if (networkConnected())
            activateIrisLicense();
        else
            handleFailure("Device is not connected to any network.", "E001");
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(SecBiometricLicenseManager.ACTION_LICENSE_STATUS)) {
                unregisterReceiver(mReceiver);

                Bundle extras = intent.getExtras();
                String status = extras.getString(SecBiometricLicenseManager.EXTRA_LICENSE_STATUS);

                if ("success".equals(status)) {
                    Toast.makeText(IRISActivity.this, "Start capturing", Toast.LENGTH_SHORT).show();
                    capture();
                } else {
                    int err_code = extras.getInt(SecBiometricLicenseManager.EXTRA_LICENSE_ERROR_CODE);
                    Log.e(TAG, "License Status FAILED, ERROR CODE : " + err_code);
                    handleFailure("License not activated, please activate license.", "E002");
                }
            }
        }
    };

    protected Boolean activateIrisLicense() {
        try {
            Toast.makeText(IRISActivity.this, "Hold down! Authenticating device.", Toast.LENGTH_LONG).show();

            IntentFilter filter = new IntentFilter();
            filter.addAction(SecBiometricLicenseManager.ACTION_LICENSE_STATUS);
            filter.addAction(SecBiometricLicenseManager.ACTION_LICENSE_STATUS);
            registerReceiver(mReceiver, filter);

            String key = LICENSE_KEY; // need to fill a valid key
            String packageName = getApplicationContext().getPackageName();
            mLicenseMgr.activateLicense(key, packageName);
            return true;
        } catch (Exception e) {

            handleFailure("Error in activating license :  " + e.toString(), "E003");
        }
        return false;
    }

    private void capture() {
        uidOp = UID_OP_AUTH;
        // Get the package manager
        PackageManager packageManager = getPackageManager();
        // Get activities that can handle the intent

        Intent act = new Intent("in.gov.uidai.rdservice.iris.CAPTURE");
        List < ResolveInfo > activities = packageManager.queryIntentActivities(act, 0);
        for (ResolveInfo activity: activities) {
            // Log.d(TAG, "setOnClickListener: "+activity.activityInfo.packageName+" :
            // "+activity.activityInfo.name);
        }
        // String pkgName = activities.get(0).activityInfo.packageName; //MAKE SURE YOU
        // HAVE SLECTED SAMSUNG RD SERVICE
        // String activity = activities.get(0).activityInfo.name;
        act.setClassName("com.sec.indiaidentity", "com.sec.indiaidentity.SamsungRDActivity");
        // Log.d(TAG, "setOnClickListener "+pkgName+" : "+activity);
        // act.setClassName(getApplicationContext(),"IRISActivity");
        act.putExtra("PID_OPTIONS", getInputData());
        try {
            if (networkConnected())
                startActivityForResult(act, 1);
            else
                handleFailure("device is not connected to any network", "E004");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private boolean networkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private String getInputData() {

        Log.d(TAG, "startCaptureRegistered " + PID_XML);

        String inputxml = PID_XML;

        return inputxml;
    }

    private Properties loadPreferences(String prefsFile) {
        InputStream is = null;
        Properties prefs = null;
        try {
            is = getApplicationContext().getAssets().open(prefsFile);
            if (is != null) {
                prefs = new Properties();
                prefs.load(is);
            }
        } catch (IOException ex) {
            Log.d(this.toString(), "Error accessing preferences file");
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                Log.d(this.toString(), "Error closing preferences file");
            }
        }
        return prefs;
    }

    private String getcertIdentifier() {
        String certificateIdentifier = null;
        try {
            InputStream inputStrm = null;
            inputStrm = getApplicationContext().getAssets().open("iris/" + uidPrefs.getProperty("publicKeyFile"));
            CertificateFactory certFactory = CertificateFactory.getInstance("X509");
            uidaiCert = (X509Certificate) certFactory.generateCertificate(inputStrm);
            certExpiryDate = uidaiCert.getNotAfter();
            SimpleDateFormat ciDateFormat = new SimpleDateFormat("yyyyMMdd");
            ciDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            certificateIdentifier = ciDateFormat.format(this.certExpiryDate);
        } catch (Exception e) {
            Log.e(TAG, "Exception caught in getcertIdentifier method = ", e);
        }
        return certificateIdentifier;
    }

    private boolean hasPermission() {
        try {
            String BIOMETRIC_LICENSE_PERMISSION = "com.sec.enterprise.biometric.permission.IRIS_RECOGNITION";
            PackageManager packageManager = this.getApplicationContext().getPackageManager();
            if (packageManager.checkPermission(BIOMETRIC_LICENSE_PERMISSION,
                    this.getApplicationContext().getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Has permission! You are the boss :) ");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception caught in hasPermission method = " + e);
        }
        Log.d(TAG, "Don't have permission :( ");
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode : " + requestCode + " / resultCode : " + resultCode +
            " Activity.RESULT_OK:" + Activity.RESULT_OK);

        switch (requestCode) {
            case (1):
                {
                    if (resultCode == Activity.RESULT_OK) {
                        final String piddata = data.getStringExtra("PID_DATA");
                        // Log.d(TAG, "capture data:" + piddata);
                        if (piddata.contains("Resp errCode=\"0\" errInfo=\"No error\"")) {
                            Toast.makeText(IRISActivity.this, "Captured successfully! Go back", Toast.LENGTH_LONG).show();
                            handleSuccess(piddata);
                        } else {
                            handleFailure(piddata, "E005");
                        }
                    }
                    break;
                }
            case (999):
                {
                    if (!hasPermission())
                        handleFailure("No Permission", "E006");
                }
        }
    }

    public static void setValues(Activity activity, WebView wv) {
        mainActivity = activity;
        webView = wv;
    }

    private void handleSuccess(final String str) {
        Log.i(TAG, "handleSuccess");
        activityCallbackContext.success(str);
    }

    private void handleFailure(final String errorMsg, final String errorCode) {
        Log.e(TAG, "method = handleError | error message = " + errorMsg + " | error code = " + errorCode);
        activityCallbackContext.error("OOPS!! Something went wrong.");
    }

}
