package it.unibo.pcd

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.ActorReceptionistMessages.Register
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BoidActorsReceptionistTest extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  val testKit: ActorTestKit = ActorTestKit()

  override def afterAll(): Unit = testKit.shutdownTestKit()

  "A Receptionist" must:
    "support registering" in:
      val receptionistProbe = testKit.createTestProbe[ActorReceptionistMessages]()
      val boidActor = testKit.spawn[BoidActorMessages](BoidActor(receptionistProbe.ref, 1), "boid")


      import ActorReceptionistMessages.*
      receptionistProbe expectMessage Register(1.toString, boidActor)
