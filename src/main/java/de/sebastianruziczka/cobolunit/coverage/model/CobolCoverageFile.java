package de.sebastianruziczka.cobolunit.coverage.model;

import java.util.ArrayList;
import java.util.List;

public class CobolCoverageFile {

	private String name;
	private List<CobolCoverageMethod> methods = new ArrayList<>();

	public CobolCoverageFile(String name) {
		this.name = name;
	}

	public void addMethod(CobolCoverageMethod method) {
		this.methods.add(method);
	}

	public List<CobolCoverageMethod> methods() {
		return this.methods;
	}

	@Override
	public String toString() {
		return "CobolCoverageFile(" + this.name + ") { \n\t"
				+ this.methods.stream().map(Object::toString).reduce((y, x) -> x + ",\n\t" + y).orElse("") + "\n}";
	}
}