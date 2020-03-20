# Automatic code generation in jCuda 
> A master degree project with target to provide a tool for the automatic generation of the parallel code cpu-gpu in java.

Continuation of the project:  <https://github.com/Matikul/autoparallel>

### The manipulation of the code will be performed by using the BCEL library
`commons-bcel`:  <https://github.com/apache/commons-bcel>


First attempt with BCEL are with generate static code. 
The next step will be a dynamic transformation of the existing code, like injecting one piece of code into another.


### JCuda

`jcuda`:  <http://www.jcuda.org>
`JCudaSamplesl`:  <https://github.com/jcuda/jcuda-samples/tree/master/JCudaSamples/src/main/java/jcuda>


### Main cases to study:
1. matrix multiplication
2. nbody
3. histogram
4. fft

### The repository contains projects/folders:
* bcel
    * examples with use BCEL
	In the folder with name 'generated' are outputs files with *.class extension. If you want to check if code generated with bcel works then you copy code into new file with *java extension. 
* benchmarks
    * contains benchmarks, comparison of 3 different ways: sequential program, parallel program, jcuda program
* jcuda
    * contains examples written in jcuda
* results
    * major reports in one place



### The project is created in the IntelliJ IDEA, which can be downloaded from the website:
`IntelliJIDEA`: <https://www.jetbrains.com/idea/download/>


### In the pom.xml file you can specify the version of jCuda you are using
```sh
<dependencies>
        <dependency>
            <groupId>org.jcuda</groupId>
            <artifactId>jcuda</artifactId>
            <version>0.9.1</version>
        </dependency>
        <dependency>
            <groupId>org.jcuda</groupId>
            <artifactId>jcublas</artifactId>
            <version>0.9.1</version>
        </dependency>
        ...
</dependencies>        
```

MIT