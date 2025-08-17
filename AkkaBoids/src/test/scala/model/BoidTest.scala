package it.unibo.pcd
package model

import it.unibo.pcd.model.Boid.Boid
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.{must, mustBe}
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.should
class BoidTest extends AnyFlatSpec with Matchers:

  "a position" should "be added correctly" in:
    val p1 = Position(1, 2)
    val p2 = Position(3, 4)
    val p3 = p1 + p2

    p3.x should be(4)
    p3.y should be(6)

  it should "be subtracted correctly" in:
    val p1 = Position(1, 2)
    val p2 = Position(3, 4)

    val p3 = p1 - p2
    p3.x should be(-2)
    p3.y should be(-2)

  it should "distance should be correct" in:
    val p1 = Position(1, 1)
    val p2 = Position.zero
    import math.sqrt
    p1.distance(p2) mustBe sqrt(2)

  "A velocity" should "be added correctly" in:
    val v1 = Velocity(1, 2)
    val v2 = Velocity(3, 4)
    val v3 = v1 + v2

    v3.x should be(4)
    v3.y should be(6)

  "A velocity" should "be multiplied correctly" in:
    val v1 = Velocity(1, 2)
    val v2 = v1 * 2
    v2.x should be(2)
    v2.y should be(4)

  "A velocity" should "be normalized correctly" in:
    val v1 = Velocity(1, 1)
    val v2 = v1.normalized

    import math.sqrt
    v2.x should be(1 / sqrt(2))
    v2.y should be(1 / sqrt(2))

  "A boid" should "move when found near another" in:
    val boid1: Boid = Boid(Position.zero, Velocity.zero)
    val boid2: Boid = Boid(Position.zero, Velocity.zero)

    val model = BoidsModel.localModel

    val newBoid1 = model.update(boid1, List(boid2))
    val newBoid2 = model.update(boid2, List(boid1))

    boid1 should not equals newBoid1
    boid2 should not equals newBoid2

  it should "move correctly when multiple are nearby" in:
    val boid1: Boid = Boid(Position.one, Velocity.one)

    val model = BoidsModel.localModel

    val randomBoid: () => Boid = () => Boid(Position.random, Velocity.random)

    val newBoid1 = model.update(boid1, List(randomBoid(), randomBoid(), randomBoid()))

    boid1 should not equals newBoid1
