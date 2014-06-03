function View() {
	model.addObserver(this);
	curset = model.addInstructionSet("New", 3);
	curset.setLength(30);

	this.update = function() {
		$('#selects').html("");
		for(var i=0; i<curset.getLength(); i++) {
			$('#selects').append('<input type="range" id="select'+i+'" value="'+curset.states[i].getHeight()+'">');
		}
	}
}