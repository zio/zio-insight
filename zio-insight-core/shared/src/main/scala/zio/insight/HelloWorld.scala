package zio.insight

object HelloWorld:
  def main(args: Array[String]) = count

  private def count =
    0.to(10).foreach(i => println(s"Counting ... $i"))
end HelloWorld
