var data3;
$.get("stats", {"avgstrength":"0"}, function (data) {
    console.log(data);
    data3 = data;

    new Morris.Bar({
        element: 'graph1',
        data:data3,
        xkey: 'x',
        ykeys: 'y',
        labels: ['Average Strength']
    });
});

new Morris.Donut({
    element: 'graph2',
    data: [
        {label: "", value: 8},
        {label: "", value: 8},
        {label: "", value: 8}
    ]
});

new Morris.Line({
    element: 'graph4',
    data: [
        { y: '2006', a: 100, b: 90 },
        { y: '2007', a: 75,  b: 65 },
        { y: '2008', a: 50,  b: 40 },
        { y: '2009', a: 75,  b: 65 },
        { y: '2010', a: 50,  b: 40 },
        { y: '2011', a: 75,  b: 65 },
        { y: '2012', a: 100, b: 90 }
    ],
    xkey: 'y',
    ykeys: ['a', 'b'],
    labels: ['Series A', 'Series B']
});
