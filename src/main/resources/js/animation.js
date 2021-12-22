const width  = window.innerWidth || document.documentElement.clientWidth || 
document.body.clientWidth; 

var larghezza = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth; 

var x = 0; 
var velocita = 2;
var img = document.getElementById('animation');
var b = false;

var intervalId = window.setInterval(function(){

    document.getElementById("animation").style.left = x + "px";

    if(x + 250 > larghezza){
        velocita = -velocita;
    }

    if(x < 0){
        velocita = -velocita;
    }

    x += velocita;

}, 10)