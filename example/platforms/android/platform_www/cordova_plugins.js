cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/cordova-locationkit/www/locationkit.js",
        "id": "cordova-locationkit.LocationKit",
        "pluginId": "cordova-locationkit",
        "clobbers": [
            "LocationKit"
        ]
    },
    {
        "file": "plugins/cordova-plugin-whitelist/whitelist.js",
        "id": "cordova-plugin-whitelist.whitelist",
        "pluginId": "cordova-plugin-whitelist",
        "runs": true
    },
    {
        "file": "plugins/org.apache.cordova.splashscreen/www/splashscreen.js",
        "id": "org.apache.cordova.splashscreen.SplashScreen",
        "pluginId": "org.apache.cordova.splashscreen",
        "clobbers": [
            "navigator.splashscreen"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "cordova-locationkit": "1.0.6",
    "cordova-plugin-whitelist": "1.0.0",
    "org.apache.cordova.geolocation": "0.3.12",
    "org.apache.cordova.splashscreen": "1.0.0"
}
// BOTTOM OF METADATA
});