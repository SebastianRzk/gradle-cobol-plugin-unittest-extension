package de.sebastianruziczka.cobolunit.coverage;

public enum CobolTraceMode {

	gnucobol1 {
		@Override
		int getLineNumberFor(String line, String followingLine) {
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
		boolean isParagraph(String line) {
			return line.contains("PROGRAM-ID:") && !line.contains("Line:");
		}

		@Override
		String parseParagraphName(String line) {
			return line.split(":")[line.split(":").length - 1].trim();
		}

	},
	gnucobol2

	{

		@Override
		int getLineNumberFor(String line, String followingLine) {
			if (line.length() < 70) {
				return -1;
			}
			return Integer.parseInt(line.substring(69).trim());
		}

		@Override
		boolean isParagraph(String line) {
			return "Paragraph:".equals(line.substring(29, 39));
		}

		@Override
		String parseParagraphName(String line) {
			return line.substring(40, 63).trim();
		}

	};

	public static CobolTraceMode getTraceModeFor(String line) {
		if (line.contains("Program-Id:")) {
			return CobolTraceMode.gnucobol2;
		}
		return CobolTraceMode.gnucobol1;
	}

	abstract int getLineNumberFor(String line, String followingLine);

	abstract boolean isParagraph(String line);

	abstract String parseParagraphName(String line);
}
