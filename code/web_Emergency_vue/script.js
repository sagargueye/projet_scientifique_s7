function init() {

  const CPELyon = {
    lat: 45.729566,
    lng: 4.827962
  };


  const zoomLevel = 10;


  const map = L.map('mapid').setView([CPELyon.lat, CPELyon.lng], zoomLevel);

  /****************   Layers(Fond de maps)    **************/

  /* wikimedia*/
  const mainLayer = L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}', {
    attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
    maxZoom: 18,
    minZoom : 8,
    id: 'mapbox/streets-v11',
    tileSize: 512,
    zoomOffset: -1,
    accessToken: 'pk.eyJ1IjoibWthYmEiLCJhIjoiY2tqNjg2bmtmMXZvZTJybGd3d2NlMGZldCJ9.bIm7cZwOxYnWn1GAHR0cNQ'
  });

  /* OpenStreeMap */
  const omsLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 18,
    minZoom : 8,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  });

  /* stadia */

  const Stadia_AlidadeSmoothDark = L.tileLayer('https://tiles.stadiamaps.com/tiles/alidade_smooth_dark/{z}/{x}/{y}{r}.png', {
    maxZoom: 18,
    minZoom : 8,
    attribution: '&copy; <a href="https://stadiamaps.com/">Stadia Maps</a>, &copy; <a  href="https://openmaptiles.org/">OpenMapTiles</a> &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors'
  });
  /* Fond de Map par defaut*/
  mainLayer.addTo(map);

  /* On va ajouter un controle layers pour pouoir switcher d'une map à l'autre, ce sera plus propre*/
  L.control.layers({
    'Main': mainLayer,
    'OMS Layer': omsLayer,
    'Stadia AlidadeSmoothDark': Stadia_AlidadeSmoothDark

  }).addTo(map);

  /**********  Icon Caserne ***********************/
  var myItems = [
    ["Caserne 1", 45.7623889, 4.8418176],
    ["Caserne 2", 45.76590347290039, 4.905092239379883],
    ["Caserne 3", 45.778784, 4.878317],
    ["Caserne 4", 45.7316551, 4.8284384],
    ["Cserne 5", 45.7458, 4.9709]
  ];

  for (var i = 0; i < myItems.length; i++) {
    var item = myItems[i];

    const caserneI = L.icon({
      iconUrl: 'images/caserne.png',
      iconSize: [30, 30],
      shadowSize: [50, 64],
      iconAnchor: [22, 94],
      shadowAnchor: [4, 62],
      popupAnchor: [-3, -76]
    });
    const caserneMarquer = L.marker([item[1], item[2]], {icon: caserneI}).addTo(map);
  }


  /*****************************Affichage la liste des camions  **************************/
 // setInterval(function () {
    var vhttp = new XMLHttpRequest();
    vhttp.onreadystatechange = function () {
      if (this.readyState == 4 && this.status == 200) {
        //Ici la reponse à la requette ajax doit etre au format d'un tableau de float contenant les coordonées où declancher l'incendie
        camioncoordo = this.responseText;
        //console.log(camioncoordo);
        //ya plus dincendi e, on vide le map
        //sauf quon bloque a le faire
        if(camioncoordo == "{}"){
          console.log("ya plus de camion en intervention");
          //L.layerGroup.clearLayers();
          return true;
        }
        camioncoordo = "[" + camioncoordo + "]";
        camioncoordo = JSON.parse(camioncoordo);
        var jsonObj = camioncoordo[0];
        Object.keys(jsonObj).forEach(function (key) {
          const camionI = L.icon({
            iconUrl: 'images/Camion.png',
            iconSize: [50, 50],
            shadowSize: [50, 64],
            iconAnchor: [22, 94],
            shadowAnchor: [4, 62],
            popupAnchor: [-3, -76]
          });
          const camionMarquer = L.marker([camioncoordo[0][key].latitude, camioncoordo[0][key].longitude], {icon: camionI}).addTo(map);
        });
      }
    };

    vhttp.open("GET", "http://localhost:8080/get_camions_intervenant_from_emergency/", true);
    vhttp.send();
 // }, 5000);


  /******* Affichage des incendies ***************/
  var simulateurButton = L.control({position: 'topleft'});

  // Creation du boutton de simulation, qui fait appel à ajax pour une requette vers le web service
  //setInterval(function() {
    simulateurButton.onAdd = (mapRef) =>
    {
      var button = L.DomUtil.create('button', 'geoloc-button');
      button.innerHTML = 'Simuler incendie';
      getListIncendies(map);
      button.onclick = () =>
      {
        getListIncendies(map);
      }
      return button;
    }
    simulateurButton.addTo(map);
  //},5000);
}
function getListIncendies(map ) {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function (even, data) {
    //console.log("incendies");
    if (this.readyState == 4 && this.status == 200) {
      //Ici la reponse à la requette ajax doit etre au format d'un tableau de float contenant les coordonées où declancher l'incendie
      incendie_string = this.responseText;
      //ya plus dincendi e, on vide le map
      //sauf quon bloque a le faire
      if(incendie_string == "{}"){
        console.log("ya plus dincendie");
        //L.layerGroup.clearLayers();
        return true;
      }
      incendie_string = "[" + incendie_string + "]";
      incendie_json = JSON.parse(incendie_string);
      var jsonObj = incendie_json[0];
      Object.keys(jsonObj).forEach(function (a) {
        if (incendie_json[0][a].fin_incendie === null) {
          //map.setView([incendie_json[0][a].latitude, incendie_json[0][a].longitude], 20);
          var circle = L.circle([incendie_json[0][a].latitude, incendie_json[0][a].longitude], (20 * incendie_json[0][a].intensite), {color: 'red'}).addTo(map).bindPopup('Alerte Incendie').openPopup();
          var marker = L.marker([incendie_json[0][a].latitude, incendie_json[0][a].longitude], {title: "Incendie"});
          marker.bindPopup(incendie_json[0][a].latitude, incendie_json[0][a].longitude);
          var location = L.layerGroup([circle, marker]);
          location.addTo(map);
        }
      });
    }
  };

  //on envoie notre requete pour recuperer les incendies active
  xhttp.open("GET", "http://127.0.0.1:8080/get_incendie_from_emergency/", true);
  xhttp.send();
}

