var datasetsstring="http://131.234.28.165:3030/$/datasets";
var index=0;
var available_version=[];

/**
 * Get all the datasets from the fuseki server
 */
function checkdropbox()
{


    axios({
        method: 'get',
        url: datasetsstring})
      .then(res => getdetails(res))
      .catch(err => console.log(err));

}

/**
 * Stores versions of Tentris, Virtuoso and Fuseki into seperate arrays
 *
 * @param {object} dataset - contains all datasets available
 */
async function getdetails(dataset)
{
    for(var i=0; i<=dataset.data.datasets.length-1; i++)
    {
      dataset_available[i]= dataset.data.datasets[i]["ds.name"];
    }
    for(var i=0;i<dataset_available.length;i++)
    {
        var querystringforVersion=connectionstring+dataset_available[i]+postconnection+encodeURI(queryVersionNo);

      versionQueryResponse = await axios({
        method: 'get',
        url: querystringforVersion})
      .then(res => {return res})
      .catch(err => console.log(err));

      getversions(versionQueryResponse);

    }
    getbuttons();
}


/**
 * Pushes the new version into array of versions
 *
 * @param {object} res - response of the version query
 */
function getversions(res)
{
    resultIs = res.data.results.bindings[0]["name"].value;
    version = resultIs.substring(resultIs.lastIndexOf('/') + 1)
    for(var i=0; i<available_version.length; i++){
        if(available_version[i] == version){    //check if version already exists in 2D array
        return;
        }
    }
    available_version.push(version);
}

/**
 * Sets the dropdown button to select versions of Tentris, Fuseki and Virtuoso
 */
function getbuttons()
{
    var select = document.getElementById("AvailableVersions");
    for(var i = 0; i <available_version.length; i++) {
        var option = document.createElement('option');
        option.text = option.value = available_version[i];
        select.add(option, 0);
    }
}

checkdropbox();
