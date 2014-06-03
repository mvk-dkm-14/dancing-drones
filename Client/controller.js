function Controller() {
	model.addObserver(this);

	this.update = function () {
		$('input[type=range]').change(function () {
			curset.states[this.id.substring(6)].setHeight(this.value);
			console.log(curset.states[this.id.substring(6)].getHeight());
		});
	}
}

var view;
var controller;

$(document).ready(function () {
	view = new View();
	controller = new Controller();
	view.update();
	controller.update();
	model.loadFromStorage();
});