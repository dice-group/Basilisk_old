import { Component, OnInit } from '@angular/core';
import axios from 'axios';
import { version } from 'punycode';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';

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


  queryForAllGraphs = "SELECT ?g { GRAPH ?g {} }";

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
    .then(res => this.getVersions(res))
    .catch(err => console.log(err));
  }

  /**
   * This function is called once for each dataset
   * Goes through each element of the object and extract version names
   *
   * @param response - json object containing response of to the version query
   */
  getVersions(response){
    response.data.results.bindings.forEach(element => {
      this.listOfAllVersions.push(element);
      if(response.data.results.bindings.indexOf(element)%5 == 1){
        var name = element.g.value;
        this.listOfUniqueVersions.push(name.slice(0, name.indexOf('$')));
      }
    });
    console.log(this.listOfUniqueVersions)
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

}


