$(document).ready(function () {
  $("#close").click(function () {
    $("#menuId").slideUp("medium");
    $("#close").hide();
    $("#open").show();
  });
  $("#open").click(function () {
    $("#menuId").slideDown("slow");
    $("#open").hide();
    $("#close").show();
  });
});
function move(temp) {
  if (temp == 'bar') {
    $("#boxplot_chart").slideUp("medium");
    $("#line_chart").slideUp("medium");
    $("#other_stats").slideUp("medium");
    $("#bar_chart").slideDown("medium");
  }
  else if (temp == 'box') {
    $("#bar_chart").slideUp("medium");
    $("#line_chart").slideUp("medium");
    $("#other_stats").slideUp("medium");
    $("#boxplot_chart").slideDown("medium");
  }
  else if (temp == 'line') {
    $("#bar_chart").slideUp("medium");
    $("#boxplot_chart").slideUp("medium");
    $("#other_stats").slideUp("medium");
    $("#line_chart").slideDown("medium");
  }
  else if (temp == 'other') {
    $("#bar_chart").slideUp("medium");
    $("#boxplot_chart").slideUp("medium");
    $("#line_chart").slideUp("medium");
    $("#other_stats").slideDown("medium");
  }
}
var noofclients=["1","4","8","16","32"];
var dataset_available=[];
var connectionstring="http://131.234.28.165:3030";
    var datastore=""; //nodefault
    var postconnection="/sparql?query=";
    var queryclient="SELECT  ?client "+
                    "WHERE {"+
                    "?query <http://iguana-benchmark.eu/properties/noOfWorkers> ?client ."+
                    "}";
    var queryavgqps="SELECT AVG( ?qps )"+
                    "WHERE {"+
                    "?query <http://iguana-benchmark.eu/properties/queriesPerSecond> ?qps ."+
                    "}";
    var queryVersionNo="SELECT ?name "+
                    "{"+
                    "?query <http://iguana-benchmark.eu/properties/connection> ?name ."+
                    "}";
var counter;
function parseData(createGraph) {
	Papa.parse("http://131.234.28.165:3000/expected_csv.csv", {
		download: true,
		complete: function(results) {
			createGraph(results.data);
		}
	});
}

function createGraph(data) {
  var triplestoreTentris = ['Tentris'];
  var triplestoreFuseki = ['Fuseki'];
  var triplestoreVirtuso = ['Virtuoso'];
  var dbtriplestoreTentris = ['Tentris'];
  var dbtriplestoreFuseki = ['Fuseki'];
  var dbtriplestoreVirtuso = ['Virtuoso'];
  var wattriplestoreTentris = ['Tentris'];
  var wattriplestoreFuseki = ['Fuseki'];
  var wattriplestoreVirtuso = ['Virtuoso'];

  var noofclients=[];

	for (var i = 1; i < data.length; i++) {
    if(data[i][3]=="SWDF")
    {
        if(data[i][4]=="Tentris")
        {
            triplestoreTentris.push(data[i][7]);
            noofclients.push(data[i][5]);
        }
        else
        if(data[i][4]=="Fuseki")
        {
          triplestoreFuseki.push(data[i][7]);

        }
        else
        if(data[i][4]=="Virtuoso")
        {
          triplestoreVirtuso.push(data[i][7]);
        }
    }
    else{
      if(data[i][3]=="DBpedia")
    {
        if(data[i][4]=="Tentris")
        {
            dbtriplestoreTentris.push(data[i][7]);
            noofclients.push(data[i][5]);
        }
        else
        if(data[i][4]=="Fuseki")
        {
          dbtriplestoreFuseki.push(data[i][7]);

        }
        else
        if(data[i][4]=="Virtuoso")
        {
          dbtriplestoreVirtuso.push(data[i][7]);
        }
    }
    else{
      if(data[i][4]=="Tentris")
        {
            wattriplestoreTentris.push(data[i][7]);
            noofclients.push(data[i][5]);
        }
        else
        if(data[i][4]=="Fuseki")
        {
          wattriplestoreFuseki.push(data[i][7]);

        }
        else
        if(data[i][4]=="Virtuoso")
        {
          wattriplestoreVirtuso.push(data[i][7]);
        }

    }

    }


	}
  var bar_chart = c3.generate({
    bindto: '#bar_chart',
    data: {
        columns: [
            triplestoreTentris,
            triplestoreVirtuso,
            triplestoreFuseki
        ],
        type: 'bar'
    },
    title: {
      text: 'Performance'
    },
    axis: {
      x: {
        label: {

        text: 'Number of Clients',
        position: 'outer-center'
        },
        type: 'category',
        categories:  noofclients,
      },
      y: {
        label: {
          text: 'Average QPS per Client',
          position: 'outer-middle'
          }
      }
    },
    bar: {
        width: {
            ratio: 0.5 // this makes bar width 50% of length between ticks
        }
        // or
        //width: 100 // this makes bar width 100px
    },
    tooltip: {
      format: {
          title: function (d) { return "No of clients " + noofclients[d] },
      }
  }
});

var areachart1 = c3.generate({
  bindto:"#boxplot_chart1",
  data: {
      columns: [
        triplestoreTentris,
        triplestoreVirtuso,
        triplestoreFuseki
      ],
      types: {
        Tentris: 'area-spline',
        Fuseki: 'area-spline',
        Virtuoso:'area-spline'
          // 'line', 'spline', 'step', 'area', 'area-step' are also available to stack
      },
      groups: [['Tentris', 'Fuseki','Virtuoso']]
  },
  title: {
    text: 'SWDF'
  },
  axis: {
    x: {
      label: {

      text: 'Number of Clients',
      position: 'outer-center'
      },
      type: 'category',
      categories:  noofclients,
    },
    y: {
      label: {
        text: 'Average QPS per Client',
        position: 'outer-middle'
        }
    }
  },
  tooltip: {
    format: {
        title: function (d) { return "No of clients " + noofclients[d] },
    }
}

});
var areachart2 = c3.generate({
  bindto:"#boxplot_chart2",
  data: {
      columns: [
        dbtriplestoreTentris,
        dbtriplestoreVirtuso,
        dbtriplestoreFuseki
      ],
      types: {
        Tentris: 'area-spline',
        Fuseki: 'area-spline',
        Virtuoso:'area-spline'
          // 'line', 'spline', 'step', 'area', 'area-step' are also available to stack
      },
      groups: [['Tentris', 'Fuseki','Virtuoso']]
  },
  title: {
    text: 'DBpedia'
  },
  axis: {
    x: {
      label: {

      text: 'Number of Clients',
      position: 'outer-center'
      },
      type: 'category',
      categories:  noofclients,
    },
    y: {
      label: {
        text: 'Average QPS per Client',
        position: 'outer-middle'
        }
    }
  },
  tooltip: {
    format: {
        title: function (d) { return "No of clients " + noofclients[d] },
    }
}

});
var areachart3 = c3.generate({
  bindto:"#boxplot_chart3",
  data: {
      columns: [
        wattriplestoreTentris,
        wattriplestoreVirtuso,
        wattriplestoreFuseki
      ],
      types: {
        Tentris: 'area-spline',
        Fuseki: 'area-spline',
        Virtuoso:'area-spline'
          // 'line', 'spline', 'step', 'area', 'area-step' are also available to stack
      },
      groups: [['Tentris', 'Fuseki','Virtuoso']]
  },
  title: {
    text: 'WatDiv'
  },
  axis: {
    x: {
      label: {

      text: 'Number of Clients',
      position: 'outer-center'
      },
      type: 'category',
      categories:  noofclients,
    },
    y: {
      label: {
        text: 'Average QPS per Client',
        position: 'outer-middle'
        }
    }
  },
  tooltip: {
    format: {
        title: function (d) { return "No of clients " + noofclients[d] },
    }
}

});

var chart1 = c3.generate({
  bindto: '#line_chart1',
  title: {
    text: 'SWDF'
  },
  data: {
      x: 'x',
      columns: [
          ['x', 1, 4, 8, 16, 32],
          triplestoreTentris,
          triplestoreVirtuso,
          triplestoreFuseki

      ]
  },
  axis: {
    x: {
      label: {
      text: 'Number of Clients',
      position: 'outer-center'
      }
    },
    y: {
      label: {
        text: 'Average QPS',
        position: 'outer-middle'
        }
    }
  }
});
var chart2 = c3.generate({
  bindto: '#line_chart2',
  title: {
    text: 'Dbpedia'
  },
  data: {
      x: 'x',
      columns: [
          ['x', 1, 4, 8, 16, 32],
          dbtriplestoreTentris,
          dbtriplestoreVirtuso,
          dbtriplestoreFuseki

      ]
  },
  axis: {
    x: {
        label: {
          text: 'Number of Clients',
        position: 'outer-center'
        }
    },
    y: {
      label: {
      text: 'Average QPS',
      position: 'outer-middle'
      }
    }
  }
});
var chart3 = c3.generate({
  bindto: '#line_chart3',
  title: {
    text: 'WatDiv'
  },
  data: {
      x: 'x',

      columns: [
          ['x', 1, 4, 8, 16, 32],
          wattriplestoreTentris,
          wattriplestoreVirtuso,
          wattriplestoreFuseki

      ]
  },
  axis: {
    x: {
      label: {
        text: 'Number of Clients',
      position: 'outer-center'
      }
    },
    y: {
      label: {
        text: 'Average QPS',
        position: 'outer-middle'
        }
    }
  }
});

}

//Global variables
var arr = [];
var recordIndex = -1;
var version = "";
var versionCheck = false;
var versionIndex;
var noOfClients;


function downloadCsv(){
  window.open('../all_results.csv', 'Download');

  }
  function downloadRdf(){
    window.open('../results_task_2-1-1.nt', 'Download');

    }


  function datasets(datasetstring)
  {
    for(var i=0; i<=datasetstring.data.datasets.length-1; i++)
    {
      dataset_available[i]= datasetstring.data.datasets[i]["ds.name"];
    }

    getResults();

  function getResults()
  {
    var deferred = $.Deferred();
    var i = 0;
    var nextStep = function() {
        if (i<dataset_available.length) {
          counter = i;
          var querystringforclient=connectionstring+dataset_available[i]+postconnection+encodeURI(queryclient);
          runQueries(querystringforclient, i);

          i++;
          setTimeout(nextStep, 200);
        }
        else {
            deferred.resolve(i);
        }
    }
    nextStep();
    return deferred.promise();
  }


    async function runQueries(URL, datasetNo){

      var querystringforVersion=connectionstring+dataset_available[datasetNo]+postconnection+encodeURI(queryVersionNo);
      versionQueryResponse = await axios({
        method: 'get',
        url: querystringforVersion})
      .then(res => {return res})
      .catch(err => console.log(err));

      getVersion(versionQueryResponse);

      response = await axios({
        method: 'get',
        url: URL})
      .then(res => {return res})
      .catch(err => console.log(err));

      getClient(response);
    }

    }

    function getVersion(res)
    {
      resultIs = res.data.results.bindings[0]["name"].value;
      version = resultIs.substring(resultIs.lastIndexOf('/') + 1)
      for(var i=0; i<arr.length; i++){
        if(arr[i][0] == version){    //check if version already exists in 2D array
          versionCheck = true;
          versionIndex = i;
          return;
        }
      }

      arr.push([]); //for a new version, add new row in the 2D array
      recordIndex=recordIndex+1
      arr[recordIndex][0] = version;
      arr[recordIndex][1] = version.substring(0, version.lastIndexOf('$'));
      versionCheck = true;
      versionIndex = i;
    }

    async function getClient(clients){
        noOfClients = clients.data.results.bindings[0].client.value;

        var querystringforqps=connectionstring+dataset_available[counter]+postconnection+encodeURI(queryavgqps);

        axios({
          method: 'get',
          url: querystringforqps})
        .then(res => getQPS(res))
        .catch(err => console.log(err));
    }

    function getQPS(res) //get QPS and save it at the correct index in 2D array
    {
      AQPS = res.data.results.bindings[0][".1"].value;
      //console.log("version, arrayIndex of version, No of clients, AQPS:", version, versionIndex, noOfClients, AQPS)
      switch (noOfClients) {
        case "1":
          arr[versionIndex][2] = AQPS;
          break;
        case "4":
          arr[versionIndex][3] = AQPS
          break;
        case "8":
          arr[versionIndex][4] = AQPS
          break;
        case "16":
          arr[versionIndex][5] = AQPS
          break;
        case "32":
          arr[versionIndex][6] = AQPS
          break;
      }


    }

    function get2dArray() {
      var datasetsstring="http://131.234.28.165:3030/$/datasets";

      axios({
          method: 'get',
          url: datasetsstring})
        .then(res => datasets(res))
        .catch(err => console.log(err));
      setTimeout(function(){
        console.log("array: ",arr);
        return arr;
      }, 5000);

    }

    function displayGraph()
    {
      var e = document.getElementById("TentrisVersion");
      var TentrisVersionSelected = e.options[e.selectedIndex].value;
      if(TentrisVersionSelected=="")
      {

        alert("please select version for Tentris");
      }
      TentrisVersionSelected="Tentris$"+TentrisVersionSelected;


      /*var e = document.getElementById("FusekiVersion");
      var FusekiVersionSelected = e.options[e.selectedIndex].value;
      if(FusekiVersionSelected==null)
      {

        alert("please select version for Fuseki");
      }
      FusekiVersionSelected="fuseki$"+FusekiVersionSelected;*/


      var e = document.getElementById("VirtuosoVersion");
      var VirtuosoVersionSelected = e.options[e.selectedIndex].value;
      if(VirtuosoVersionSelected=="")
      {

        alert("please select version for Virtuoso");
      }
      VirtuosoVersionSelected="Virtuoso$"+VirtuosoVersionSelected;

      var TentrisArrayNumber=0;
      var VirtuosoArrayNumber=0;
      var FusekiArrayNumber=null;
      var triplestoreTentris=[];
      var triplestoreFuseki=[];
      var triplestoreVirtuso=[];

      for(var i=0; i<arr.length; i++){
        if(arr[i][0] == TentrisVersionSelected){
          TentrisArrayNumber=i;
          triplestoreTentris=arr[TentrisArrayNumber];
          triplestoreTentris=triplestoreTentris.slice(1,7);
        }else{
        if(arr[i][0] == VirtuosoVersionSelected){
          VirtuosoArrayNumber=i;
          triplestoreVirtuso=arr[VirtuosoArrayNumber];
          triplestoreVirtuso=triplestoreVirtuso.slice(1,7);

        }/*else
        if(arr[i][0] == FusekiVersionSelected){
          FusekiArrayNumber=i;
          triplestoreFuseki=arr[FusekiArrayNumber];
          triplestoreFuseki=triplestoreFuseki.slice(1,7);
        }*/
      }

      }

      console.log(FusekiArrayNumber+ "  "+ triplestoreFuseki);


      console.log(triplestoreVirtuso);



      generatebargraph(triplestoreTentris,triplestoreVirtuso,triplestoreFuseki);
      generateareagraph(triplestoreTentris,triplestoreVirtuso,triplestoreFuseki);
      generatelinegraph(triplestoreTentris,triplestoreVirtuso,triplestoreFuseki);

    }
    function generatebargraph(triplestoreTentris,triplestoreVirtuso,triplestoreFuseki)
    {


      var bar_chart = c3.generate({
        bindto: '#bar_chart',
        data: {
            columns: [
                triplestoreTentris,
                triplestoreVirtuso,
                //triplestoreFuseki
            ],
            type: 'bar'
        },
        title: {
          text: 'Bar-Chart'
        },
        size:{
          height:530,
          width:1300

        },
        axis: {
          x: {
            label: {

            text: 'Number of Clients',
            position: 'outer-center'
            },
            type: 'category',
            categories:  noofclients,
          },
          y: {
            label: {
              text: 'Average QPS per Client',
              position: 'outer-middle'
              }
          }
        },
        bar: {
            width: {
                ratio: 0.5 // this makes bar width 50% of length between ticks
            }
            // or
            //width: 100 // this makes bar width 100px
        },
        tooltip: {
          format: {
              title: function (d) { return "No of clients " + noofclients[d] },
          }
      }
    });


    }
    function generateareagraph(triplestoreTentris,triplestoreVirtuso,triplestoreFuseki)
    {
      var areachart = c3.generate({
        bindto:"#boxplot_chart",
        data: {
            columns: [
              triplestoreTentris,
              triplestoreVirtuso,
              //triplestoreFuseki
            ],
            types: {
              Tentris: 'area',
              //fuseki: 'area-spline',
              Virtuoso:'area-spline'
                // 'line', 'spline', 'step', 'area', 'area-step' are also available to stack
            },
            groups: [['Tentris','Virtuoso']]
        },
        title: {
          text: 'Area-Chart'
        },
        size:{
          height:530,
          width:1300

        },
        axis: {
          x: {
            label: {

            text: 'Number of Clients',
            position: 'outer-center'
            },
            type: 'category',
            categories:  noofclients,
          },
          y: {
            label: {
              text: 'Average QPS per Client',
              position: 'outer-middle'
              }
          }
        },
        tooltip: {
          format: {
              title: function (d) { return "No of clients " + noofclients[d] },
          }
      }

      });

    }
    function generatelinegraph(triplestoreTentris,triplestoreVirtuso,triplestoreFuseki)
    {
      var chart1 = c3.generate({
        bindto: '#line_chart',
        title: {
          text: 'Line-Chart'
        },
        data: {
            x: 'x',
            columns: [
                ['x', 1, 4, 8, 16, 32],
                triplestoreTentris,
                triplestoreVirtuso,
                //triplestoreFuseki

            ]
        },
        size:{
          height:530,
          width:1300

        },
        axis: {
          x: {
            label: {
            text: 'Number of Clients',
            position: 'outer-center'
            }
          },
          y: {
            label: {
              text: 'Average QPS',
              position: 'outer-middle'
              }
          }
        }
      });

    }
    function clearGraph()
    {
      window.location.reload()
    }

    get2dArray();


//parseData(createGraph);
