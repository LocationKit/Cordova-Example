/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
L.mapbox.accessToken = 'pk.eyJ1Ijoiam9obmZvbnRhaW5lIiwiYSI6ImIwZjJmNjE0NTUxYTExYWUzYWY0ZWQ0ZjM1YmNjMzZmIn0.U3BspxMHfZRfSIY3ap8PwA';
var map = "empty";
var marker;

function stop() {
    var success = function() {
        var button = document.getElementById("button")
        button.innerHTML = "Start";
        button.removeEventListener("click", stop, false);
        button.addEventListener("click", start );
        document.getElementById("search").removeEventListener("click", searchPlaces);
        document.getElementById("showCurrentLocation").removeEventListener("click", showCurrentLocation);
        document.getElementById("showCurrentPlace").removeEventListener("click", showCurrentPlace);
        document.getElementById("testPlaceWithLocation").removeEventListener("click", testPlaceWithLocation);
    }
    var fail = function() {
        writeErrorToTable("Stop failed");
    }
    LocationKit.pause(success, fail);
}

function start() {
    var callbackSuccess = function(message) {
        if (message.msg_type === "start") {
            writeToTable("Location Kit Started");
            var button = document.getElementById("button")
            button.innerHTML = "Stop";
            button.removeEventListener("click", start, false);
            button.addEventListener("click", stop );
            document.getElementById("search").addEventListener("click", searchPlaces);
            document.getElementById("showCurrentLocation").addEventListener("click", showCurrentLocation);
            document.getElementById("showCurrentPlace").addEventListener("click", showCurrentPlace);
            document.getElementById("testPlaceWithLocation").addEventListener("click", testPlaceWithLocation);
            console.log("started");
            return;
        }
        if (message.msg_type === "location") {
            if (map === "empty") {
                map = L.mapbox.map('map', 'mapbox.streets')
                .setView([message.position.coords.latitude, message.position.coords.longitude], 17);
                marker = L.marker([message.position.coords.latitude, message.position.coords.longitude]).addTo(map);
            } else {
                map.setView([message.position.coords.latitude, message.position.coords.longitude], 17);
                marker.setLatLng([message.position.coords.latitude, message.position.coords.longitude]).update();
            }
            writeToTable("Got location: " + message.position.coords.latitude + " " + message.position.coords.longitude);
            return;
        }
        if (message.msg_type === "start_visit") {
            var d = new Date(message.visit.arrival_date);
            writeToTable("Arrived: " + d.toDateString() + " "+ ( message.visit.place.venue.name || message.visit.place.address.street_number + " " + message.visit.place.address.street_name) );
            return;
        }
        if (message.msg_type === "end_visit") {
            var d = new Date(message.visit.departure_date);
            writeToTable("Left:  " + d.toDateString() + " "+ ( message.visit.place.venue.name || message.visit.place.address.street_number + " " + message.visit.place.address.street_name));
        }
        if (message.msg_type == "activity_changed") {
            writeToTable("Activity Detected: " + message.activity);
        }
    }
    var callbackError = function(message) {
        writeErrorToTable("Error calling LocationKit Plugin " + message.error);
    }
    LocationKit.startWithApiToken("5cf3fea962f53a33",null, callbackSuccess,callbackError );
}
function writeErrorToTable(data) {
    var parentElement = document.getElementById("output");
    var dataElement = document.createElement("p");
    dataElement.setAttribute("class","event error");
    dataElement.innerHTML = data;
    if (parentElement.hasChildNodes()) {
        var elem = parentElement.firstChild;
        parentElement.insertBefore(dataElement, elem);
    } else {
        parentElement.appendChild(dataElement);
    }
    
}

function writeToTable(data) {
    var parentElement = document.getElementById("output");
    var dataElement = document.createElement("p");
    dataElement.setAttribute("class","event data");
    dataElement.innerHTML = data;
    if (parentElement.hasChildNodes()) {
        var elem = parentElement.firstChild;
        parentElement.insertBefore(dataElement, elem);
    } else {
        parentElement.appendChild(dataElement);
    }
}
function searchPlaces() {
    var success = function(places) {
        var result = "<ul>";
        for (var i in places) {
            var place = places[i];
            result += "<li>" + (place.venue.name || place.address.street_number + " " +place.address.street_name) +  "</li>";
        }
        result += "</ul>";
        writeToOverlay(result);
    }
    var fail = function(message) {
        writeErrorToTable(message.error);
    }
    var search_request = LocationKit.makeLKSearchRequest(0,0,1000,100,null,"cafe");
    writeToTable("search for cafes");
    LocationKit.searchForPlaces(search_request, success,fail);
}
function showCurrentLocation() {
    var success = function(position) {
        writeToTable("Current Location " + position.coords.latitude + "," + position.coords.longitude);
    }
    var fail = function(error) {
        writeErrorToTable(error);
    }
    writeToTable("show current location place");
    LocationKit.getCurrentLocation(success,fail);
}
function showCurrentPlace() {
    var success = function() {
        writeToTable("Current place: " +  (place.venue.name != null ?  place.venue.name : place.address.street_number + " " + place.address.street_name ) );
    }
    var fail = function(message) {
        writeErrorToTable(message);
    }
    writeToTable("getting current place");
    LocationKit.getCurrentPlace(success,fail);
}
function testPlaceWithLocation() {
    var success = function(place) {
        writeToTable("Got place: " +  (place.venue.name != null ?  place.venue.name :  place.address.street_number + " " + place.address.street_name ) );
    }
    var fail = function(message) {
        writeErrorToTable(message);
    }
    writeToTable("getting place");
    LocationKit.getPlaceForLocation(38.90441582543816,-77.04319066997527, success,fail);
}
function clearAndHideOverlayWindow() {
    var overlay = document.getElementById("overlay");
    var overlayData = document.getElementById("overlayData");
    overlayData.innerHTML = "";
    overlay.setAttribute("class", "hideOverlay");
}
function writeToOverlay(data) {
    var overlay = document.getElementById("overlay");
    overlay.setAttribute("class", "showOverlay");
    var overlayData = document.getElementById("overlayData");
    overlayData.innerHTML = data;
    
}


var app = {
    // Application Constructor
initialize: function() {
    this.bindEvents();
    
},
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
bindEvents: function() {
    document.addEventListener('deviceready', this.onDeviceReady, false);
},
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
onDeviceReady: function() {
    document.getElementById("button").addEventListener("click", start);
    document.getElementById("overlayClose").addEventListener("click",clearAndHideOverlayWindow);
    app.receivedEvent('deviceready');
    //    LocationKit.resume(null,null);
},
    // Update DOM on a Received Event
receivedEvent: function(id) {
    var parentElement = document.getElementById(id);
    var listeningElement = parentElement.querySelector('.listening');
    var receivedElement = parentElement.querySelector('.received');
    
    listeningElement.setAttribute('style', 'display:none;');
    receivedElement.setAttribute('style', 'display:block;');
    
    console.log('Received Event: ' + id);
}
};


app.initialize();