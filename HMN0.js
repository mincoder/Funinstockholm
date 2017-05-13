//google api key AIzaSyDGbsRaW3DFXkgN5wiYHsgHObHAxXgwxg4

document.write("<h1 id=\"buttonbuffer\"></h1><center id=\"holder\"><button onClick=\"onClick(true)\" id=\"button1\"><p style=\"font-size:5em;\">Outdoors</p></button><br><br><br><br><br><br><button onClick=\"onClick(false)\" id=\"button2\"><p style=\"font-size:5em;\">Indoors</p></button></center>");
var outside=true;
var english=true;
var loaded=false;
var adress="";
var jsonfromapi;
var destinations= new Array("","","","","","","","","","");
function onClick(os) {
  outside=os;
  if(!loaded) {
    var elem = document.getElementById('button1');
    elem.parentNode.removeChild(elem);
    var elem = document.getElementById('button2');
    elem.parentNode.removeChild(elem);
    var elem = document.getElementById('buttonbuffer');
    elem.parentNode.removeChild(elem);
  }
  document.getElementById("holder").innerHTML = "";
  if(english) {
    document.getElementById("holder").innerHTML = "<button id=\"text\" style=\"font-size:3em;\">Loading...<img width=\"100em\" height=\"100em\" src=\"loading.gif\"/></button>";
  } else {
    document.getElementById("holder").innerHTML = "<button id=\"text\" style=\"font-size:3em;\">Laddar...<img width=\"100em\" height=\"100em\" src=\"loading.gif\"/></button>";
  }
  if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(onLocation);
  } else {
    if(english) {
      alert("Geolocation is not supported by this browser. Please update your browser or switch browser to the current version of chrome.");
    } else {
      alert("Geolocation fungerar inte på din webbläsare. Var snäll och updatera din webbläsare eller installera den nyaste versionen av google chrome.");
    }
  }
  loaded=true;
//  $.get("test.php", { name:"Donald", town:"Ducktown" });
}

function onLocation(pos) {
  var latlon = pos.coords.latitude + "," + pos.coords.longitude;
  $.getJSON("https://funinstockholmapi.herokuapp.com/", { longitude:pos.coords.longitude, latitude:pos.coords.latitude, outside, english}, function(jsonresp){
    //https://www.google.com/maps/embed/v1/directions?key=YOUR_API_KEY&origin=Oslo+Norway&destination=Telemark+Norway&avoid=tolls|highways
  $.getJSON("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + pos.coords.latitude + "," + pos.coords.longitude + "&key=AIzaSyDGbsRaW3DFXkgN5wiYHsgHObHAxXgwxg4", function(jsonresp1){
    adress=jsonresp1.results[0].formatted_adress;
  });
  var glob_times = 0;
  for(var i=0;i<10;i++) {
    $.getJSON("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + jsonresp.List[i].Latitude + "," + jsonresp.List[i].Longitude + "&key=AIzaSyDGbsRaW3DFXkgN5wiYHsgHObHAxXgwxg4", function(jsonresp2){
      destinations[glob_times]=jsonresp2.results[0].formatted_address.replace(/ /g,"+").replace(/,/g,"");
      var t = destinations;
      if(glob_times==0) {
        document.getElementById("holder").innerHTML = "";
        document.getElementById("holder").innerHTML = document.getElementById("holder").innerHTML + "<table id=\"tab\" color=\"white\">";
      }
      //for(var j=0;j<10;j++) {
        document.getElementById("tab").innerHTML = document.getElementById("tab").innerHTML + "<tbody><tr id=\"button\"><td><a href=\"https://www.google.com/maps/place/" + t[glob_times] +"/\"><img width=\"160em\" height=\"160em\" src=\"data:image/png;base64,"+jsonresp.List[glob_times].Img+"\"/></a></td><td><h1>" + jsonresp.List[glob_times].Name + "</h1></td><td>" + jsonresp.List[glob_times].Description + "</td></tr></tbody></a>";
      //}
      if(glob_times==9) {
        document.getElementById("holder").innerHTML = document.getElementById("holder").innerHTML + "<table>";
      }
      jsonfromapi=jsonresp;
      glob_times++;
    });
  }
  });

}

function changeLang() {
  if(loaded) {
    onClick(outside);
  }
  if(english) {
    english=false;
    document.getElementById("minibutton").innerHTML = "Svenska";
    try {
      document.getElementById("button1").innerHTML = "<p style=\"font-size:5em;\">Utomhus</p>";
      document.getElementById("button2").innerHTML = "<p style=\"font-size:5em;\">Inomhus</p>";

    } catch(err) {

    }

  } else {
    english=true;
    document.getElementById("minibutton").innerHTML = "English";
    try {
      document.getElementById("button1").innerHTML = "<p style=\"font-size:5em;\">Outdoors</p>";
      document.getElementById("button2").innerHTML = "<p style=\"font-size:5em;\">Indoors</p>";

    } catch(err) {

    }
  }
}
