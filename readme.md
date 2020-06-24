# Automatic code generation in jCuda 
> A master thesis project for the automatic generation of the parallel cpu-gpu in java. 
> 
> Dynamic transformation of the existing code, like injecting one piece of code into another. 
> 
> Baseline project:  <https://github.com/Matikul/autoparallel>

----


## Basic cases selected for the transformation:
1. matrix multiplication
2. nbody
3. histogram
4. fft


## The project consists of several parts:
* bcel
  * intro into bcel issue
  * bytecode transformation examples
  * *.class files
  * core part of the project
* benchmarks
  * compare different ways of doing things: 
    * sequential program, 
    * parallel program, 
    * jcuda program


----


## BCEL library
`commons-bcel`:  <https://github.com/apache/commons-bcel>


## JCuda
`jcuda`:  <http://www.jcuda.org> 

`JCudaSamplesl`:  <https://github.com/jcuda/jcuda-samples/tree/master/JCudaSamples/src/main/java/jcuda>


## The project was created in IntelliJ IDEA:
`IntelliJIDEA`: <https://www.jetbrains.com/idea/download/>


## maven dependencies:
```sh
<dependencies>
        <dependency>
          <groupId>org.apache.bcel</groupId>
          <artifactId>bcel</artifactId>
          <version>6.5.0</version>
        </dependency>
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