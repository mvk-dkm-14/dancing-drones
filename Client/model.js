//JavaScript Document

/***************************************
Execute on start
***************************************/

// this is the instance of the main model
var model = new Model();
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

	this.getId = function() { return _id; }

	this.setHeight = function(height) {
		_height = height;
		model.notifyObservers();
	}

	this.getHeight = function() { return _height; }

	this.setComment = function(comment) {
		_comment = comment;
		model.notifyObservers();
	}

	this.getComment = function() { return _comment; }
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

	this.getId = function() { return _id; }

	this.setTitle = function(title) {
		_title = title;
		model.notifyObservers();
	}

	this.getTitle = function() { return _title; }

	this.setDrones = function(drones) {
		_drones = drones;
		model.notifyObservers();
	}

	this.getDrones = function() { return _drones; }

	/* SET THE NUMBER OF STATES OF THE SHOW, WHICH IS THE LENGTH IN SECONDS * 2 + 1 */
	this.setLength = function(seconds) {
		for(i = 0; i < seconds; i++) {
			this.states.push(new State(5, ""));
		}
	}

	/* SET THE NUMBER OF STATES OF THE SHOW, WHICH IS THE LENGTH IN SECONDS * 2 + 1 */
	this.getLength = function(seconds) { return (this.states.length-1)/2; }
}

/***************************************
Model
***************************************/

// this is the main model that holds information about all things.

function Model () {
	this.instructionsets = [];

	/* ADD AND REMOVE INSTRUCTION SETS */

	this.addInstructionSet = function (title, drones) {
		this.instructionsets.push(new InstructionSet(title, drones));
	}
	this.getInstructionSet = function (id) {
		for(i = 0; i < this.instructionsets.length; i++) {
			if(this.instructionsets[i].getId() === id) {
				return this.instructionsets[i];
			}
		}
		return -1;
	this.removeInstructionSet = function (id) {
		for(i = 0; i < this.instructionsets.length; i++) {
			if(this.instructionsets[i].getId() === id) {
				this.instructionsets[i] = null;
			}
		}
	}

	/* SEND INSTRUCTION SET TO SERVER */

	this.sendInstructionSet = function (id) {
		//TODO
	}

	/* CLEARING MODEL */
	this.clearModel = function () {
		this.instructionsets = [];

		this.notifyObservers();

		//TODO MORE
	}

	//*** OBSERVABLE PATTERN ***
	var listeners = [];
	
	this.notifyObservers = function (args) {
	    for (var i = 0; i < listeners.length; i++) {
		        listeners[i].update(args);
	    }
	};
	
	this.addObserver = function (listener) {
	    listeners.push(listener);
	};
	//*** END OBSERVABLE PATTERN ***
}