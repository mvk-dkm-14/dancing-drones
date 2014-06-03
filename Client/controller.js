function Controller() {
	this.update = function () {
		$('input[type=range]').change(function () {
			curset.states[this.id.substring(6)].setHeight(this.value);
			console.log(curset.states[this.id.substring(6)].getHeight());
		});
		$('#save').click(function () {
			model.saveToStorage();
		});
		$('#send').click(function () {
			model.sendInstructionSet();
		});
	};
}

var view;
var controller;

$(document).ready(function () {
	view = new View();
	model.addObserver(view);
	controller = new Controller();
	model.addObserver(controller);
	view.update();
	controller.update();
	model.loadFromStorage();
});