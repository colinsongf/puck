package puck.util

import breeze.linalg.DenseVector

/**
 * stuff from http://graphics.stanford.edu/~seander/bithacks.html
 *
 * @author dlwh
 **/
object BitHacks {

  private val logTable256 = new Array[Int](256)
  for(i <- 2 until 256) {
    logTable256(i) = 1 + logTable256(i/2)
  }

  def log2(v: Int) = {
    var tt = 0
    if ({tt = v >> 24; tt != 0}) {
      24 + logTable256(tt)
    } else if ({tt = v >> 16; tt != 0}) {
      16 + logTable256(tt)
    } else if ({tt = v >> 8; tt != 0}) {
      8 + logTable256(tt)
    } else {
      logTable256(v)
    }

  }


  def roundToNextPowerOfTwo(vv: Int) = {
    var v = vv - 1
    v |= v >> 1
    v |= v >> 2
    v |= v >> 4
    v |= v >> 8
    v |= v >> 16
    v += 1
    v
  }


  def asBitSet(col: DenseVector[Int]):java.util.BitSet = {
    val bitset = new java.util.BitSet()
    var i = 0
    var fsb = -1
    val cc = col.copy
    while(i < cc.length) {
      while(cc(i) != 0) {
        if(cc(i) < 0)
          fsb = 31
        else
          fsb = BitHacks.log2(cc(i))
        bitset.set(i * 32 + fsb)
        cc(i) &= ~(1<<fsb)
      }
      i += 1
    }

    bitset
  }

  def firstSetBit(col: DenseVector[Int]):Int = {
    var i = 0
    var fsb = -1
    while(i < col.length && fsb < 0) {
      if(col(i) < 0)
        fsb = 31
      else if(col(i) == 0)
        fsb = -1
      else
        fsb = BitHacks.log2(col(i))
      i += 1
    }

    if(fsb < 0) -1
    else ((i-1) * 32) + fsb
  }

  // we want to sort each bit vector so that the one with the greatest high order bit comes last
  def bitVectorLT(a: DenseVector[Int], b: DenseVector[Int]): Boolean = {
    OrderBitVectors.OrderingBitVectors.compare(a,b) < 0
  }

  object OrderBitVectors {
    implicit object OrderingBitVectors extends Ordering[DenseVector[Int]] {
      def compare(a: DenseVector[Int], b: DenseVector[Int]): Int = {
        var i = a.length - 1
        // stupid java and its lack of unsigned ints
        while(i >= 0) {
          val ai = a(i)
          val bi = b(i)
          if(ai != bi) {
            val al = (ai& ~(1<<31)).toLong | (ai >>> 31).toLong << 31
            val bl = (bi& ~(1<<31)).toLong | (bi >>> 31).toLong << 31
            return java.lang.Long.compare(al, bl)
          }
          i -= 1
        }
        0
      }
    }
  }
}

