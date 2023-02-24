var connection = new WebSocket('ws://127.0.0.1:4444');
demandedEnergy = []
receivedEnergy = []
labelsarr = []
var lineChart1
var lineChart2
first1 = true
first2 = true

regions = Object.keys(RawData)
borders={}
regions.forEach(region => {
    //console.log(region);
    //RawData.region is an array of js
    len = RawData[region].length;
    borders[region] = []
    RawData[region].forEach(element => {
        borders[region].push([element.lat, element.lng]);
    })
});


var map = L.map('map').setView([-12.829544, 45.1719858], 11);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  }).addTo(map);
  

  // create a red polygon from an array of LatLng points
//var latlngs = borders["Acoua"];

//var polygon = L.polygon(latlngs, {color: 'red'}).addTo(map);

polygons = regions
for (let i =0; i<regions.length;i++){
    
    console.log(regions[i])
    polygons[i] =  L.polygon(borders[regions[i]], {color: 'red'}).addTo(map);
    // polygons[i].bindPopup("I am a polygon.");
    regions = Object.keys(RawData)
    polygons[i].on('mouseover', function f(){
        polygons[i].bindPopup(regions[i]).openPopup();//"Region name: "+ +". <br/>  Please choose timestep to visualize data."
    });
    polygons[i].on('click', function f(){
        connection.send(regions[i]); // Send the message 'Mayotte' to the server //polygons[i].bindPopup(regions[i]).openPopup();//"Region name: "+ +". <br/>  Please choose timestep to visualize data."
    });
}


connection.onopen = function () {
    console.log('Connected!');
    connection.send('Handréma'); // Send the message 'Mayotte' to the server //Handréma //Mayotte //Mzouazia //Mbouanatsa
};

// Log errors
connection.onerror = function (error) {
    console.log('WebSocket Error ' + error);
};



// Log messages from the server
connection.onmessage = function (e) {
    console.log('Server: ' + e.data);
    var jsonObj = JSON.parse(e.data);


    if(jsonObj.hasOwnProperty('number_dem')){
        max = jsonObj['number_dem']
        for (i=0; i<max; i++){
            receivedEnergy.push(jsonObj['rec_'+i])
            labelsarr.push(i)
        }
        for (i=0; i<max; i++){
            demandedEnergy.push(jsonObj['dem_'+i])
            //labelsarr.push(i)
        }
    }
    else{
        index = jsonObj['index']
        receivedEnergy.push(jsonObj['rec_'+index])
        demandedEnergy.push(jsonObj['dem_'+index])
        labelsarr.push(index)
    }




    //const labels = [0,1,2,3,4,5,6,7,8,9];
    var ctx1 =document.getElementById("chart1")
    console.log(first1)
    if (!first1) {
        lineChart1.destroy()
     }
     else{
        first1=false
     }
    lineChart1 = new Chart(ctx1, {
        type : 'line',
        data : {
            labels: labelsarr,
            datasets: [
            // {
            // label: 'Real Demand',
            // data: [],//65, 59, 80, 81, 56, 55, 40
            // fill: false,
            // borderColor: 'rgb(85, 138, 82)',
            // tension: 0.1
            // },
            {
                label: 'Energy Demanded (kWh)',
                data: demandedEnergy,//34, 34, 73, 37, 57, 55, 47
                fill: false,
                borderColor: 'rgb(75, 192, 192)',
                tension: 0.1
            },
            {
                label: 'Received Energy (kWh)',
                data: receivedEnergy,//65, 78, 84, 89, 56, 53, 50
                fill: false,
                borderColor: 'rgb(64, 34, 192)',
                tension: 0.1
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true,
                },
                // yAxes: [{
                //     scaleLabel: {
                //       display: true,
                //       labelString: 'Energy (KWh)'
                //     }
                //   }],
                //   xAxes: [{
                //     scaleLabel: {
                //       display: true,
                //       labelString: 'Time (hour)'
                //     }
                //   }],//,
                //x : {
                //    max: jsonObj["number"],
                //    min: 0,
                //    ticks: {
                //        stepSize: 1
                //    }
                //}
            }
        }
    })





//const labels = [0,1,2,3,4,5,6,7,8,9];
    var ctx2 =document.getElementById("chart2")
    console.log(first2)
    if (!first2) {
        lineChart2.destroy()
     }
     else{
        first2=false
     }
    lineChart2 = new Chart(ctx2, {
        type : 'line',
        data : {
            labels: labelsarr,
            datasets: [
            // {
            // label: 'Real Demand',
            // data: [],//65, 59, 80, 81, 56, 55, 40
            // fill: false,
            // borderColor: 'rgb(85, 138, 82)',
            // tension: 0.1
            // },
            {
                label: 'Energy Demanded (kWh)',
                data: demandedEnergy,//34, 34, 73, 37, 57, 55, 47
                fill: false,
                borderColor: 'rgb(75, 192, 192)',
                tension: 0.1
            },
            {
                label: 'Received Energy (kWh)',
                data: receivedEnergy,//65, 78, 84, 89, 56, 53, 50
                fill: false,
                borderColor: 'rgb(64, 34, 192)',
                tension: 0.1
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true,
                },
                // yAxes: [{
                //     scaleLabel: {
                //       display: true,
                //       labelString: 'Energy (KWh)'
                //     }
                //   }],
                //   xAxes: [{
                //     scaleLabel: {
                //       display: true,
                //       labelString: 'Time (hour)'
                //     }
                //   }],//,
                //x : {
                //    max: jsonObj["number"],
                //    min: 0,
                //    ticks: {
                //        stepSize: 1
                //    }
                //}
            }
        }
    })
};


// const labels = [0,1,2,3,4,5,6,7,8,9];
// var ctx =document.getElementById("chart")
// var lineChart = new Chart(ctx, {
//     type : 'line',
//     data : {
//         labels: labels,
//         datasets: [{
//           label: 'Real Data',
//           data: [],//65, 59, 80, 81, 56, 55, 40
//           fill: false,
//           borderColor: 'rgb(85, 138, 82)',
//           tension: 0.1
//         },
//         {
//             label: 'Predicted Data',
//             data: [],//34, 34, 73, 37, 57, 55, 47
//             fill: false,
//             borderColor: 'rgb(75, 192, 192)',
//             tension: 0.1
//           },
//           {
//             label: 'Received Data',
//             data: [],//65, 78, 84, 89, 56, 53, 50
//             fill: false,
//             borderColor: 'rgb(64, 34, 192)',
//             tension: 0.1
//           }]
//       },
//       options: {
//         scales: {
//             y: {
//                 beginAtZero: true
//             }
//         }
//     }
// })

// console.log(pvs)




//// zoom the map to the polygon
//map.fitBounds(polygon.getBounds());

  //L.marker([-12.69924690840159, 45.05121618004634]).addTo(map)
  //    .bindPopup('A pretty CSS3 popup.<br> Easily customizable.')
  //   .openPopup();</script>



