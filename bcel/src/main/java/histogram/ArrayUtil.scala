package histogram

import scala.util.Random

object ArrayUtil {
  def randomFloatArray1D(N: Int, range: Int): Array[Float] = {
    val arr: Array[Float] = Array.ofDim[Float](N)
    val random: Random = new Random()
    for (i <- 0 until N) {
      arr(i) = random.nextInt(range)
    }
    arr
  }
}
