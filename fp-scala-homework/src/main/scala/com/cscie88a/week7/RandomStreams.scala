package com.cscie88a.week7

import scala.util.Random
import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import cats.implicits._
import fs2.{io, text, Stream, Pure}

// domain classes
case class Student(id: Int, name: String)
case class StudentScore(id: Int, name: String, score: Int)

// class to hold random state
// (S, A) => (S, A)
case class RandomGen[A](gen: Random, next: A)

object RandomGen {

  // a random stream of integers
  def randomInts0(seed: Int): Stream[Pure, Int] = {
    val zero: RandomGen[Int] = RandomGen(new Random(seed), 0)
    val streamIntStates: Stream[Pure, RandomGen[Int]] = Stream.iterate(zero) {rg =>
      RandomGen(rg.gen, rg.gen.nextInt)
    }
    val intStream = streamIntStates.map(_.next).tail
    intStream
  }

  // a random stream of strings
  def randomStrings0(seed: Int): Stream[Pure, String] = {
    val zero: RandomGen[String] = RandomGen(new Random(seed), "")
    val streamStringStates: Stream[Pure, RandomGen[String]] = Stream.iterate(zero) {rg =>
      RandomGen(rg.gen, rg.gen.nextString(5))
    }
    val strStream = streamStringStates.map(_.next).tail
    strStream
  }

  // generic random value stream generator using Higher Order Functions
  def randomVal[A](seed: Int, zero: A, f: (Random) => A): Stream[Pure, A] = {
    val genZero: RandomGen[A] = RandomGen(new Random(seed), zero)
    val streamValueStates: Stream[Pure, RandomGen[A]] = Stream.iterate(genZero) {rg =>
      RandomGen(rg.gen, f(rg.gen))
    }
    streamValueStates.map(_.next).tail
  }

  // application of Higher Order Function created above to create new random streams
  def randomInts(seed: Int): Stream[Pure, Int] = randomVal(seed, 0, _.nextInt)

  def randomStrings(seed: Int): Stream[Pure, String] = randomVal(seed, "", _.nextString(5))

  def randomScores(seed: Int): Stream[Pure, Int] = randomVal(seed, 0, _.nextInt(100))

  // combining complex streams from simpler streams
  def randomIntStrPairs(seed: Int): Stream[Pure, (Int, String)] =
    randomInts(seed).zip(randomStrings(seed))

  // creating infinite streams of domain classes e.g, students, sensor data, etc
  def randomStudents(seed: Int): Stream[Pure, Student] =
    randomInts(seed).zip(randomStrings(seed)).map { t =>
      Student(t._1, t._2)
    }

  // creating infinite streams of domain classes using destructuring
  def randomStudentScores(seed: Int): Stream[Pure, StudentScore] = {
    randomInts(seed).zip(randomStrings(seed)).zip(randomScores(seed)).map {t =>
      t match {
        case ((id, name), score) => StudentScore(id, name, score)
      }
    }
  }

}