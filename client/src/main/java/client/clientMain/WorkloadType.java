package client.clientMain;

/**
 * 1=read heavy, 2=mixed, 3=write heavy
 * 
 * @author Ricardo Ara&uacute;jo Santos - ricardo@lsd.ufcg.edu.br  (created in 08/01/2014)
 */
public enum WorkloadType {
	
	READ_HEAVY(0.05), MIXED(0.1), WRITE_HEAVY(0.15);
	
	private final double newClientProb;
	
	/**
	 * Constructor.
	 * @param newClientProb
	 */
	WorkloadType(double newClientProb){
		this.newClientProb = newClientProb;
	}

	/**
	 * @return probability of a client be a newcomer at system (require more write at database)
	 */
	public double getNewClientProb() {
		return newClientProb;
	}
}
