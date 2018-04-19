set -e

echo ">>>>>>> creating test-repository"
cd endToEndTest
cd gradle-cobol-plugin
git pull
cd ..
cd gradle-cobol-plugin-example
git pull
cd ..
echo "<<<<<<<"

echo ">>>>>>> create local repo for this jar"
rm -rvf ./repo
cd ..
gradle publish
mv ../repo endToEndTest/repo
echo "<<<<<<<"

echo ">>>>>>> create local repo for gradle plugin jar"
cd endToEndTest/gradle-cobol-plugin
gradle publish
cd ..
cd ..
echo "<<<<<<<"


echo ">>>>>>> prepare test-repository"
rm -v endToEndTest/gradle-cobol-plugin-example/settings.gradle
cp endToEndTest/gradle-cobol-plugin-example/ci/local_repo_test_settings.gradle endToEndTest/gradle-cobol-plugin-example/settings.gradle
rm -v endToEndTest/gradle-cobol-plugin-example/build.gradle
cp ci/build.gradle.replacement endToEndTest/gradle-cobol-plugin-example/build.gradle
echo "<<<<<<<"

echo ">>>>>>> exec test"
cd endToEndTest/gradle-cobol-plugin-example
gradle cobolCheck
echo "<<<<<<<"
