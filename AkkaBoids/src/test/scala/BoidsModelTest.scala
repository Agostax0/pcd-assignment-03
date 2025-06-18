package it.unibo.pcd

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.{have, not}
import org.scalatest.matchers.should.Matchers.should

class BoidsModelTest extends AnyFlatSpec:

  "A BoidsModel" should "be correctly instantiated" in:
    val model = BoidsModel.empty

    model.boids should have size 0

  it should "correctly initialize boids upon request" in:
    val model: BoidsModel = BoidsModel.empty.initBoids(100)

    model.boids should have size 100

  it should "update boids correctly" in:
    val model: BoidsModel = BoidsModel.empty.initBoids(100)

    val boids = model.boids

    val updatedModel = model.boids_=(boids.map(_.update(model)))

    boids should not equal updatedModel.boids
