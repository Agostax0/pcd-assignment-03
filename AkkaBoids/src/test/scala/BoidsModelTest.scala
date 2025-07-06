package it.unibo.pcd

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.{contain, have, not}
import org.scalatest.matchers.should.Matchers.should

class BoidsModelTest extends AnyFlatSpec {}
//
//  "A BoidsModel" should "be correctly instantiated" in:
//    val model = BoidsModel.
//
//    model.boids should have size 0
//
//  it should "correctly initialize boids upon request" in:
//    val boids = BoidsModel.local.initBoids(100)
//
//    boids should have size 100
//
//  it should "update boids correctly" in:
//    val boids = LocalBoidsModel().initBoids(100).view.toList
//
//    val updatedModel = LocalBoidsModel(boids = boids.map(_.update(LocalBoidsModel())))
//
//    boids should not equal updatedModel.boids
//    boids should have size updatedModel.boids.size
