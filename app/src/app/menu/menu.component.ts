import { Component, OnInit } from '@angular/core';
import axios from 'axios';
import { version } from 'punycode';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import * as d3 from 'd3'

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent implements OnInit {

  constructor() { }

  allDatasets="http://131.234.28.165:3030/$/datasets";
  connectionString="http://131.234.28.165:3030";
  postConnectionString="/sparql?query=";

  listOfAllDatasets=[];
  listOfAllVersions=[];
  listOfUniqueVersions=[];
  selectedVersions=[];
  selectXaxis=[1, 4, 8];
  selectYaxis= ["qps", "qmph"]



  queryForAllGraphs = "SELECT ?g { GRAPH ?g {} }";

  querySelectPart = "SELECT ?query ?property ?value \n"
  queryWherePart = "WHERE {" +
    "VALUES ?property { <http://iguana-benchmark.eu/properties/connection>" +
    "<http://iguana-benchmark.eu/properties/noOfWorkers>" +
    "<http://iguana-benchmark.eu/properties/queriesPerSecond>" +
    "<http://iguana-benchmark.eu/properties/queryMixes>" +
    "<http://iguana-benchmark.eu/properties/totalTime>" +
    "<http://iguana-benchmark.eu/properties/failed>" +
    "<http://iguana-benchmark.eu/properties/wrongCodes>}" +

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
  }

  /**
   * Get the datasets from the response object and store them into a seperate array
   *
   * @param {Object} response - json object containing details of the available datasets
   */
  getDatasets(response){
    for(var i=0; i<response.data.datasets.length; i++){
      var datasetName = response.data.datasets[i]["ds.name"];
      this.listOfAllDatasets.push(datasetName);
      this.runVersionQuery(datasetName);
    }
    console.log(this.listOfAllDatasets);
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
      if(response.data.results.bindings.indexOf(element)%5 == 1){
        var name = element.g.value;
        this.listOfUniqueVersions.push(name.slice(0, name.indexOf('$')));
      }
    });
    //this.getAllData();
  }

  getAllData(){
    this.listOfAllVersions.forEach(element => {
      var queryFromPart = "FROM <" + element[0] + "> \n";
      var queryAllParts = this.querySelectPart + queryFromPart + this.queryWherePart;
      let queryForAllData = this.connectionString + element[1] + this.postConnectionString + encodeURIComponent(queryAllParts);
      console.log(queryForAllData);

      axios({
        method: 'get',
        url: queryForAllData})
      .then(res => console.log(res))
      .catch(err => console.log(err));
    })
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
   * Deletes the respective version from the list when clicked on cross icon
   *
   * @param {String} version - version to be deleted
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

}





