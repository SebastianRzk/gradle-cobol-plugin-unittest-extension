package de.sebastianruziczka.cobolunit.coverage.model

class CobolCoverageFile {

	private String name
	private List<CobolCoverageMethod> methods = new ArrayList<>()

	public CobolCoverageFile(String name) {
		this.name = name
	}

	public void addMethod(CobolCoverageMethod method) {
		this.methods.add(method)
	}


	@Override
	public String toString() {
		return 'CobolCoverageFile(' + this.name + ') { \n\t' + methods.join(',\n\t') + '\n}'
	}
}