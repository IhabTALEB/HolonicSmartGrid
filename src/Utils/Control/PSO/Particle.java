/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils.Control.PSO;

/**
 *
 * @author ihab
 */
public class Particle {

	double[] position; //The position vector of this particle
	double fitness; //The fitness of this particle
	double[] velocity; //The velocity vector of this particle
	double[] personalBest; //Personal best of the particle

	public Particle(double[] position, double[] velocity) {
		this.position = position;
		this.velocity = velocity; 
	}
	
	
}