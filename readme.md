# Automatic code generation in jCuda 
> A master degree project which goal is to provide a tool for the automatic generation of the parallel Java code.

### The manipulation of the code will be performed using the BCEL library
```sh
https://github.com/apache/commons-bcel
```

### JCuda
```sh
http://www.jcuda.org
```

### Main cases to study:
1. matrix multiplication
2. nbody
3. histogram
4. fft

### The repository contains projects/folders:
* jcuda
    * contains examples written in jcuda
* benchmarks
    * contains benchmarks, comparison of 3 different ways: sequential program, parallel program, jcuda program
* results
    * major reports in one place



### The project is created in the IntelliJ IDEA, which can be downloaded from the website:
```sh
 https://www.jetbrains.com/idea/download/
```

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