import { Component, OnInit } from '@angular/core';
import axios from 'axios';
import { version } from 'punycode';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { getLocaleDateFormat } from '@angular/common';
import { element } from 'protractor';

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
  dataInSelectedVersions=[];
  listOfWorkers=["$1worker", "$4workers", "$8workers", "$16workers", "$32workers"]
  dic = {};
  size = 0.0;


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
      this.runVersionQuery(datasetName, i);
    }
  }

  /**
   * Run query to get the all versions of the dataset passed
   *
   * @param {String} dataset - Name of the dataset
   */
  runVersionQuery(dataset, datasetNo){
    var getVersionsQuery = this.connectionString + dataset + this.postConnectionString + encodeURIComponent(this.queryForAllGraphs);
    axios({
      method: 'get',
      url: getVersionsQuery})
    .then(res => this.getVersions(res, dataset, datasetNo))
    .catch(err => console.log(err));
  }

  /**
   * This function is called once for each dataset
   * Goes through each element of the object and extract version names
   *
   * @param response - json object containing response of to the version query
   */
  getVersions(response, dataset, datasetNo){
    response.data.results.bindings.forEach(element => {
      this.listOfAllVersions.push([element.g.value, dataset]);
      if(response.data.results.bindings.indexOf(element)%5 == 1){
        var name = element.g.value;
        this.listOfUniqueVersions.push(name.slice(0, name.indexOf('$')));
      }
    });
    if(datasetNo == 2){
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
    this.dic[element] = data;
  }


  /**
   * Creates an array of selected versions
   *
   * @param {String} version - Version selected by user
   */
  versionSelected(version){
    this.selectedVersions.push(version);
    var allWrokersData = [version];
    this.listOfWorkers.forEach(worker => {
      allWrokersData.push(this.dic[version + worker]);
    })
    this.dataInSelectedVersions.push(allWrokersData);
    console.log(this.dataInSelectedVersions)
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


