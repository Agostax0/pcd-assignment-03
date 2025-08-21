package it.unibo.pcd.legacy;

import it.unibo.pcd.model.BoidsModel;
import it.unibo.pcd.model.Position;
import it.unibo.pcd.model.Velocity;

import java.util.ArrayList;
import java.util.List;

public class Boid {

    private Position pos;
    private Velocity vel;

    public Boid(Position pos, Velocity vel) {
    	this.pos = pos;
    	this.vel = vel;
    }
    
    public Position getPos() {
    	return pos;
    }

    public Velocity getVel() {
    	return vel;
    }
    
    public void update(BoidsModel model, List<Boid> nearbyBoids) {

    	/* change velocity vector according to separation, alignment, cohesion */

    	Velocity separation = calculateSeparation(nearbyBoids, model);
    	Velocity alignment = calculateAlignment(nearbyBoids, model);
    	Velocity cohesion = calculateCohesion(nearbyBoids, model);
    	
    	vel = vel.$plus(alignment.$mul(model.alignmentWeight()))
    			.$plus(separation.$mul(model.separationWeight()))
    			.$plus(cohesion.$mul(model.cohesionWeight()));
        
        /* Limit speed to MAX_SPEED */

        double speed = vel.abs();
        
        if (speed > model.maxSpeed()) {
            vel = vel.normalized().$mul(model.maxSpeed());
        }

        /* Update position */

        pos = pos.$plus(new Position(vel.x(), vel.y()));
        
        /* environment wrap-around */
        
        if (pos.x() < model.minX()) pos = pos.$plus(new Position(model.width(), 0));
        if (pos.x() >= model.maxX()) pos = pos.$plus(new Position(-model.width(), 0));
        if (pos.y() < model.minY()) pos = pos.$plus(new Position(0, model.height()));
        if (pos.y() >= model.maxY()) pos = pos.$plus(new Position(0, -model.height()));
    }

    private Velocity calculateAlignment(List<Boid> nearbyBoids, BoidsModel model) {
        double avgVx = 0;
        double avgVy = 0;
        if (nearbyBoids!= null && !nearbyBoids.isEmpty()) {
	        for (Boid other : nearbyBoids) {
	        	Velocity otherVel = other.getVel();
	            avgVx += otherVel.x();
	            avgVy += otherVel.y();
	        }	        
	        avgVx /= nearbyBoids.size();
	        avgVy /= nearbyBoids.size();
	        return new Velocity(avgVx - vel.x(), avgVy - vel.y()).normalized();
        } else {
        	return new Velocity(0, 0);
        }
    }

    private Velocity calculateCohesion(List<Boid> nearbyBoids, BoidsModel model) {
        double centerX = 0;
        double centerY = 0;
        if (nearbyBoids != null && !nearbyBoids.isEmpty()) {
	        for (Boid other: nearbyBoids) {
	        	Position otherPos = other.getPos();
	            centerX += otherPos.x();
	            centerY += otherPos.y();
	        }
            centerX /= nearbyBoids.size();
            centerY /= nearbyBoids.size();
            return new Velocity(centerX - pos.x(), centerY - pos.y()).normalized();
        } else {
        	return new Velocity(0, 0);
        }
    }
    
    private Velocity calculateSeparation(List<Boid> nearbyBoids, BoidsModel model) {
        double dx = 0;
        double dy = 0;
        int count = 0;
        if (nearbyBoids != null && !nearbyBoids.isEmpty()) {
            for (Boid other: nearbyBoids) {
                Position otherPos = other.getPos();
                double distance = pos.distance(otherPos);
                if (distance < model.avoidRadius()) {
                    dx += pos.x() - otherPos.x();
                    dy += pos.y() - otherPos.y();
                    count++;
                }
            }
        }
        if (count > 0) {
            dx /= count;
            dy /= count;
            return new Velocity(dx, dy).normalized();
        } else {
        	return new Velocity(0, 0);
        }
    }
}
