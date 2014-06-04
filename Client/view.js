function View() {
	curset = model.addInstructionSet("New", 3);
	curset.setLength(60);
	curset.setDrones("A");

	this.update = function() {
		$('#selects').html("");
		for(var i=0; i<curset.getLength(); i++) {
			$('#selects').append('<input type="range" orient="vertical" id="select'+i+'" value="'+curset.states[i].getHeight()+'">');
		}
	};
}