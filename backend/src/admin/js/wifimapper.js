$( document ).ready(function() {
    $("#my-final-table").DataTable({
        ajax: "apn",
        columns: [
            { data: "bssid" },
            { data: "ssid" },
            { data: "location.x" },
            { data: "location.y" },
            { data: "speed" }
        ]
    });
});