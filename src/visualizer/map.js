
$("#logs").hide();
$(document).ready(function() {
    var connection = new WebSocket('ws://127.0.0.1:4444');
    demandedEnergy = []
    receivedEnergy = []
    batStored = []
    demandratios = []
    pvratios = []
    impactCarbons = []
    bestPhases = []
    labelsarr = []
    var lineChart
    var indicatorsChart
    //var lineChart2
    first = true






    connection.onopen = function () {
        console.log('Connected!');
        requestdatajson ={
            "request" : "vis",
//            "target" : "Mayotte"
            "target" : "Mamoudzou"
        } 
        connection.send(JSON.stringify(requestdatajson)); // Send the message 'Mayotte' to the server //Handréma //Mayotte //Mzouazia //Mbouanatsa
        requestdatainfo = {
            "request" : "info",
            "holon" : "Level 1"
        }
        connection.send(JSON.stringify(requestdatainfo))
    };

    // Log errors
    connection.onerror = function (error) {
        console.log('WebSocket Error ' + error);
    };
    //server
    connection.onmessage = function (e) {
        console.log('Server: ' + e.data);
        var jsonObj = JSON.parse(e.data);

        if(jsonObj['type'] == "vis" || true){
            // Visualisation
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
                for (i=0; i<max; i++){
                    batStored.push(jsonObj['bat_'+i])
                    //labelsarr.push(i)
                }
                for (i=0; i<max; i++){
                    demandratios.push(jsonObj['demandratio_'+i])
                    //labelsarr.push(i)
                }
                for (i=0; i<max; i++){
                    pvratios.push(jsonObj['pvratio_'+i])
                    //labelsarr.push(i)
                }
                for (i=0; i<max; i++){
                    impactCarbons.push(jsonObj['impactCarbon_'+i])
                    //labelsarr.push(i)
                }
                for (i=0; i<max; i++){
                    bestPhases.push(jsonObj['bestPhase_'+i])
                    //labelsarr.push(i)
                }
            }
            else{
                index = jsonObj['index']
                receivedEnergy.push(jsonObj['rec_'+index])
                demandedEnergy.push(jsonObj['dem_'+index])
                batStored.push(jsonObj['bat_'+index])
                demandratios.push(jsonObj['demandratio_'+index])
                pvratios.push(jsonObj['pvratio_'+index])
                impactCarbons.push(jsonObj['impactCarbon_'+index])
                bestPhases.push(jsonObj['bestPhase_'+index])
                if(index=='24') saveGraphs();
                console.log("verification : "+ (jsonObj['rec_'+index]==(jsonObj['dem_'+index]+jsonObj['bat_'+index])))
                labelsarr.push(index)
            }
        }
        else{
            //Logs
            if(jsonObj['type']=="log"){
                logList = document.getElementById("log-list")
                var logItem = document.createElement('li');
                logItem.classList.add('list-group-item');
                logItem.textContent = jsonObj['log'];
        
                logList.appendChild(logItem);
                
            }
            else{
                //info
                if(jsonObj['type']=="info"){
                    logList = document.getElementById("log-list")
                    var logItem = document.createElement('li');
                    logItem.classList.add('list-group-item');
                    logItem.textContent = jsonObj['log'];
            
                    logList.appendChild(logItem);
                }
        }
    }




        //const labels = [0,1,2,3,4,5,6,7,8,9];
        var ctx =document.getElementById("chart")
        var ctx2 =document.getElementById("chart2")
        var ctx3 =document.getElementById("chart3")
//        var ctx4 =document.getElementById("chart4")
//        var ctx5 =document.getElementById("chart5")
        console.log(first)
        if (!first) {
            lineChart.destroy()
            lineChart2.destroy()
            lineChart3.destroy()
//            lineChart4.destroy()
//            lineChart5.destroy()
        }
        else{
            first=false
        }
        lineChart = new Chart(ctx, {
            type : 'line',
            data : {
                labels: labelsarr,
                datasets: [
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
                },
                {
                    label: 'Battery Storage (kWh)',
                    data: batStored,//65, 78, 84, 89, 56, 53, 50
                    fill: false,
                    borderColor: 'rgb(155, 155, 155)',
                    tension: 0.1
                }]//]
            },
            options: {
                devicePixelRatio: 7,
                scales: {
                    y: {
                        beginAtZero: true,
                    },
                }
            }
        })
        
        lineChart2 = new Chart(ctx2, {
            type : 'line',
            data : {
                labels: labelsarr,
                datasets: [
                {
                    label: 'RES Ratio',
                    data: pvratios,//65, 78, 84, 89, 56, 53, 50
                    fill: false,
                    borderColor: 'rgb(64, 34, 192)',
                    tension: 0.1
                },
                {
                    label: 'Satisfaction Ratio',
                    data: demandratios,//34, 34, 73, 37, 57, 55, 47
                    fill: false,
                    borderColor: 'rgb(75, 192, 192)',
                    tension: 0.1
                },
                {
                    label: 'Reached phase',
                    data: bestPhases,//65, 78, 84, 89, 56, 53, 50
                    fill: false,
                    borderColor: 'rgb(155, 155, 155)',
                    tension: 0.1
                }
                ]//]
            },
            options: {
                devicePixelRatio: 7,
                scales: {
                    y: {
                        beginAtZero: true,
                    },
                }
            }
        })


        lineChart3 = new Chart(ctx3, {
            type : 'line',
            data : {
                labels: labelsarr,
                datasets: [
                {
                    label: 'Impact Carbon',
                    data: impactCarbons,//65, 78, 84, 89, 56, 53, 50
                    fill: false,
                    borderColor: 'rgb(155, 155, 155)',
                    tension: 0.1
                }]//]
            },
            options: {
                devicePixelRatio: 7,
                scales: {
                    y: {
                        beginAtZero: true,
                    },
                }
            }
        })
    };


    console.log("reached !")

    $("#logsBtn").on( "click", function() {
        $("#logs" ).show();
        $("#sim").hide();
        //console.log("clicked");
    });
    
    $("#simBtn").on( "click", function() {
        $("#sim" ).show();
        $("#logs").hide();
        //console.log("clicked 2");
    });

    $("#getLogsBtn").on( "click", function() {
        logList = document.getElementById("log-list")
        logList.html=''
        holon
        agent
        priority
        json ={
            "request" : "log",
            "target" : holon,
            "agent" : agent,
            "priority" : priority
        }
        connection.send(json);
    });


    function selectLevel() {
        var selectedLevel = document.getElementById('targetSelect').value;
        console.log('Selected target:', selectedValue);

        json ={
            "request" : "info",
            "target" : selectedLevel
        }
        connection.send(json);
    }
    
    function saveGraphs(){
        saveGraph("chartContainer", "chart1.pdf")
        saveGraph("chart2Container", "chart2.pdf")
        saveGraph("chart3Container", "chart3.pdf")
    }
    
    function saveGraph(containerId, path) {
        // Get the div element containing the chart
        const chartContainer = document.getElementById(containerId);

        // Set the width and height of the PDF to match the div
        const divWidth = chartContainer.offsetWidth;
        const divHeight = chartContainer.offsetHeight;
        
        
        // Create a new jsPDF instance
        const pdf = new jsPDF({
            unit: "px",
            format: [divWidth, divHeight],
            dpi: 1000, // Increase this value for higher DPI
          });
        
        // Set the scale and quality options for html2canvas
        const scale = 5; // Increase this value for higher resolution
        const quality = 10; // Increase this value for better image quality

        // Use html2canvas to render the div content as an image
        html2canvas(chartContainer, { scale, quality }).then(canvas => {
            const imgData = canvas.toDataURL("image/png");

            // Add the image to the PDF
            pdf.addImage(imgData, "PNG", 0, 0, divWidth, divHeight);

            // Save the PDF
            pdf.save(path);
        });
    }

    function sendChanges(){
        var holonId = document.getElementById("holonID").value;
        var typeSelect = document.getElementById('typeSelect').value;
        var valueInput = document.getElementById('valueInput').value;
        var powerInput = document.getElementById('powerInput').value;
        var capacityInput = document.getElementById('capacityInput').value;
        var ecoCost = document.getElementById('ecoCost').value;
        var envCost = document.getElementById('envCost').value;

        var formData = {
            request: "mod",
            target: holonId,
            type: typeSelect,
        }

        if (typeSelect === 'demandRatio') {
            formData["value"] = valueInput;
        }else if (typeSelect === 'pvRatio' || typeSelect === 'windValue' || typeSelect === 'thermalGeneration') {
            formData["value"] = valueInput;
            formData["ecoCost"] = ecoCost;
            formData["envCost"] = envCost;
        }
        else if (typeSelect === 'storage') {
            formData["powerInput"] = powerInput;
            formData["capacityInput"] = valueInput;
            formData["ecoCost"] = ecoCost;
            formData["envCost"] = envCost;
        }
        


        connection.send(JSON.stringify(formData));
        
    }


  document.getElementById('typeSelect').addEventListener('change', function() {
    var typeSelect = this.value;
    var valueGroup = document.getElementById('valueGroup');
    var powerGroup = document.getElementById('powerGroup');
    var capacityGroup = document.getElementById('capacityGroup');
    var econGroup = document.getElementById('econGroup');
    var envGroup = document.getElementById('envGroup');

    valueGroup.style.display = 'none';
    powerGroup.style.display = 'none';
    capacityGroup.style.display = 'none';

    if (typeSelect === 'disconnection' || typeSelect === 'no_thermal') {
      // Do nothing, only buttons are shown
    } else if (typeSelect === 'demandRatio') {
      valueGroup.style.display = 'block';
    } else if (typeSelect === 'pvRatio' || typeSelect === 'windValue' || typeSelect === 'thermalGeneration') {
      valueGroup.style.display = 'block';
      econGroup.style.display = 'block';
      envGroup.style.display = 'block';
    } else if (typeSelect === 'storage') {
      powerGroup.style.display = 'block';
      capacityGroup.style.display = 'block';
      econGroup.style.display = 'block';
      envGroup.style.display = 'block';
    }
  });

  document.getElementById('typeSelect').dispatchEvent(new Event('change'));
    
    
    $("#saveGraph").on("click", saveGraphs);
    $("#applyChanges").on("click", sendChanges);

});

