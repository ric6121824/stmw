# Run this bash script from inside the folder /stmw/sample_files/

# Compile searcher
javac SampleSearcher.java

# Run searcher (permitting restricted native access features and enable optimization via vectorization (optional parameters))
java --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector SampleSearcher "the keeper" 10
