package cordova.locationkit;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import socialradar.locationkit.ILocationKitBinder;
import socialradar.locationkit.ILocationKitEventListener;
import socialradar.locationkit.LocationKitService;
import socialradar.locationkit.LocationKitServiceOptions;
import socialradar.locationkit.internal.network.LKNetworkClient;
import socialradar.locationkit.model.LKActivityMode;
import socialradar.locationkit.model.LKPowerLevel;
import socialradar.locationkit.model.LKVenueFilter;
import socialradar.locationkit.model.LKVisit;
import socialradar.locationkit.model.LKVisitCriteria;

/**
 * This class echoes a string called from JavaScript.
 */
public class LocationKit extends CordovaPlugin {
    private static String LOG_TAG = "LocationKitCordova";
    private boolean bound = false;
    private CallbackContext serviceCallbackContext;
    private ILocationKitBinder service;
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(LOG_TAG, "initializing LocationKitPlugin");
        if (!bound) {
            Intent i = new Intent(cordova.getActivity(), LocationKitService.class);
            cordova.getActivity().getApplicationContext().bindService(i, connection, Service.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (bound) {
                cordova.getActivity().getApplicationContext().unbindService(connection);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "error unbinding service",e);
        }
        Log.v(LOG_TAG, "destroy LocationKitPlugin");
        super.onDestroy();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("startWithApiToken")) {
            if (bound && service != null) {
                handleStartWithApiToken(args, callbackContext);
            }
            return true;
        } else if (action.equals("getCurrentPlace")) {
            return true;
        } else if (action.equals("getPlaceForLocation")) {
            return true;
        } else if (action.equals("getCurrentLocation")) {
            return true;
        } else if (action.equals("pause")) {
            return true;
        } else if (action.equals("resume")) {
            return true;
        } else if (action.equals("searchForPlaces")) {
            return true;
        } else if (action.equals("updateUserValues")) {
            return true;
        }
        return false;
    }
    private void handleStartWithApiToken(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String apiToken = args.getString(0);
        LocationKitServiceOptions lkServiceOptions = null;
        if (args.get(1) != null) {
            JSONObject serviceOptionsJson = args.getJSONObject(1);
            LocationKitServiceOptions.Builder builder = new LocationKitServiceOptions.Builder();
            if (serviceOptionsJson.has("interval")) {
                builder.withInterval(serviceOptionsJson.getLong("interval"));
            }
            if (serviceOptionsJson.has("power_level")) {
                builder.withPowerLevel(LKPowerLevel.valueOf(serviceOptionsJson.getString("power_level")));
            }
            if (serviceOptionsJson.has("visit_criteria")) {
                JSONArray array = serviceOptionsJson.getJSONArray("visit_criteria");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    LKVisitCriteria criteria = new LKVisitCriteria();
                    criteria.setAddressId(object.getString("address_id"));
                    criteria.setRadius(object.getDouble("radius"));
                    criteria.setVenueCategory(object.getString("venue_category"));
                    criteria.setVenueName(object.getString("venue_name"));
                    builder.withVisitCriteria(criteria);
                }
            }
            lkServiceOptions = builder.build();
        }
        this.serviceCallbackContext = callbackContext;
        service.startWithApiToken(apiToken,lkServiceOptions,listener);
    }
    /**
     *   if (message != null && message.length() > 0) {
     callbackContext.success(message);
     } else {
     callbackContext.error("Expected one non-empty string argument.");
     }
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bound = true;
            LocationKit.this.service = (ILocationKitBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    private ILocationKitEventListener listener = new ILocationKitEventListener() {
        @Override
        public void onStartVisit(LKVisit lkVisit) {
            Log.v(LOG_TAG, "visit started");
            if (serviceCallbackContext != null) {

                try {
                    String dataStr = LKNetworkClient.sGson.toJson(lkVisit);
                    JSONObject object = new JSONObject();
                    object.put("msg_type", "start_visit");
                    JSONObject data = new JSONObject(dataStr);
                    object.put("visit", data);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, object);
                    result.setKeepCallback(true);
                    serviceCallbackContext.sendPluginResult(result);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "failed",e);
                }
            }
        }

        @Override
        public void onEndVisit(LKVisit lkVisit) {
            Log.v(LOG_TAG, "visit ended");
            if (serviceCallbackContext != null) {
                try {
                    String dataStr = LKNetworkClient.sGson.toJson(lkVisit);
                    JSONObject object = new JSONObject();
                    object.put("msg_type", "end_visit");
                    JSONObject data = new JSONObject(dataStr);
                    object.put("visit", data);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, object);
                    result.setKeepCallback(true);
                    serviceCallbackContext.sendPluginResult(result);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "failed",e);
                }
            }
        }

        @Override
        public void onNetworkUnavailable() {
            Log.v(LOG_TAG, "network available");
        }

        @Override
        public void onNetworkAvailable() {
            Log.v(LOG_TAG, "network unavailable");
        }

        @Override
        public void onLocationManagerDisabled() {
            Log.v(LOG_TAG, "locationmanager disabled");
        }

        @Override
        public void onLocationManagerEnabled() {
            Log.v(LOG_TAG, "locationmanager enabled");
        }

        @Override
        public void onChangedActivityMode(LKActivityMode lkActivityMode) {
            Log.v(LOG_TAG, "activity changed");
            if (serviceCallbackContext != null) {
                JSONObject object = new JSONObject();
                try {
                    object.put("type", "activity_change");
                    object.put("activity", lkActivityMode.toString());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "failed",e);
                }
                PluginResult result = new PluginResult(PluginResult.Status.OK, object);
                result.setKeepCallback(true);
                serviceCallbackContext.sendPluginResult(result);
            }
        }

        @Override
        public void onError(Exception e, String s) {
            Log.e(LOG_TAG, s, e);
            if (serviceCallbackContext != null) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, s);
                result.setKeepCallback(true);
                serviceCallbackContext.sendPluginResult(result);
            }
        }

        @Override
        public void onPermissionDenied(String s) {
            Log.e(LOG_TAG, s);
            if (serviceCallbackContext != null) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, s);
                result.setKeepCallback(true);
                serviceCallbackContext.sendPluginResult(result);
            }
        }

        @Override
        public void onUnbind() {
            Log.v(LOG_TAG, "UNBIND");
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.v(LOG_TAG, "Location Changed");
            if (serviceCallbackContext != null) {
                JSONObject object = new JSONObject();
                try {
                    object.put("msg_type", "location");
                    JSONObject data = new JSONObject();

                    data.put("latitude", location.getLatitude());
                    data.put("longitude", location.getLongitude());
                    data.put("accuracy", location.getAccuracy());
                    data.put("speed", location.getSpeed());
                    data.put("bearing", location.getBearing());
                    data.put("provider", location.getProvider());
                    data.put("position", data);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "failed",e);
                }

                PluginResult result = new PluginResult(PluginResult.Status.ERROR, object);
                result.setKeepCallback(true);
                serviceCallbackContext.sendPluginResult(result);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.v(LOG_TAG, "status changed");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.v(LOG_TAG,"providerEnabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.v(LOG_TAG, "providerDisabled");
        }
    };
}
