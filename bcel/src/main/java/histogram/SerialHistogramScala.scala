package histogram

import java.util.Arrays

object SerialHistogramScala {

  private var data: Array[Float] = _

  private var results: Array[Float] = _

  def main(args: Array[String]): Unit = {
    val A: Array[Float] = ArrayUtil.randomFloatArray1D(10, 5)
    val serial: SerialHistogramScala = new SerialHistogramScala(A, 10)
    println(Arrays.toString(getData))
    calculate()
    println(Arrays.toString(getResult))
  }

  def calculate(): Unit = {
    for (i <- 0 until data.length) {
      results(i) = data(i) + 1
    }
  }

  def getData(): Array[Float] = data

  def getResult(): Array[Float] = results

  class SerialHistogramScala(data2: Array[Float], N: Int) {
    data = data2
    results = Array.ofDim[Float](N)
  }

}


