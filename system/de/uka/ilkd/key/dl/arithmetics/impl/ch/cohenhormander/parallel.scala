package de.uka.ilkd.key.dl.arithmetics.impl.ch.cohenhormander;
//package cohenhormander;



final object Parallel {
  // parallel map
/*
  final def pmap[A,B](l: List[A], f: A => B): List[B] = {
    val buffer = new Array[B](l.length)
    val mappers =
      for(idx <- (0 until l.length).toList) yield {
        scala.actors.Futures.future {
//          println("done with a future!");
          buffer(idx) = f(l(idx));
      }
    }
    for(mapper <- mappers) mapper()
    buffer.toList
  }


  val cpus = Runtime.getRuntime().availableProcessors;

  import _root_.java.util.{Timer,TimerTask};
  import _root_.java.util.concurrent._;


  final def pmap2[A,B](l: List[A], f: A => B): List[B] = {
    val buffer = new Array[B](l.length)
    val e = Executors.newFixedThreadPool(cpus);
    (0 until l.length - 1).foreach(i => e.execute(new Runnable 
                                                  {def run = {buffer(i) = f(l(i))}}));
    e.shutdown;
    e.awaitTermination(Math.MAX_LONG, TimeUnit.SECONDS);
    buffer.toList
  }
*/


}
