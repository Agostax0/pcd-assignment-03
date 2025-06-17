package it.unibo.pcd

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.have
import org.scalatest.matchers.should.Matchers.should

class BoidsModelTest extends AnyFlatSpec:

  "A BoidsModel" should "be correctly instantiated" in:
    val model = BoidsModel.empty

    model.boids should have size 0

  it should "correctly initialize boids upon request" in:
    val model: BoidsModel = BoidsModel.empty.initBoids(100)

    model.boids should have size 100
