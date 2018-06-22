package de.sebastianruziczka.cobolunit.coverage;

public enum CobolTraceMode {

	gnucobol1 {
		@Override
		public int getLineNumberFor(String line, String followingLine) {
			if (this.isParagraph(line)) {
				return this.getNumber(followingLine) - 1;
			}
			return this.getNumber(line);
		}

		private int getNumber(String line) {
			if (!line.contains("Line:") | !line.contains("Statement")) {
				return -1;
			}
			String number = line.split("Line:")[1].split("Statement")[0].trim();
			return Integer.parseInt(number);
		}

		@Override
		public boolean isParagraph(String line) {
			return line.contains("PROGRAM-ID:") && !line.contains("Line:");
		}

		@Override
		public String parseParagraphName(String line) {
			return line.split(":")[line.split(":").length - 1].trim();
		}

		@Override
		public String parseProgrammID(String line) {
			if (this.isParagraph(line)) {
				return line.substring("PROGRAM-ID:".length()).split(":")[0].trim();
			}
			return line.substring("PROGRAM-ID:".length()).split("\t")[0].trim();
		}
	},
	gnucobol2or3 {
		@Override
		public int getLineNumberFor(String line, String followingLine) {
			if (line.length() < 70) {
				return -1;
			}
			return Integer.parseInt(line.split("Line:")[1].trim());
		}

		@Override
		public boolean isParagraph(String line) {
			return line.contains("Paragraph:");
		}

		@Override
		public String parseParagraphName(String line) {
			return line.split("Paragraph:")[1].split("Line:")[0].trim();
		}

		@Override
		public String parseProgrammID(String line) {
			return line.substring("Program-Id: ".length()).trim().split(" ")[0].trim();
		}
	};

	public static CobolTraceMode getTraceModeFor(String line) {
		if (line.contains("Program-Id:")) {
			return CobolTraceMode.gnucobol2or3;
		}
		return CobolTraceMode.gnucobol1;
	}

	public abstract int getLineNumberFor(String line, String followingLine);

	public abstract boolean isParagraph(String line);

	public abstract String parseParagraphName(String line);

	public abstract String parseProgrammID(String line);
}
