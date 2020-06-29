import { Component, OnInit, Input, ViewChild, ElementRef } from '@angular/core';
import axios from 'axios';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import * as c3 from 'c3';
import { min, max } from 'd3';
import { Options } from 'ng5-slider';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { KickoffBenchmarkingComponent } from '../kickoff-benchmarking/kickoff-benchmarking.component';
import { FormGroup } from '@angular/forms';


@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent implements OnInit {
  allchecked: boolean;
  selectedValues: any;

  constructor(public dialog: MatDialog) { }

  allDatasets="http://131.234.28.165:3030/$/datasets";
  connectionString="http://131.234.28.165:3030";
  postConnectionString="/sparql?query=";

  @ViewChild('verSelect', {static: false}) verSelect: ElementRef;
  @ViewChild('xAxis', {static: false}) xAxis: ElementRef;
  @ViewChild('yAxis', {static: false}) yAxis: ElementRef;
  @ViewChild('graph', {static: false}) graph: ElementRef;

  form: FormGroup;
  delay = ms => new Promise(res => setTimeout(res, ms));
  counter = 0;
  listOfAllDatasets=[]; //contains names of available datasets
  listOfAllVersionsURI=[];
  listOfAllVersionsDic = [];
  listOfUniqueVersions=[]; //contains all versions of triple stores available
  selectedVersions=[]; //containes versions selected by user
  selectedOptions=["x-axis", "y-axis", "graph"];
  listOfWorkers=["$1worker", "$4workers", "$8workers", "$16workers", "$32workers"];
  noOfClients=["1 client", "4 clients", "8 clients", "16 clients", "32 clients", "All"];
  queryId=[];  //contains all queryIds
  dataDictionary = {}; //contains all data
  selectedVersionsResultSize = [];
  selectedVersionsQueryTime = [];
  selectedVersionsNoOfWorkers = [];
  allVersionsSelectedData = [];
  metrices=["QPS", "Average QPS", "Query Time", "Average Query Time", "Average QMpH", "No. of Failed Queries", "Failed Reason"];

  displaySideMenu: Boolean = false;
  disPlayScatterPlot: Boolean = false;
  sliderMoved: Boolean = false;
  scatterChart;
  value = 30;
  highValue = 70;
  options: Options = {
    floor: 0,
    ceil: 100,
    vertical: true
    };


  queryForAllGraphs = "SELECT ?g { GRAPH ?g {} }";

  querySelectPart = "SELECT ?query ?property ?value \n"
  queryWherePartForVersionNames = "WHERE {" +
    "VALUES ?property { <http://iguana-benchmark.eu/properties/connection>" +
    "<http://iguana-benchmark.eu/properties/noOfWorkers>}" +

    "?query ?property ?value ." +
  "}"
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
   * @param {Object} response - javascript object containing response of to the version query
   * @param {String} dataset - dataset name of the current graph
   */
  getVersions(response, dataset){
    response.data.results.bindings.forEach(element => {
      this.listOfAllVersionsURI.push([element.g.value, dataset]);
    });
    this.counter--;
    if(this.counter == 0){
      this.runQueryForVersionNames();
    }
  }

  /**
   * Run query to get all the versions available of current triple store
   */
  runQueryForVersionNames() {
    this.listOfAllVersionsURI.forEach(element => {
      var queryFromPart = "FROM <" + element[0] + "> \n";
      var queryAllParts = this.querySelectPart + queryFromPart + this.queryWherePartForVersionNames;
      let queryForAllData = this.connectionString + element[1] + this.postConnectionString + encodeURIComponent(queryAllParts);

      axios({
        method: 'get',
        url: queryForAllData})
      .then(res => this.getAllVersions(res.data.results.bindings, element[0], element[1]))
      .catch(err => console.log(err));
    })
  }

  /**
   * Get list of all versions push them into listOfUniqueVersions
   *
   * @param {Array} data - response of queried data
   * @param {String} versionURI - Graph name of current version
   * @param {String} datasetName - Name of dataset to which this graph belongs
   */
  getAllVersions(data, versionURI, datasetName){
    var version = data[0].value.value.slice(data[0].value.value.lastIndexOf("/")+1);
    var worker = data[1].value.value;
    {
      if(worker == "1") this.listOfAllVersionsDic[version+this.listOfWorkers[0]] = [versionURI, datasetName];
      if(worker == "4") this.listOfAllVersionsDic[version+this.listOfWorkers[1]] = [versionURI, datasetName];
      if(worker == "8") this.listOfAllVersionsDic[version+this.listOfWorkers[2]] = [versionURI, datasetName];
      if(worker == "16") this.listOfAllVersionsDic[version+this.listOfWorkers[3]] = [versionURI, datasetName];
      if(worker == "32") this.listOfAllVersionsDic[version+this.listOfWorkers[4]] = [versionURI, datasetName];
    }
    if(this.listOfUniqueVersions.indexOf(version) == -1){
      this.listOfUniqueVersions.push(version);
    }
  }


  /**
   * Runs queries to get data for all selected versions of triple stores
   *
   * @param {Array} versions - Array of selected versions
   */
  populateDataDictionary(versions){
    var queryForAllData;
    versions.forEach(version => {
      this.listOfWorkers.forEach(worker => {
        let queryFromPart = "FROM <" + this.listOfAllVersionsDic[version+worker][0] + "> \n";
        let queryAllParts = this.querySelectPart + queryFromPart + this.queryWherePart;
        queryForAllData = this.connectionString + this.listOfAllVersionsDic[version+worker][1] +
                          this.postConnectionString + encodeURIComponent(queryAllParts);

        axios({
          method: 'get',
          url: queryForAllData})
        .then(res => this.sortData(res.data.results.bindings))
        .catch(err => console.log(err));
      });
    });
  }

  /**
   *sorts data with respect to the version and store them in the form of dictionary
   *
   * @param {Array} data - data in the form of array for each triple store
   */
  sortData(data){
    var queryMixes = []
    var arr4d = [];
    var arr3d = [];
    var version;
    var uniqueVersion;
    this.queryId.forEach(id => {
      var arr2d = [id, [], [], [], [], []]
      arr3d.push(arr2d);
    })

    data.forEach(item => {
      //if the response object contains connection string
      if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "connection") {
        uniqueVersion = item.value.value.slice(item.value.value.lastIndexOf("/")+1);
      }
      //if the response object contains QPS value
      else if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "queriesPerSecond") {
        var index = parseInt(item.query.value.slice(item.query.value.lastIndexOf("l")+1));
        arr3d[index][1].push(item.value.value)
      }
      //if the response object contains total time
      else if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "totalTime") {
        var index = parseInt(item.query.value.slice(item.query.value.lastIndexOf("l")+1));
        arr3d[index][2].push(item.value.value)
      }
      //if the response object contains number of failed queries
      else if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "failed") {
        var index = parseInt(item.query.value.slice(item.query.value.lastIndexOf("l")+1));
        arr3d[index][3].push(item.value.value)
      }
      //if the response object contains wrong codes
      else if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "wrongCodes") {
        var index = parseInt(item.query.value.slice(item.query.value.lastIndexOf("l")+1));
        arr3d[index][4].push(item.value.value)
      }
      //if the response object contains result size
      else if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "resultSize") {
        var index = parseInt(item.query.value.slice(item.query.value.lastIndexOf("l")+1));
        arr3d[index][5].push(item.value.value)
      }
      //if the response object contains queryMixes
      else if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "queryMixes") {
        queryMixes.push(item.value.value)
      }
      //if the response object contains number of workers
      else if(item.property.value.slice(item.property.value.lastIndexOf("/")+1) == "noOfWorkers") {
        var worker = item.value.value;
        if(worker == "1") version = uniqueVersion + this.listOfWorkers[0];
        if(worker == "4") version = uniqueVersion + this.listOfWorkers[1];
        if(worker == "8") version = uniqueVersion + this.listOfWorkers[2];
        if(worker == "16") version = uniqueVersion + this.listOfWorkers[3];
        if(worker == "32") version = uniqueVersion + this.listOfWorkers[4];
        arr4d.push(worker)
      }
    });

    arr4d.push(queryMixes);
    arr4d.push(arr3d);
    this.dataDictionary[version] = arr4d;
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
   * Wait until dataDictionary is completely populated with required data
   */
  waitForData = async () => {
    await this.delay(100);
    if(Object.keys(this.dataDictionary).length == this.selectedVersions.length*5){
      this.sortDataForGraphs();
    }
    else {
      this.waitForData();
    }
  };

  /**
   * When user clicks on 'Submit' button
   */
  onSubmit(){
    this.dataDictionary = {};
    this.populateDataDictionary(this.selectedVersions);
    this.waitForData();
  }

  /**
   * Sort the data in dataDictionary to display graphs
   */
  sortDataForGraphs(){

    var keys = [];
    var concatenated = [];
    var avgConcatenated;

    var allClientsData = [];

    var resultSize = [];
    var allResultSize = [];

    var queryTime = [];
    var allQueryTime = [];

    var workers = [];
    var allWorkers = [];

    this.allVersionsSelectedData = [];
    this.selectedVersionsResultSize = [];
    this.selectedVersionsQueryTime = [];
    this.selectedVersionsNoOfWorkers = [];

    //get data related to values selected on x-axis
    this.selectedVersions.forEach(version => {
      allClientsData[0] = version;
      allResultSize[0] = version+"_x"
      allQueryTime[0] = version+"_x"
      allWorkers[0] = version+"_x"
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
        this.listOfWorkers.forEach(worker => {
          keys.push(version+worker);
        });
      }

      if(keys){
        //Run for each NoOfClient of a version
        keys.forEach(keyz => {
          var data = this.dataDictionary[keyz];
          var indexAvgQmph = this.getIndex(this.metrices.indexOf(this.selectedOptions[1]));

          if(indexAvgQmph[2] == true){
            allClientsData.push(this.getAvg(data[1]));
          }
          else{
            //if queryID is selected
            if(selectedQueryID){
              concatenated = concatenated.concat(data[2][selectedQueryID][indexAvgQmph[0]])
              if(indexAvgQmph[1] == false){allClientsData.push(concatenated)}
            }
            else{
              if(this.selectedOptions[2] == "Scatter-Plot") {
                this.disPlayScatterPlot = true;
                data[2].forEach(queryid => {
                  for(var i = 0; i < queryid[indexAvgQmph[0]].length; i++) {
                    if(queryid[5][i] >= this.value && queryid[5][i] <= this.highValue) {
                      concatenated = concatenated.concat(queryid[indexAvgQmph[0]][i]);
                      resultSize = resultSize.concat(queryid[5][i]);
                      queryTime = queryTime.concat(queryid[2][i]);
                      if(data[0] == "1") workers.push(data[0] + " client");
                      else workers.push(data[0] + " clients")
                    }
                  }
                });
              }
              else {
                data[2].forEach(queryid => {
                  for(var i = 0; i < queryid[indexAvgQmph[0]].length; i++) {
                    if(queryid[5][i] >= this.value && queryid[5][i] <= this.highValue) {
                      concatenated = concatenated.concat(queryid[indexAvgQmph[0]][i]);
                      resultSize = resultSize.concat(queryid[5][i]);
                    }
                  }
                  this.disPlayScatterPlot = false;
                });
              }
              if(indexAvgQmph[1] == false) {
                allClientsData = allClientsData.concat(concatenated);
                allResultSize = allResultSize.concat(resultSize);
                allWorkers = allWorkers.concat(workers);
                if(this.selectedOptions[2] == "Scatter-Plot") {
                  allQueryTime = allQueryTime.concat(queryTime);
                }
              }
            }

            //if avg is true and aggregated is also true
            if(indexAvgQmph[1] == true){
              avgConcatenated = this.getAvg(concatenated);
              allClientsData.push(avgConcatenated);
              allResultSize.push(resultSize);
            }
          }
        });
      }
      if(allClientsData.length != 0){
        this.allVersionsSelectedData.push(allClientsData);
        this.selectedVersionsResultSize.push(allResultSize);
        this.selectedVersionsQueryTime.push(allQueryTime);
        this.selectedVersionsNoOfWorkers.push(allWorkers);
      }

      //empty all arrays before the next iteration start
      concatenated = [];
      avgConcatenated == null;
      allClientsData = [];
      keys = [];
      resultSize = [];
      allResultSize = []
      queryTime = [];
      allQueryTime = [];
      allWorkers = [];
    })

    if(!this.sliderMoved) this.getSliderMinMax();
    this.selectGraph();
  }

  /**
   * Select which graph needs to display and call its function
   */
  selectGraph() {
    switch(this.selectedOptions[2]){
      case "Bar-Chart":
        this.barGraph(this.allVersionsSelectedData, this.noOfClients);
        break;
      case "Line-Chart":
        this.lineGraph(this.allVersionsSelectedData, this.noOfClients);
        break;
      case "Area-Chart":
        this.areaGraph(this.allVersionsSelectedData, this.noOfClients);
        break;
      case "Scatter-Plot":
        this.scatterPlot('noOfClients');
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
    switch(index){
      case 0:
        indexOfMetrice = 1;
        break;
      case 1:
        indexOfMetrice = 1;
        avg = true;
        break;
      case 2:
        indexOfMetrice = 2;
        avg = true;
        break;
      case 3:
        indexOfMetrice = 3;
        break;
      case 4:
        indexOfMetrice = 1;
        qmph = true;
        break;
      case 5:
        indexOfMetrice = 3;
        break;
      case 6:
        indexOfMetrice = 4;
        break;
    }
    return [indexOfMetrice, avg, qmph]
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

/**
 * Generate bar graph for the data passed
 *
 * @param data
 * @param noOfclient
 */
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

  if(this.selectedOptions[1] == 'QPS') chart.axis.labels({x: 'Number Of Clients', y: 'Query Per Second'});
  else chart.axis.labels({x: 'Number Of Clients', y: this.selectedOptions[1]});

  data.forEach(col => {
    chart.load({
      columns: [
          col
      ]
  });
  })
  this.displaySideMenu = true;
  }


  /**
   * Generate scatter plot for the data passed
   *
   * @param test
   */
  scatterPlot(xAxisVariable) {

    //for number of clients the type of the x-axis should be category
    if(xAxisVariable == "noOfClients") {
      this.scatterChart = c3.generate({
        size: {
          height: 480,
          width: 1090
        },
        data: {
            xs: {
            },
            columns: [
                ],
            order: 'desc',
            type: 'scatter'
        },
        axis: {
          x: {
            label: {
              text: '',
              position: 'outer-right'
            },
              type: 'category',
              tick: {
                  fit: false
              }
          },
          y: {
            label: {
              text: '',
              position: 'outer-top'
            },
          }
      }

    });
    }
    //for other values the type of the x-axis should be normal
    else {
      this.scatterChart = c3.generate({
        size: {
          height: 480,
          width: 1090
        },
        data: {
            xs: {
            },
            columns: [
                ],
            order: 'desc',
            type: 'scatter'
        },
        axis: {
          x: {
              label: {
                text: '',
                position: 'outer-right'
              },
              tick: {
                  fit: false
              }
          },
          y: {
            label: {
              text: '',
              position: 'outer-top'
            },
          }
      }

    });
    }


  for(var i=0; i<this.allVersionsSelectedData.length; i++){

    //check id the selected radio button is Query Time
    if(xAxisVariable == "queryTime"){
      var x = this.allVersionsSelectedData[i][0];
      this.scatterChart.load({
        xs: {
          [this.allVersionsSelectedData[i][0]]: this.selectedVersionsQueryTime[i][0]
        },
        columns: [
          this.selectedVersionsQueryTime[i],
          this.allVersionsSelectedData[i]
        ]
      });
      this.scatterChart.axis.labels({x: 'Total Query Time', y: 'Query Per Second'});
    }

    //check id the selected radio button is No. Of Clients
    else if(xAxisVariable == "noOfClients"){
      var x = this.allVersionsSelectedData[i][0];
      this.scatterChart.load({
        xs: {
          [this.allVersionsSelectedData[i][0]]: this.selectedVersionsNoOfWorkers[i][0]
        },
        columns: [
          this.selectedVersionsNoOfWorkers[i],
          this.allVersionsSelectedData[i]
        ]
      });

      this.scatterChart.axis.labels({x: 'Number of Clients', y: 'Query Per Second'});
    }

    //check id the selected radio button is Results size
    else if(xAxisVariable == "resultSize") {
      var x = this.allVersionsSelectedData[i][0];
      this.scatterChart.load({
        xs: {
          [this.allVersionsSelectedData[i][0]]: this.selectedVersionsResultSize[i][0]
        },
        columns: [
          this.selectedVersionsResultSize[i],
          this.allVersionsSelectedData[i]
        ]
      });
      this.scatterChart.axis.labels({x: 'Result Size', y: 'Query Per Second'});
    }

  }
  this.displaySideMenu = true;
  }

  /**
   * Generate line graph for the data passed
   *
   * @param data
   * @param noOfclient
   */
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

  if(this.selectedOptions[1] == 'QPS') chart.axis.labels({x: 'Number Of Clients', y: 'Query Per Second'});
  else chart.axis.labels({x: 'Number Of Clients', y: this.selectedOptions[1]});

  //push all the columns one by one
  data.forEach(col => {
    chart.load({
      columns: [
          col
      ]
  });
  })
  this.displaySideMenu = true;
  }

  /**
   * Generate area graph for the data passed
   *
   * @param data
   * @param noOfclient
   */
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

  //select the labels of the graphs
  if(this.selectedOptions[1] == 'QPS') chart.axis.labels({x: 'Number Of Clients', y: 'Query Per Second'});
  else chart.axis.labels({x: 'Number Of Clients', y: this.selectedOptions[1]});

  //push all the columns one by one
  data.forEach(col => {
    chart.load({
      columns: [
          col
      ]
  });
  })
  chart.transform('area-spline');
  this.displaySideMenu = true;
  }


/**
 * Populate slider for the min and max value of result size
 */
getSliderMinMax(){
  var resultSize = [];
  this.selectedVersions.forEach((element) => {
    this.listOfWorkers.forEach(worker => {
      this.dataDictionary[element+worker][2].forEach(queryid => {
        resultSize.push(parseInt(queryid[5][0]));
      })
    })
  });

  this.value = min(resultSize);
  this.highValue = max(resultSize);
  var floor = min(resultSize);
  var ceil = max(resultSize);
  var opt: Options = {
    floor: floor,
    ceil: ceil,
    vertical: true
    };
    this.options = opt;
  }

  /**
   * Calls when slider value is changed
   */
  onChangeSlider = async () => {
    this.sliderMoved = true;
    this.sortDataForGraphs();
  }

  /**
   * refresh the window to delete all selected data
   */
  clearAll() {
    location.reload();
  }

  /**
   * Open kick off benchmarking dialog when button is clicked
   */
  RunBenchmarking() {
    let dialogRef = this.dialog.open(KickoffBenchmarkingComponent, {
      height: '370px',
      width: '600px',
    });
  }
}








