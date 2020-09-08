package histogram

import java.util.Arrays

import histogram.SerialHistogramScala.results


object SerialHistogramScala {

  private var data: Array[Float] = _

  private var results: Array[Float] = _

  def main(args: Array[String]): Unit = {
    val A: Array[Float] = ArrayUtil.randomFloatArray1D(10, 5)
    val serial: SerialHistogramScala = new SerialHistogramScala(A, 10)
    println(Arrays.toString(serial.getData))
    serial.calculate()
    println(Arrays.toString(serial.getResult))
  }

}

class SerialHistogramScala(data: Array[Float], N: Int) {

  SerialHistogramScala.data = data

  results = Array.ofDim[Float](N)

  def calculate(): Unit = {
    for (i <- 0 until data.length) {
      results(i) = data(i) + 1
    }
  }

  def getData(): Array[Float] = data

  def getResult(): Array[Float] = results

}
