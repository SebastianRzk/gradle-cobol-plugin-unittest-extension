package de.sebastianruziczka.cobolunit.coverage.sourcefilereader

enum IgnoredToken {
	ELSE('ELSE'), ENDIF('END-IF.')

	private String tokenString;
	IgnoredToken(String tokenString){
		this.tokenString = tokenString
	}
	public static boolean matchAny(String line) {
		String trimmedLine = line.replaceAll("\\s","")
		for (IgnoredToken token : IgnoredToken.values()) {
			if (token.tokenString.equals(trimmedLine)) {
				return true
			}
		}
		return false
	}
}
