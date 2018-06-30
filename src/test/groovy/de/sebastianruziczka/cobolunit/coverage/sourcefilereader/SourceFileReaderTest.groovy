package de.sebastianruziczka.cobolunit.coverage.sourcefilereader;

import static de.sebastianruziczka.cobolunit.coverage.model.CoverageStatus.not_passed
import static de.sebastianruziczka.cobolunit.coverage.model.CoverageStatus.passed
import static org.assertj.core.api.Assertions.assertThat
import static org.junit.Assert.*

import org.junit.Test

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.api.CobolCodeType
import de.sebastianruziczka.api.CobolSourceFile
import de.sebastianruziczka.cobolunit.CobolUnitSourceFile
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageLine
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageMethod
import de.sebastianruziczka.cobolunit.coverage.model.CoverageStatus

class SourceFileReaderTest {

	private String simpleFile() {
		return """      ******************************************************************
      * Author:
      * Date:
      * Purpose:
      * Tectonics: cobc
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. Main.
       DATA DIVISION.
       FILE SECTION.
       WORKING-STORAGE SECTION.
       01  WS-NAME PIC X(4).
       01  WS-GREETING-RESULT PIC X(10).
       01  WS-GREETING PIC X(6) VALUE 'HELLO '.
       PROCEDURE DIVISION.
       2000-COMPUTE-GREETING.
            DISPLAY WS-GREETING-RESULT.
            DISPLAY WS-GREETING-RESULT.
            DISPLAY WS-GREETING-RESULT.
       END PROGRAM Main."""
	}



	@Test
	public void test_readFile_shouldReadSimpleMethod(){
		SourceFileReader component_under_test = new SourceFileReaderForTest(this.simpleFile())

		CobolCoverageFile result = component_under_test.read(this.fileStub())
		CobolCoverageMethod resultMethod = result.methods().get(0)

		assertThat(result.methods().size()).isEqualTo(1)
		assertThat(resultMethod.name()).isEqualTo("2000-COMPUTE-GREETING")
		assertLines(resultMethod, 17, 18, 19)
		assertStatus(resultMethod, not_passed, not_passed, not_passed)
	}



	private String fileWithComment() {
		return """      ******************************************************************
      * Author:
      * Date:
      * Purpose:
      * Tectonics: cobc
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. Main.
       DATA DIVISION.
       FILE SECTION.
       WORKING-STORAGE SECTION.
       01  WS-NAME PIC X(4).
       01  WS-GREETING-RESULT PIC X(10).
       01  WS-GREETING PIC X(6) VALUE 'HELLO '.
       PROCEDURE DIVISION.
       2000-COMPUTE-GREETING.
            DISPLAY WS-GREETING-RESULT.
       *>   DISPLAY WS-GREETING-RESULT.
            DISPLAY WS-GREETING-RESULT.
       END PROGRAM Main."""
	}



	@Test
	public void test_readFile_shouldIgnoreComment(){
		SourceFileReader component_under_test = new SourceFileReaderForTest(this.fileWithComment())

		CobolCoverageFile result = component_under_test.read(this.fileStub())
		CobolCoverageMethod resultMethod = result.methods().get(0)

		assertThat(result.methods().size()).isEqualTo(1)
		assertThat(resultMethod.name()).isEqualTo("2000-COMPUTE-GREETING")
		assertLines(resultMethod, 17, 19)
		assertStatus(resultMethod, not_passed, not_passed)
	}


	private String fileWithFollowLine() {
		return """      ******************************************************************
      * Author:
      * Date:
      * Purpose:
      * Tectonics: cobc
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. Main.
       DATA DIVISION.
       FILE SECTION.
       WORKING-STORAGE SECTION.
       01  WS-NAME PIC X(4).
       01  WS-GREETING-RESULT PIC X(10).
       01  WS-GREETING PIC X(6) VALUE 'HELLO '.
       PROCEDURE DIVISION.
       2000-COMPUTE-GREETING.
            DISPLAY WS-GREETING-RESULT
       -    SOMETHING OTHER.
            DISPLAY WS-GREETING-RESULT.
       END PROGRAM Main."""
	}


	@Test
	public void test_readFile_shouldParseFollowLine_withOnlyNotPassed(){
		SourceFileReader component_under_test = new SourceFileReaderForTest(this.fileWithFollowLine())

		CobolCoverageFile result = component_under_test.read(this.fileStub())
		CobolCoverageMethod resultMethod = result.methods().get(0)

		assertThat(result.methods().size()).isEqualTo(1)
		assertThat(resultMethod.name()).isEqualTo("2000-COMPUTE-GREETING")
		assertLines(resultMethod, 17, 18, 19)
		assertStatus(resultMethod, not_passed, not_passed, not_passed)
	}

	@Test
	public void test_readFile_shouldParseFollowLine_withOneLinePassed_shouldFollow(){
		SourceFileReader component_under_test = new SourceFileReaderForTest(this.fileWithFollowLine())

		CobolCoverageFile result = component_under_test.read(this.fileStub())
		CobolCoverageMethod resultMethod = result.methods().get(0)
		resultMethod.setLineCoveredWithRelativeIndex(0)

		assertThat(result.methods().size()).isEqualTo(1)
		assertThat(resultMethod.name()).isEqualTo("2000-COMPUTE-GREETING")
		assertLines(resultMethod, 17, 18, 19)
		assertStatus(resultMethod, passed, passed, not_passed)
	}


	private String fileWithCondition() {
		return """      ******************************************************************
      * Author:
      * Date:
      * Purpose:
      * Tectonics: cobc
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. Main.
       DATA DIVISION.
       FILE SECTION.
       WORKING-STORAGE SECTION.
       01  WS-NAME PIC X(4).
       01  WS-GREETING-RESULT PIC X(10).
       01  WS-GREETING PIC X(6) VALUE 'HELLO '.
       PROCEDURE DIVISION.
       2000-COMPUTE-GREETING.
           DISPLAY WS-GREETING-RESULT
           IF WS-NAME = WS-ADMIN-NAME THEN
              DISPLAY 'HELLO ADMIN'
              DISPLAY ' WELCOME TO YOUR WORLD!'
           ELSE
              DISPLAY 'YOU ARE NOT ADMIN.'
              DISPLAY ' THIS IS SAD :('
           END-IF.
           DISPLAY WS-GREETING-RESULT.
       END PROGRAM Main."""
	}

	@Test
	public void test_readFile_shouldIgnoreELSEAndENDIF(){
		SourceFileReader component_under_test = new SourceFileReaderForTest(this.fileWithCondition())

		CobolCoverageFile result = component_under_test.read(this.fileStub())
		CobolCoverageMethod resultMethod = result.methods().get(0)

		assertThat(result.methods().size()).isEqualTo(1)
		assertThat(resultMethod.name()).isEqualTo("2000-COMPUTE-GREETING")
		assertLines(resultMethod, 17, 18, 19, 20, 22, 23, 25)
	}



	private void assertLines(CobolCoverageMethod method, int... lines) {
		List<CobolCoverageLine> methodStatus =  method.methodStatus()
		for (int i = 0 ; i< lines.length; i++) {
			assertThat(methodStatus.get(i).lineNumber()).isEqualTo(lines[i])
		}
	}

	private void assertStatus(CobolCoverageMethod method, CoverageStatus... lines) {
		List<CobolCoverageLine> methodStatus =  method.methodStatus()
		for (int i = 0 ; i< lines.length; i++) {
			assertThat(methodStatus.get(i).status()).isEqualTo(lines[i])
		}
	}

	CobolUnitSourceFile fileStub() {
		return new CobolUnitSourceFile(new CobolSourceFileStub(), null, null, null)
	}
}
class CobolSourceFileStub extends CobolSourceFile{
	public CobolSourceFileStub(){
		super(new CobolExtension(), "MAINUT")
	}

	@Override
	public String getAbsolutePath(CobolCodeType type) {
		return ''
	}
}

class SourceFileReaderForTest extends SourceFileReader{
	private String[] fileContent
	public SourceFileReaderForTest(String content) {
		super(new CobolExtension())
		this.fileContent = content.split('\n')
	}

	@Override
	public String[] fileContent(String s) {
		return this.fileContent
	}
}
