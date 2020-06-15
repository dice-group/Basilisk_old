import { Component, OnInit, Input, ViewChild } from '@angular/core';
import axios from 'axios';
import { version } from 'punycode';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import * as d3 from 'd3'
import * as c3 from 'c3';
import {FormControl} from '@angular/forms';
import { selectAll } from 'd3';
import { MatSelect } from '@angular/material/select';


@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent implements OnInit {
  allchecked: boolean;
  selectedValues: any;

  constructor() { }

  allDatasets="http://131.234.28.165:3030/$/datasets";
  connectionString="http://131.234.28.165:3030";
  postConnectionString="/sparql?query=";

  counter = 0;
  listOfAllDatasets=[]; //contains names of available datasets
  listOfAllVersions=[]; //contains all versions w.r.t noOfWorkers
  listOfUniqueVersions=[]; //contains all versions of triple stores available
  selectedVersions=[]; //containes versions selected by user
  selectedOptions=["x-axis", "y-axis", "graph"];
  listOfWorkers=["$1worker", "$4workers", "$8workers", "$16workers", "$32workers"];
  noOfClients=["1 client", "4 clients", "8 clients", "16 clients", "32 clients", "All"];
  queryId=[];  //contains all queryIds
  dataDictionary = {}; //contains all data
  result_size=[1, 2, 3]
  metrices=["QPS", "Avg QPS", "Avg query time", "No. of Failed queries", "QMpH",
            "QPS per query", "Avg QPS per query", "Avg query-time per query", "No. of Failed queries", "Failed Reason"];



  queryForAllGraphs = "SELECT ?g { GRAPH ?g {} }";

  querySelectPart = "SELECT ?query ?property ?value \n"
  queryWherePart = "WHERE {" +
    "VALUES ?property { <http://iguana-benchmark.eu/properties/connection>" +
    "<http://iguana-benchmark.eu/properties/noOfWorkers>" +
    "<http://iguana-benchmark.eu/properties/queriesPerSecond>" +
    "<http://iguana-benchmark.eu/properties/queryMixes>" +
    "<http://iguana-benchmark.eu/properties/totalTime>" +
    "<http://iguana-benchmark.eu/properties/failed>" +
    "<http://iguana-benchmark.eu/properties/wrongCodes>" +
    "<http://iguana-benchmark.eu/properties/resultSize>}" +

    "?query ?property ?value ." +
  "}"

  /**
   * This function calls imediately when the component is created
   * Gets list of datasets available on fuseki server
   */
  ngOnInit(): void {
    axios({
      method: 'get',
      url: this.allDatasets})
    .then(res => this.getDatasets(res))
    .catch(err => console.log(err));

    for(var i = 0; i <= 12; i++){ //populate queryId
      this.queryId.push("sparql" + i);
    }
  }

  /**
   * Get the datasets from the response object and store them into a seperate array
   *
   * @param {Object} response - json object containing details of the available datasets
   */
  getDatasets(response){
    this.counter = response.data.datasets.length;
    for(var i=0; i<response.data.datasets.length; i++){
      var datasetName = response.data.datasets[i]["ds.name"];
      this.listOfAllDatasets.push(datasetName);
      this.runVersionQuery(datasetName);
    }
  }

  /**
   * Run query to get the all versions of the dataset passed
   *
   * @param {String} dataset - Name of the dataset
   */
  runVersionQuery(dataset){
    var getVersionsQuery = this.connectionString + dataset + this.postConnectionString + encodeURIComponent(this.queryForAllGraphs);
    axios({
      method: 'get',
      url: getVersionsQuery})
    .then(res => this.getVersions(res, dataset))
    .catch(err => console.log(err));
  }

  /**
   * This function is called once for each dataset
   * Goes through each element of the object and extract version names
   *
   * @param response - json object containing response of to the version query
   */
  getVersions(response, dataset){
    response.data.results.bindings.forEach(element => {
      this.listOfAllVersions.push([element.g.value, dataset]);
      let name = element.g.value;
      let version = name.slice(0, name.indexOf('$'));
      if(!this.listOfUniqueVersions.includes(version)){
        this.listOfUniqueVersions.push(version);
      }
    });
    this.counter--;
    if(this.counter == 0){
      this.getAllData();
    }
  }

  /**
   * Runs queries to get data for all version of triple stores
   */
  getAllData(){
    this.listOfAllVersions.forEach(element => {
      var queryFromPart = "FROM <" + element[0] + "> \n";
      var queryAllParts = this.querySelectPart + queryFromPart + this.queryWherePart;
      let queryForAllData = this.connectionString + element[1] + this.postConnectionString + encodeURIComponent(queryAllParts);

      axios({
        method: 'get',
        url: queryForAllData})
      .then(res => this.sortData(res.data.results.bindings, element[0]))
      .catch(err => console.log(err));
    })
  }

  /**
   *sorts data with respect to the version and store them in the form of dictionary
   *
   * @param {Array} data - data in the form of array for each triple store
   * @param {String} element - contains name of version and no. of workers
   */
  sortData(data, element){

    var qm = []
    var arr4d = [];
    var arr3d = [];
    this.queryId.forEach(id => {
      var arr2d = [id, [], [], [], [], []]
      arr3d.push(arr2d);
    })

    data.forEach(item => {
      if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "queriesPerSecond") {
        var index = parseInt(item.query.value.slice(item.query.value.lastIndexOf("l")+1));
        arr3d[index][1].push(item.value.value)
      }
      else if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "totalTime") {
        var index = parseInt(item.query.value.slice(item.query.value.lastIndexOf("l")+1));
        arr3d[index][2].push(item.value.value)
      }
      else if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "failed") {
        var index = parseInt(item.query.value.slice(item.query.value.lastIndexOf("l")+1));
        arr3d[index][3].push(item.value.value)
      }
      else if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "wrongCodes") {
        var index = parseInt(item.query.value.slice(item.query.value.lastIndexOf("l")+1));
        arr3d[index][4].push(item.value.value)
      }
      else if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "resultSize") {
        var index = parseInt(item.query.value.slice(item.query.value.lastIndexOf("l")+1));
        arr3d[index][5].push(item.value.value)
      }
      else if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "queryMixes") {
        qm.push(item.value.value)
      }
      else if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "noOfWorkers") {
        arr4d.push(item.value.value)
      }
    });

    arr4d.push(qm);
    arr4d.push(arr3d);
    this.dataDictionary[element] = arr4d;
  }


  /**
   * Creates an array of selected versions
   *
   * @param {String} version - Version selected by user
   */
  versionSelected(version){
    this.selectedVersions.push(version);
  }


  /**
   * Delete the respective version from the list when any version is unchecked
   *
   * @param {String} version - version to be deleted
   */
  updateVersion(value, selected)
  {
    if(selected){
      this.selectedVersions.push(value);
    }
    else {
      const index = this.selectedVersions.indexOf(value);
      if (index > -1) {
        this.selectedVersions.splice(index, 1);
      }
    }
  }

  /**
   * Delete the version from the list when clicked on cross icon
   *
   * @param version
   */
  versionDeleted(version)
  {
   const index=this.selectedVersions.indexOf(version);
    if(index>-1)
     {
     this.selectedVersions.splice(index,1);
     }
  }

  /**
   * Changes the position of the selected versions in the array when an item is dragged
   *
   * @param {Event} event - event containing index of draged items
   */
  dragDrop(event: CdkDragDrop<string[]>){
    moveItemInArray(this.selectedVersions, event.previousIndex, event.currentIndex);
  }

  /**
   * Calls when user selects x-axis value
   *
   * @param {String} xValue - selected value
   */
  xAxisSelected(xValue){
    this.selectedOptions[0] = xValue;
  }

  /**
   * Calls when user selects y-axis value
   *
   * @param {String} yValue - selected value
   */
  yAxisSelected(yValue){
    this.selectedOptions[1] = yValue;
  }

  /**
   * calls when user selects graph to display
   *
   * @param {String} graph - selected value
   */
  graphSelected(graph){
    this.selectedOptions[2] = graph;
  }

  /**
   * When user clicks on 'Submit' button
   */
  onSubmit(){
    console.log(this.listOfAllDatasets)
    console.log(this.dataDictionary)
    console.log(this.listOfAllVersions)
    console.log(this.listOfUniqueVersions)

    //this.awain()
    var keys = [];
    var concatenated = [];
    var avgConcatenated;
    var avgNonAggregated = [];
    var allClientsData = [];
    var allVersionsData = [];

    //get data related to values selected on x-axis
    this.selectedVersions.forEach(version => {
      allClientsData[0] = version;
      if(this.selectedOptions[0].slice(2, 8) == "client" || this.selectedOptions[0].slice(3, 9) == "client"){
        var key = version + this.listOfWorkers[this.noOfClients.indexOf(this.selectedOptions[0])];
        keys.push(key);
      }
      else if(this.selectedOptions[0] == "All"){
        this.listOfWorkers.forEach(worker => {
          keys.push(version+worker);
        });
      }
      else if(this.selectedOptions[0].slice(0, 6) == "sparql"){
        var selectedQueryID = parseInt(this.selectedOptions[0].slice(6));
        console.log(selectedQueryID);
        this.listOfWorkers.forEach(worker => {
          keys.push(version+worker);
        });
      }

      if(keys){
        //Run for each client of a version
        keys.forEach(keyz => {
          var data = this.dataDictionary[keyz];

        var indexAggAvgQmph = this.getIndex(this.metrices.indexOf(this.selectedOptions[1]));

        if(indexAggAvgQmph[3] == true){
          allClientsData.push(data[1])
        }
        else{
          //if aggregated is true
        if(indexAggAvgQmph[1] == true){
          //if queryID is selected
          if(selectedQueryID){
            concatenated = concatenated.concat(data[2][selectedQueryID][indexAggAvgQmph[0]])
            if(indexAggAvgQmph[2] == false){allClientsData.push(concatenated)}
          }
          else{
            data[2].forEach(queryid => {
              concatenated = concatenated.concat(queryid[indexAggAvgQmph[0]])
            });
            if(indexAggAvgQmph[2] == false){allClientsData = allClientsData.concat(concatenated)}
          }

          //if avg is true and aggregated is also true
          if(indexAggAvgQmph[2] == true){
            avgConcatenated = this.getAvg(concatenated);
            allClientsData.push(avgConcatenated);
          }

        }
        //if aggregated is false
        else{
          if(selectedQueryID){
            concatenated = concatenated.concat(data[2][selectedQueryID][indexAggAvgQmph[0]])
            allClientsData.push(concatenated)
          }
          else{
            data[2].forEach(queryid => {
              concatenated.push(queryid[indexAggAvgQmph[0]]);
            });
            allClientsData.push(concatenated)
          }
          //if avg is true and aggregated is false
          if(indexAggAvgQmph[2] == true){

            concatenated.forEach(ana => {
              avgNonAggregated.push(this.getAvg(ana))
            })
          }
        }
        }
     })
    }
    if(allClientsData.length != 0){
      console.log(allClientsData);
      allVersionsData.push(allClientsData);
    }
    else if(avgNonAggregated.length != 0){
      console.log(avgNonAggregated)
    }
    else{
      console.log(concatenated)
    }

    concatenated = [];
    avgConcatenated == null;
    avgNonAggregated = [];
    allClientsData = [];
    keys = [];
  })

  switch(this.selectedOptions[2]){
    case "Bar-Chart":
      this.barGraph(allVersionsData, this.noOfClients);
      break;
    case "Line-Chart":
      this.lineGraph(allVersionsData, this.noOfClients);
      break;
    case "Area-Chart":
      this.areaGraph(allVersionsData, this.noOfClients);
      break;
    case "Scatter-Plot":
      break;
  }
  }

  /**
   * Gets the index of the required data
   *
   * @param {Number} index - index of the selected y-axis value
   */
  getIndex(index){
    var indexOfMetrice;
    var avg = false;
    var qmph = false;
    var aggregated: Boolean;
    switch(index){
      case 0:
        indexOfMetrice = 1;
        aggregated = true;
        break;
      case 1:
        indexOfMetrice = 1;
        aggregated = true;
        avg = true;
        break;
      case 2:
        indexOfMetrice = 2;
        aggregated = true;
        avg = true;
        break;
      case 3:
        indexOfMetrice = 3;
        aggregated = true;
        break;
      case 4:
        indexOfMetrice = 1;
        aggregated = true;
        qmph = true;
        break;
      case 5:
        indexOfMetrice = 1;
        aggregated = false;
        break;
      case 6:
        indexOfMetrice = 1;
        aggregated = false;
        avg = true;
        break;
      case 7:
        indexOfMetrice = 2;
        aggregated = false;
        avg = true;
        break;
      case 8:
        indexOfMetrice = 3;
        aggregated = false;
        break;
      case 8:
        indexOfMetrice = 4;
        aggregated = false;
        break;

    }
    return [indexOfMetrice, aggregated, avg, qmph]
  }

  /**
   * Calculates and returns average of an array
   * @param {Array} inputData - input array
   */
  getAvg(inputData) {
    var i = 0, sum = 0, len = inputData.length;
    while (i < len) {
        sum = sum + parseInt(inputData[i++]);
    }
    return sum / len;
  }


  barGraph(data, noOfclient){
    var clients = noOfclient.slice();

    var chart = c3.generate({
      size: {
        height: 480,
        width: 1090
      },
      data: {
          columns: [
          ],
          type: 'bar'
      },
      axis: {
        x: {
          label: {

          text: 'Number of Clients',
          position: 'outer-center'
          },
          type: 'category',
          categories: clients.splice(0, 5)
        },
        y: {
          label: {
            text: 'Average QPS',
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
      }
  });

  data.forEach(col => {
    chart.load({
      columns: [
          col
      ]
  });
  })

  }


  scatterPlot(test) {
    console.log(test)

    var chart = c3.generate({
      data: {
          xs: {
            hi: 'hi_x'

          },
          columns: [
            test[0],
            test[1]
              ],
          type: 'scatter'
      },
      axis: {
          x: {
              label: 'Sepal.Width',
              tick: {
                  fit: false
              }
          },
          y: {
              label: 'Petal.Width'
          }
      }
  });
  }

  lineGraph(data, noOfclient){
    var clients = noOfclient.slice();
    var chart = c3.generate({
      size: {
        height: 480,
        width: 1050
      },
      data: {
          columns: [
          ]
      },
      axis: {
        x: {
          label: {

          text: 'Number of Clients',
          position: 'outer-center'
          },
          type: 'category',
          categories: clients.splice(0, 5)
        },
        y: {
          label: {
            text: 'Average QPS',
            position: 'outer-middle'
            }
        }
      }
  });

  data.forEach(col => {
    chart.load({
      columns: [
          col
      ]
  });
  })
  }

  areaGraph(data, noOfclient){
    var clients = noOfclient.slice();
    var chart = c3.generate({
      size: {
        height: 480,
        width: 1050
      },
      data: {
          columns: [
          ]
      },
      axis: {
        x: {
          label: {
          text: 'Number of Clients',
          position: 'outer-center'
          },
          type: 'category',
          categories: clients.splice(0, 5)
        },
        y: {
          label: {
            text: 'Average QPS',
            position: 'outer-middle'
            }
        }
      }
  });

  data.forEach(col => {
    chart.load({
      columns: [
          col
      ]
  });
  })

  chart.transform('area-spline');
  }

}









