
google.charts.load('current', {'packages':['corechart']});

google.charts.setOnLoadCallback(drawChart);

function drawChart() {

    var chart = new google.visualization.PieChart(document.getElementById('results'));
    chart.draw(data, options);
}
