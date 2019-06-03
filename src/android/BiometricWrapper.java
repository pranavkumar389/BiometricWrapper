/**
 * @author CE032
 *
 */
 
package cordova.plugin.biometric;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static cordova.plugin.biometric.CommonVar.PID_XML;
import static cordova.plugin.biometric.CommonVar.activityCallbackContext;


/**
 * This class echoes a string called from JavaScript.
 */
public class BiometricWrapper extends CordovaPlugin {

    public static final String TAG = "BiometricWrapper";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            PID_XML = args.getJSONObject(0).getString("PID_XML");
            activityCallbackContext = callbackContext;
        } catch (JSONException exp) {
            // Log.e(TAG, exp);
            callbackContext.error("OOPS! Something went wrong.");
        }

        if ("activateIris".equals(action)) {

            this.activateIris();
            return true;

        } else if ("activateFingerprint".equals(action)) {

            this.activateFingerprint();
            return true;
        }
        return false;
    }

    private void activateIris() {

        Context context = cordova.getActivity().getApplicationContext();
        Intent intent = new Intent(context, IRISActivity.class);

        cordova.startActivityForResult(this, intent, 0);
    }

    private void activateFingerprint() {

        Context context = cordova.getActivity().getApplicationContext();
        Intent intent = new Intent(context, MantraRDActivity.class);

        cordova.startActivityForResult(this, intent, 0);
    }
}