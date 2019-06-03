package cordova.plugin.biometric;

import java.io.StringWriter;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;

import static cordova.plugin.biometric.CommonVar.PID_XML;
import static cordova.plugin.biometric.CommonVar.activityCallbackContext;

//  import io.ionic.starter.R;

public class MantraRDActivity extends Activity {

    private ArrayList < String > positions;

    static WebView webView;
    static Activity mainActivity;
    String deviceInfo;

    private static final String TAG = "MantraRDActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_mantra_rd);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        positions = new ArrayList < String > ();

        callDeviceInfo();

    }

    private void callDeviceInfo() {
        try {
            Intent intent = new Intent();
            intent.setAction("in.gov.uidai.rdservice.fp.INFO");
            startActivityForResult(intent, 1);
        } catch (Exception e) {
            Toast.makeText(MantraRDActivity.this, "RDService not found", Toast.LENGTH_SHORT).show();
            setFailureCB("RDService not found");
        }
    }

    private String getPIDOptions() {
        try {

            String pidVer = "2.0";
            String posh = "UNKNOWN";
            if (positions.size() > 0) {
                posh = positions.toString().replace("[", "").replace("]", "").replaceAll("[\\s+]", "");
            }

            Opts opts = new Opts();
            opts.fCount = "1";
            opts.fType = "0";
            opts.iCount = "0";
            opts.iType = "0";
            opts.pCount = "0";
            opts.pType = "0";
            opts.format = "0";
            opts.pidVer = "2.0";
            opts.timeout = "10000";
            //            opts.otp = "";
            opts.posh = posh;
            String env = "P";

            opts.env = env;

            PidOptions pidOptions = new PidOptions();
            pidOptions.ver = pidVer;
            pidOptions.Opts = opts;

            Serializer serializer = new Persister();
            StringWriter writer = new StringWriter();
            serializer.write(pidOptions, writer);
            //return writer.toString(); 
            return PID_XML;
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        if (data != null) {
                            String result = data.getStringExtra("DEVICE_INFO");
                            deviceInfo = result;
                            String rdService = data.getStringExtra("RD_SERVICE_INFO");
                            String display = "";
                            startMantra();
                        }
                    } catch (Exception e) {
                        Log.e("Error", "Error while deserialze device info", e);
                    }
                }
                break;
            case 2:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        if (data != null) {
                            String result = data.getStringExtra("PID_DATA");
                            JSONObject json = new JSONObject();
                            json.put("encodedImage", result);
                            json.put("deviceInfo", deviceInfo);

                            if (result != null) {
                                setSuccessCB(json);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Error", "Error while deserialize pid data", e);
                    }
                }
                break;
        }
    }

    void startMantra() {
        try {
            String pidOption = getPIDOptions();
            if (pidOption != null) {
                Intent intent2 = new Intent();
                intent2.setAction("in.gov.uidai.rdservice.fp.CAPTURE");
                intent2.putExtra("PID_OPTIONS", pidOption);
                startActivityForResult(intent2, 2);
            }
        } catch (Exception e) {
            Toast.makeText(MantraRDActivity.this, "RDService not found", Toast.LENGTH_SHORT).show();
            setFailureCB("RDService not found!!");
        }
    }

    private void setSuccessCB(final JSONObject resultJson) {
        Log.i(TAG, "handleSuccess");
        String jsonText = resultJson.toString();
        Log.d(TAG, jsonText);
        activityCallbackContext.success(jsonText);
    }

    private void setFailureCB(final String errorMsg) {
        Log.e(TAG, "method = handleError | error message = " + errorMsg);
        activityCallbackContext.error(errorMsg);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setFailureCB("Back pressed");

    }

    public static void setValues(WebView wv, Activity act) {
        webView = wv;
        mainActivity = act;
    }
}
