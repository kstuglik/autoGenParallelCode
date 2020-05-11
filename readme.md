# Automatic code generation in jCuda 
> A master degree project with target to provide a tool for the automatic generation of the parallel code cpu-gpu in java.

Continuation of the project:  <https://github.com/Matikul/autoparallel>

### I use the BCEL library to operate on the byte code.
`commons-bcel`:  <https://github.com/apache/commons-bcel>


First attempt with BCEL are with generate static code. 
The next step will be a dynamic transformation of the existing code, like injecting one piece of code into another.


### JCuda

`jcuda`:  <http://www.jcuda.org> \
`JCudaSamplesl`:  <https://github.com/jcuda/jcuda-samples/tree/master/JCudaSamples/src/main/java/jcuda>


### Basic cases selected for the transformation:
1. matrix multiplication
2. nbody
3. histogram
4. fft


### The project consists of several parts:
* bcel
    * examples of byte code transformations
	* The generated file is also a *.class file and is available in the folder: generated
* benchmarks
    * contains benchmarks, comparison of 3 different ways: sequential program, parallel program, jcuda program
* jcuda
    * jcuda programs
* parallel
    * test using the fork-join framework (sequential version only)  




### The project was created in IntelliJ IDEA:
`IntelliJIDEA`: <https://www.jetbrains.com/idea/download/>


### Add to pom.xml file entries depending on used JCUDA libraries:
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

### If the above doesn't work:
```sh
An alternative solution to maven is to download the JCUDA library files yourself, to do so go to http://jcuda.org/downloads/downloads.html, to the download section and download the *.zip package. Unpack the content. In IDE, go to the project structure section and point to all/selected *.jar files. If you are asked, indicate that the imported libraries are for the project module with name: jcuda)
```

MIT