var map, heatmap, baseUrl;

function getLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(initMap);
    } else {
        initMap();
    }
}

function initMap(position) {

    map = new google.maps.Map(document.getElementById('map'), {
        zoom: 16,
        center: {lat: -33.9576875, lng: 18.4596769},
        mapTypeId: 'roadmap'
    });

    heatmap = new google.maps.visualization.HeatmapLayer({
        data: getPoints(),
        map: map
    });
}

function getPoints() {

    var points = [];

    $.get("strength", {"timestamp":"0"}, function (data) {
        for(var i=0; i<data.length; i++){
            var obj = data[i];
            points.push({location: new google.maps.LatLng(obj.location.x.valueOf(), obj.location.y.valueOf()), weight: obj.signalStrength});
        }
    });

    return points;
}