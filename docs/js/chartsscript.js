/**
 * Collapse and show menu to view graphs on full screen
 */
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

/**
 * Set the properties of the graphs
 *
 * @param {String} temp - contains the graph which is to display
 */
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
    /**
     * Query to fetch the number of clients
     */
    var queryclient="SELECT  ?client "+
                    "WHERE {"+
                    "?query <http://iguana-benchmark.eu/properties/noOfWorkers> ?client ."+
                    "}";

    /**
     * Query to fetch the QPS
     */
    var queryavgqps="SELECT AVG( ?qps )"+
                    "WHERE {"+
                    "?query <http://iguana-benchmark.eu/properties/queriesPerSecond> ?qps ."+
                    "}";

    /**
     * Query to fetch the version number of dataset
     */
    var queryVersionNo="SELECT ?name "+
                    "{"+
                    "?query <http://iguana-benchmark.eu/properties/connection> ?name ."+
                    "}";


var counter;
/**
 * Generate the graphs based on CSV file
 *
 * @param {Function} createGraph - Generates the graphs
 */
function parseData(createGraph) {
	Papa.parse("http://131.234.28.165:3000/expected_csv.csv", {
		download: true,
		complete: function(results) {
			createGraph(results.data);
		}
	});
}

/**
 * Split the data into seperate arrays for every triple stores of each datasets
 * and set properties of the graphs based on the results
 * @param {Array} data - Results in form of 2D array
 */
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
  /**
   * Set properties of the bar chart
   */
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
    },
    tooltip: {
      format: {
          title: function (d) { return "No of clients " + noofclients[d] },
      }
  }
});

/**
 * Set properties of the boxplot for SWDF dataset
 */
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

/**
 * Set properties of the boxplot for DPpedia dataset
 */
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

/**
 * Set properties of the boxplot for WatDiv dataset
 */
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

/**
 * Set properties of the line for SWDF dataset
 */
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

/**
 * Set properties of the line chart for DBpedia
 */
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

/**
 * Set properties of the line chart for WatDiv dataset
 */
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
selectedVersions = []

/**
 * Download results as a csv file
 */
function downloadCsv(){
  window.open('../all_results.csv', 'Download');
  }

  /**
   * Download results as RDF file
   */
  function downloadRdf(){
    window.open('../results_task_2-1-1.nt', 'Download');
    }


  /**
   * Retrieve available datasets
   *
   * @param {String} datasetstring - Datasets available on the server
   */
  function datasets(datasetstring)
  {
    for(var i=0; i<=datasetstring.data.datasets.length-1; i++)
    {
      dataset_available[i]= datasetstring.data.datasets[i]["ds.name"];
    }

    getResults();


  /**
   * Run the query to get number of clients for all datasets
   */
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
          setTimeout(nextStep, 500);
        }
        else {
          arr.forEach(testData);
          deferred.resolve(i);
        }
    }
    nextStep();
    return deferred.promise();
  }


  /**
   * Runs query to get the version number
   *
   * @param {String} URL - Query converted into URL
   * @param {Number} datasetNo - Number of dataset
   */
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

  /**
   * Retrieve version number from the response and push it to the 2D array
   *
   * @param {Object} res - Response to the query to get verion number
   */
  function getVersion(res)
  {
    resultIs = res.data.results.bindings[0]["name"].value;
    version = resultIs.substring(resultIs.lastIndexOf('/') + 1);
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

  /**
   * Retrieve number of clients from the query response and run QPS query
   *
   * @param {Object} clients - Response to the client's query
   */
  async function getClient(clients){
    noOfClients = clients.data.results.bindings[0].client.value;

      var querystringforqps=connectionstring+dataset_available[counter]+postconnection+encodeURI(queryavgqps);

      axios({
        method: 'get',
        url: querystringforqps})
      .then(res => getQPS(res))
      .catch(err => console.log(err));
  }

  /**
   * get QPS and save it at the correct index in 2D array
   *
   * @param {Object} res - Response to the QPS query
   */
  function getQPS(res)
  {
      AQPS = res.data.results.bindings[0][".1"].value;
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

  /**
   * Retrieve the list of datasets from the server
   */
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

   /**
    * Checks if the data is completely loaded for all the triplestores
    * If not, it will run the queries again to fill the missing data
    *
    * @param {Array} item - Array containing data related to dataset
    * @param {Number} index - index of the array
    */
   function testData(item, index){
     error = false;
     for(var i=0; i<item.length; i++){
       if(item[i] == null){
         error = true;
       }
      }
      if(error == true){
        document.getElementById("myLoader").style.display = "flex"
        get2dArray();
        return;
      }
      else{
        document.getElementById("myLoader").style.display = "none"
      }
    }


  /**
 * Runs everytime a version of a triple store is selected
 */
function versionSelected() {
  selectedVersions.push(document.getElementById("AvailableVersions").value);
  var node = document.createElement("LI");
  var textnode = document.createTextNode(document.getElementById("AvailableVersions").value);
  var icon = document.createElement('i')
  icon.className = 'fa fa-trash-o de'
  node.appendChild(textnode);
  node.appendChild(icon);
  document.getElementById("myList").appendChild(node);
}

selectedTripleStores = [];
/**
 * Calls when the 'Submit' button is clicked by user
 * Display graphs for the selected versions
 */
function onSubmit() {
  for(var i=0; i<arr.length; i++){
    selectedVersions.forEach(element => {
      if(arr[i][0] == element){
        tripleStore=arr[i];
        tripleStore.splice(1,1);
        selectedTripleStores.push(tripleStore);
      }
    })
  }
  generatebargraph(selectedTripleStores);
  generateareagraph(selectedTripleStores);
  generatelinegraph(selectedTripleStores);
}

/**
 * Clears all selected versions of the triple stores
 */
function clearGraph(){
  selectedVersions = [];
  document.getElementById("myList").innerHTML = "";
}

  /**
   * Generates bar graph for the selected versions of dataset
   *
   * @param {Array} tripleStoreArray - Contains data related to Fuseki graph
   */
  function generatebargraph(tripleStoreArray)
    {

      var bar_chart = c3.generate({
        bindto: '#bar_chart',
        data: {
            columns: [
              tripleStoreArray[0],
              tripleStoreArray[1],
              tripleStoreArray[2]
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
        },
        tooltip: {
          format: {
              title: function (d) { return "No of clients " + noofclients[d] },
          }
      }
    });


    }

  /**
  * Generates area graph for the selected versions of dataset
  *
  * @param {Array} tripleStoreArray - Contains data related to Fuseki graph
  */
  function generateareagraph(tripleStoreArray)
    {
      var areachart = c3.generate({
        bindto:"#boxplot_chart",
        data: {
            columns: [
              tripleStoreArray[0],
              tripleStoreArray[1],
              tripleStoreArray[2]
            ],
            types: {
              tentris: 'area-spline',
              fuseki: 'area-spline',
              virtuoso:'area-spline',
              // 'line', 'spline', 'step', 'area', 'area-step' are also available to stack
            },
            groups: [['Tentris','Virtuoso', 'Fuseki']]
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

  /**
  * Generates line graph for the selected versions of dataset
  *
  * @param {Array} tripleStoreArray - Contains data related to Fuseki graph
  */
  function generatelinegraph(tripleStoreArray)
    {
      var chart1 = c3.generate({
        bindto: '#line_chart',
        title: {
          text: 'Line-Chart'
        },
        data: {
            x: 'x',
            columns: [
              ["x", 1, 2, 3, 4, 5],
              tripleStoreArray[0],
              tripleStoreArray[1],
              tripleStoreArray[2]
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

  /**
   * Reload the window to clear all the graphs
   */
  function clearGraph()
  {
    window.location.reload()
  }

  get2dArray();