//JavaScript Document

/***************************************
Execute on start
***************************************/

// this is the instance of the main model


var model = new Model();

//var ip = "130.229.143.104";
var ip = "localhost";
var mysocket = new WebSocket("ws://"+ip+":1337", "binary");
mysocket.onopen = function (evt) { var init = translate("00110000"); mysocket.send(init); };

var stateids = 0;
var instructionids = 0;

/***************************************
States
***************************************/

// This is the state constructor
// If you want to create a new state call
// var state = new State(height (int), "comment" (string));
function State(height, comment){

	var _id = stateids++; // int
	var _height = height; // int
	var _comment = comment; // String

	this.getId = function() { return _id; };

	this.setHeight = function(height) {
		_height = height;
		model.notifyObservers();
	};

	this.getHeight = function() { return _height; };

	this.setComment = function(comment) {
		_comment = comment;
		model.notifyObservers();
	};

	this.getComment = function() { return _comment; };
}


/***************************************
Instruction sets
***************************************/

// This is the Instruction set constructor
// If you want to create a new instruction set call
// var instructionset = new InstructionSet("title" (string), drones (int));
function InstructionSet(title, drones) {

	var _id = instructionids++; // int
	var _title = title; // String
	var _drones = drones; // int
	this.states = []; // array of state ids

	this.getId = function() { return _id; };

	this.setTitle = function(title) {
		_title = title;
		model.notifyObservers();
	};

	this.getTitle = function() { return _title; };

	this.setDrones = function(drones) {
		_drones = drones;
		model.notifyObservers();
	};

	this.getDrones = function() { return _drones; };

	/* SET THE NUMBER OF STATES OF THE SHOW, WHICH IS THE LENGTH IN SECONDS * 2 + 1 */
	this.setLength = function(seconds) {
		this.states = [];
		for(i = 0; i < seconds; i++) {
			this.states.push(new State(50, ""));
		}
	};

	/* GET THE NUMBER OF STATES OF THE SHOW, WHICH IS THE LENGTH IN SECONDS * 2 + 1 */
	this.getLength = function() { return this.states.length; };
}

/***************************************
Model
***************************************/

// this is the main model that holds information about all things.

function Model() {
	this.instructionsets = [];

	/* ADD AND REMOVE INSTRUCTION SETS */

	this.addInstructionSet = function (title, drones) {
		return this.instructionsets[this.instructionsets.push(new InstructionSet(title, drones))-1];
	};
	this.getInstructionSet = function (id) {
		for(i = 0; i < this.instructionsets.length; i++) {
			if(this.instructionsets[i].getId() === id) {
				return this.instructionsets[i];
			}
		}
		return -1;
	};
	this.removeInstructionSet = function (id) {
		for(i = 0; i < this.instructionsets.length; i++) {
			if(this.instructionsets[i].getId() === id) {
				this.instructionsets[i] = null;
			}
		}
	};

	/* SEND INSTRUCTION SET TO SERVER */

	this.connectToDrone = function () {
		var connect = translate("01001000");
		var unit = translate("1");
		mysocket.send(connect+unit);
	};

	this.droneTakeOff = function () {
		var takeoff = translate("01101001");
		var unit = translate("1");
		mysocket.send(takeoff+unit);
	};

	this.droneLand = function () {
		var land = translate("01101010");
		var unit = translate("1");
		mysocket.send(land+unit);
	};

	this.sendInstructionSet = function (id) {
		var unit = translate("1");
		var fillerbyte = translate("0");
		var gotoheight = translate("01101110");

		for(var i = 0; i < this.instructionsets[0].getLength(); i++)
			doSendDelayed(i, translate(this.instructionsets[0].states[i].getHeight().toString(2)));

		function doSendDelayed(i, myheight) {
			setTimeout(function () { mysocket.send(gotoheight+unit+fillerbyte+fillerbyte+fillerbyte+myheight);  }, 330 * i);
			setTimeout(function () { console.log(gotoheight+unit+fillerbyte+fillerbyte+fillerbyte+myheight);  }, 330 * i);
		}

	};

	/* CLEARING MODEL */
	this.clearModel = function () {
		this.instructionsets = [];

		this.notifyObservers();

		//TODO MORE
	};

	//*** OBSERVABLE PATTERN ***
	var listeners = [];
	
	this.notifyObservers = function (args) {
		for (var i = 0; i < listeners.length; i++) {
			listeners[i].update(args);
		}
	};

	this.saveToStorage = function () {
		if (Modernizr.localstorage) {
			var str = "[";
			//str += this.instructionsets[0].getLength()+",";
			var first = true;
			for(var i = 0; i < this.instructionsets[0].getLength(); i++) {
				if(first)
					first = false;
				else
					str += ",";
				str += this.instructionsets[0].states[i].getHeight();
			}
			str += "]";
			localStorage.setItem('Model', str);
		}
	};

	this.loadFromStorage = function () {
		if (Modernizr.localstorage) {
			var str = localStorage.getItem('Model');
			var arr = JSON.parse(str);
			this.instructionsets[0].setLength(arr.length);
			for(var i = 0; i < arr.length; i++) {
				this.instructionsets[0].states[i].setHeight(arr[i]);
				$('#select'+i).val(arr[i]);
			}
		}
	}

	this.getObservers = function () {
		return listeners;
	};

	this.addObserver = function (listener) {
		listeners.push(listener);
	};
	//*** END OBSERVABLE PATTERN ***
}

function translate (num){
	var bucket = parseInt(num, 2);
	bucket = String.fromCharCode(bucket);
	return bucket;
}